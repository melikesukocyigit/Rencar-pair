package com.turkcell.rencar_pair.ui.common.photo

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.turkcell.rencar_pair.ui.theme.Primary
import com.turkcell.rencar_pair.ui.theme.bodyM
import com.turkcell.rencar_pair.ui.theme.titleM
import java.io.File
import java.io.FileOutputStream

/**
 * Kamera + galeri ile fotograf secimini tek yerde toplayan ortak bilesen.
 * Ehliyet dogrulama ekranindaki selfie deseninden cikarildi; arac durum
 * fotograflari (surus oncesi 4 yon) ayni mekanizmayi miras alir.
 *
 * Kamera bilincli olarak TakePicturePreview kullanir: FileProvider/manifest
 * kurulumu gerektirmez ve MVP kapsaminda dogrulama amacli cozunurluk yeterlidir
 * (karar docs/decisions.md'de).
 */
class PhotoPicker(
    val launchCamera: (cacheFileName: String) -> Unit,
    val launchGallery: () -> Unit,
)

@Composable
fun rememberPhotoPicker(onImageReady: (Uri) -> Unit): PhotoPicker {
    val context = LocalContext.current
    val currentOnImageReady by rememberUpdatedState(onImageReady)
    // TakePicturePreview sonucu dondugunde hangi dosya adiyla cache'e yazilacagini
    // bilmek icin launch sirasinda secilen ad tutulur (ayni picker'i birden fazla
    // hedef icin kullanan ekranlar yan basina farkli ad gecebilsin diye).
    val pendingCacheName = remember { arrayOf("photo_temp.jpg") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            runCatching {
                val cacheFile = File(context.cacheDir, pendingCacheName[0])
                FileOutputStream(cacheFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                currentOnImageReady(Uri.fromFile(cacheFile))
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            currentOnImageReady(uri)
        }
    }

    return remember {
        PhotoPicker(
            launchCamera = { cacheFileName ->
                pendingCacheName[0] = cacheFileName
                cameraLauncher.launch(null)
            },
            launchGallery = { galleryLauncher.launch("image/*") },
        )
    }
}

/** Secilen gorseli upload icin byte dizisine cevirir; okunamazsa null doner. */
fun readImageBytes(contentResolver: ContentResolver, uri: Uri): ByteArray? =
    runCatching { contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()

/**
 * "Kamera ile cek / Galeriden sec" alt sayfasi. Ehliyet ekranindaki selfie
 * seciciyle ayni gorunum; secim sonrasi kapatma sorumlulugu cagirana aittir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(
    title: String,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = titleM,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(24.dp))
            PhotoSourceRow(
                icon = Icons.Default.CameraAlt,
                label = "Kamera ile çek",
                onClick = onCameraSelected,
            )
            Spacer(modifier = Modifier.height(12.dp))
            PhotoSourceRow(
                icon = Icons.Default.Collections,
                label = "Galeriden seç",
                onClick = onGallerySelected,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PhotoSourceRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = bodyM,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
