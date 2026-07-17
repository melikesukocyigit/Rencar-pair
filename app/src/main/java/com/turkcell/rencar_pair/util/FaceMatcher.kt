package com.turkcell.rencar_pair.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.sqrt

sealed interface FaceMatchResult {
    /** similarity: 0..1 kosinus benzerligi; isMatch: esik uzerinde mi. */
    data class Success(val similarity: Float, val isMatch: Boolean) : FaceMatchResult
    data object NoFaceInLicense : FaceMatchResult
    data object NoFaceInSelfie : FaceMatchResult
    data class Error(val message: String) : FaceMatchResult
}

/**
 * Cihaz uzerinde (on-device) yuz eslestirme. Ehliyet on yuzundeki fotograf ile
 * kullanicinin selfie'sini karsilastirir; internet gerektirmez.
 *
 * Boru hatti: ML Kit ile yuz tespit + kirp -> TFLite (MobileFaceNet) ile embedding
 * cikar -> kosinus benzerligi. Model assets/mobile_facenet.tflite'tan yuklenir;
 * input/output boyutlari calisma aninda modelden okunur.
 */
@Singleton
class FaceMatcher @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "FaceMatcher"
        private const val MODEL_ASSET = "mobile_facenet.tflite"

        // Kosinus benzerligi esigi. Akademik FaceNet/MobileFaceNet benchmark'larindaki
        // 0.70 gibi degerler, yuksek cozunurluklu, duz-bakan, iyi isikli cift icin
        // gecerlidir. Bizim gercek girdimiz bundan cok daha zorlu:
        //   - Kimlik/ehliyet uzerindeki yuz, karta gore kucuk bir alan (~1/4);
        //     kirpilip 112x112'ye buyutulunce netlik ciddi dusuyor.
        //   - MobileFaceNet burada ince ayarsiz (fine-tune edilmemis) genel model;
        //     Turkce kimlik fotograflarina ozel egitilmedi.
        //   - Selfie ile kimlik fotografi arasinda acis/isik/ifade farki var.
        // Bu ucu birlikte, ayni kisi icin bile benzerlik skorunu dusuruyor. Esik bu
        // yuzden akademik degerin cok altinda, demo/MVP icin bilincli tutuluyor: amac
        // gercek kisiyi kesin reddetmemek (false-negative onceligi). Bu, guvenligi
        // zayiflatan bir tercih ama zaten bu akis prod-guvenlik siniri degil, opsiyonel
        // bir hizlandirma butonu (bkz. docs/decisions.md, docs/ml-face-matching.md).
        // Her match() cagrisinda gercek skor Logcat'e yazilir ("FaceMatcher" tag'i) -
        // ilk gercek testlerden sonra bu deger kalibre edilmelidir.
        const val THRESHOLD = 0.35f

        private const val MAX_INPUT_DIM = 1024 // ML Kit'e vermeden once olcekleme (OOM onlemi)
    }

    private val detector by lazy {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
        )
    }

    private val interpreter: Interpreter by lazy {
        Interpreter(FileUtil.loadMappedFile(context, MODEL_ASSET))
    }

    private val inputSize: Int by lazy { interpreter.getInputTensor(0).shape()[1] }
    private val embeddingSize: Int by lazy { interpreter.getOutputTensor(0).shape()[1] }

    suspend fun match(licenseBytes: ByteArray, selfieBytes: ByteArray): FaceMatchResult {
        return try {
            val licenseBitmap = decode(licenseBytes)
                ?: return FaceMatchResult.Error("Ehliyet görseli okunamadı.")
            val selfieBitmap = decode(selfieBytes)
                ?: return FaceMatchResult.Error("Selfie görseli okunamadı.")

            val licenseFace = detectAndCrop(licenseBitmap)
                ?: return FaceMatchResult.NoFaceInLicense
            val selfieFace = detectAndCrop(selfieBitmap)
                ?: return FaceMatchResult.NoFaceInSelfie

            val licenseEmbedding = embed(licenseFace)
            val selfieEmbedding = embed(selfieFace)
            val similarity = cosineSimilarity(licenseEmbedding, selfieEmbedding)
            val isMatch = similarity >= THRESHOLD
            // Kalibrasyon icin: gercek skor her zaman loglanir (esigi gecsin ya da
            // gecmesin). Test sirasinda bu satiri izleyip THRESHOLD'u ayarlayin.
            Log.d(TAG, "similarity=$similarity threshold=$THRESHOLD isMatch=$isMatch")
            FaceMatchResult.Success(similarity = similarity, isMatch = isMatch)
        } catch (e: Exception) {
            FaceMatchResult.Error(e.message ?: "Yüz analizi başarısız oldu.")
        }
    }

    private fun decode(bytes: ByteArray): Bitmap? =
        runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull()

    private suspend fun detectAndCrop(bitmap: Bitmap): Bitmap? {
        val scaled = scaleDown(bitmap, MAX_INPUT_DIM)
        val faces = suspendCancellableCoroutine { cont ->
            detector.process(InputImage.fromBitmap(scaled, 0))
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        // Birden fazla yuz varsa en buyugunu (ana yuz) sec.
        val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
            ?: return null
        val box = face.boundingBox
        val left = box.left.coerceAtLeast(0)
        val top = box.top.coerceAtLeast(0)
        val right = box.right.coerceAtMost(scaled.width)
        val bottom = box.bottom.coerceAtMost(scaled.height)
        if (right <= left || bottom <= top) return null
        return Bitmap.createBitmap(scaled, left, top, right - left, bottom - top)
    }

    private fun embed(faceBitmap: Bitmap): FloatArray {
        val resized = Bitmap.createScaledBitmap(faceBitmap, inputSize, inputSize, true)
        val input = toNormalizedBuffer(resized)
        val output = Array(1) { FloatArray(embeddingSize) }
        interpreter.run(input, output)
        return l2Normalize(output[0])
    }

    private fun toNormalizedBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            // MobileFaceNet standart on-isleme: (v - 127.5) / 128
            buffer.putFloat((r - 127.5f) / 128f)
            buffer.putFloat((g - 127.5f) / 128f)
            buffer.putFloat((b - 127.5f) / 128f)
        }
        return buffer
    }

    private fun l2Normalize(vector: FloatArray): FloatArray {
        var norm = 0f
        for (value in vector) norm += value * value
        norm = sqrt(norm)
        if (norm == 0f) return vector
        return FloatArray(vector.size) { vector[it] / norm }
    }

    // Iki vektor de L2-normalize edildiginden nokta carpim = kosinus benzerligi.
    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        for (i in a.indices) dot += a[i] * b[i]
        return dot
    }

    private fun scaleDown(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxOf(w, h)
        return Bitmap.createScaledBitmap(bitmap, (w * ratio).toInt(), (h * ratio).toInt(), true)
    }
}
