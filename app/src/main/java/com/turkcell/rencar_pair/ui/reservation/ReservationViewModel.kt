package com.turkcell.rencar_pair.ui.reservation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencar_pair.data.repository.RentalRepository
import com.turkcell.rencar_pair.data.repository.ReservationRepository
import com.turkcell.rencar_pair.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReservationUiState(
            vehicleId = savedStateHandle["vehicleId"] ?: "",
            brand = savedStateHandle["brand"] ?: "",
            model = savedStateHandle["model"] ?: "",
            plate = savedStateHandle["plate"] ?: "",
            pricePerDay = savedStateHandle.get<String>("pricePerDay")?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ReservationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Plan hizlica degistirilirse eski bir quote cevabinin gec gelip state'i yanlis
    // plana ait fiyatla ezmesini engellemek icin son istegin kimligi tutulur.
    private var quoteRequestId = 0

    init {
        loadQuote(_uiState.value.selectedPlan)
    }

    fun onIntent(intent: ReservationIntent) {
        when (intent) {
            is ReservationIntent.PlanSelected -> {
                _uiState.update { it.copy(selectedPlan = intent.plan) }
                loadQuote(intent.plan)
            }

            is ReservationIntent.TermsToggled ->
                _uiState.update { it.copy(termsAccepted = intent.accepted) }

            ReservationIntent.ConfirmClicked -> confirmReservation()
        }
    }

    // Ekranda sure secici olmadigindan, estimatedDurationLabel ile ayni sabit sureler
    // quote sorgusunda da kullanilir (Dakikalik->30dk, Saatlik->60dk, Gunluk->1440dk).
    private fun minutesFor(plan: RentalPlan): Int = when (plan) {
        RentalPlan.DAKIKALIK -> 30
        RentalPlan.SAATLIK -> 60
        RentalPlan.GUNLUK -> 1440
    }

    private fun loadQuote(plan: RentalPlan) {
        val requestId = ++quoteRequestId
        val vehicleId = _uiState.value.vehicleId
        viewModelScope.launch {
            _uiState.update { it.copy(isQuoteLoading = true, quoteError = null) }
            val result = vehicleRepository.getQuote(vehicleId, apiPlanFor(plan), minutesFor(plan))
            if (requestId != quoteRequestId) return@launch // plan bu sirada degisti, cevap eski
            result
                .onSuccess { quote ->
                    _uiState.update { it.copy(isQuoteLoading = false, quote = quote, quoteError = null) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isQuoteLoading = false, quote = null, quoteError = error.message ?: "Fiyat alınamadı.")
                    }
                }
        }
    }

    // API v2 kurali: kiralama YALNIZ aktif bir rezervasyon uzerinden acilir (POST /rentals,
    // aksi halde 409 doner). Bu yuzden "Rezervasyonu Tamamla" once POST /reservations ile
    // araci RESERVED durumuna alip, ancak basarili olursa POST /rentals'i cagiriyor.
    private fun confirmReservation() {
        val state = _uiState.value
        if (!state.isConfirmEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val reservationResult = reservationRepository.reserveVehicle(state.vehicleId)
            val reservation = reservationResult.getOrNull()
            if (reservation == null) {
                _uiState.update { it.copy(isSubmitting = false) }
                _effect.send(
                    ReservationEffect.ShowError(
                        reservationResult.exceptionOrNull()?.message ?: "Araç rezerve edilemedi.",
                    ),
                )
                return@launch
            }

            val endDate = endDateFor(state.selectedPlan)
            val result = rentalRepository.createRental(state.vehicleId, endDate, apiPlanFor(state.selectedPlan))
            _uiState.update { it.copy(isSubmitting = false) }
            result
                .onSuccess { rental ->
                    _effect.send(
                        ReservationEffect.NavigateToVehicleCondition(
                            rentalId = rental.id,
                            vehicleId = state.vehicleId,
                            brand = state.brand,
                            model = state.model,
                            plate = state.plate,
                            pricePerDay = state.pricePerDay,
                        ),
                    )
                }
                .onFailure { _effect.send(ReservationEffect.ShowError(it.message ?: "Rezervasyon oluşturulamadı.")) }
        }
    }

    // API v2: plan alani wire degerleri (RentalDtos.kt) Turkce enum adlarindan farkli.
    private fun apiPlanFor(plan: RentalPlan): String = when (plan) {
        RentalPlan.DAKIKALIK -> "PER_MINUTE"
        RentalPlan.SAATLIK -> "HOURLY"
        RentalPlan.GUNLUK -> "DAILY"
    }

    // endDate yalniz DAILY planda zorunlu/anlamli (RentalDtos.kt:8); PER_MINUTE/HOURLY
    // kiralamalar endDate'e ulasarak degil ayri bir /rentals/{id}/finish cagrisiyla
    // bitiyor (Batch 2 karari, docs/decisions.md), bu yuzden bu planlarda null gonderilir.
    // java.time yerine SimpleDateFormat kullanildi: minSdk 24'te java.time,
    // core library desugaring olmadan calismiyor.
    private fun endDateFor(plan: RentalPlan): String? {
        if (plan != RentalPlan.GUNLUK) return null
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(calendar.time)
    }
}
