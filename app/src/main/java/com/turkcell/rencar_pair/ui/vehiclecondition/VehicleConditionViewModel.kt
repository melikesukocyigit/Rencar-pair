package com.turkcell.rencar_pair.ui.vehiclecondition

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleConditionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VehicleConditionUiState(
            rentalId = savedStateHandle["rentalId"] ?: "",
            vehicleId = savedStateHandle["vehicleId"] ?: "",
            brand = savedStateHandle["brand"] ?: "",
            model = savedStateHandle["model"] ?: "",
            plate = savedStateHandle["plate"] ?: "",
            pricePerDay = savedStateHandle.get<String>("pricePerDay")?.toDoubleOrNull() ?: 0.0,
        ),
    )
    val uiState: StateFlow<VehicleConditionUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VehicleConditionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: VehicleConditionIntent) {
        when (intent) {
            is VehicleConditionIntent.PhotoMockCaptured ->
                _uiState.update { it.copy(checkedSides = it.checkedSides + intent.side) }

            VehicleConditionIntent.StartRentalClicked -> startRental()
        }
    }

    private fun startRental() {
        val state = _uiState.value
        if (!state.isStartEnabled) return
        viewModelScope.launch {
            _effect.send(
                VehicleConditionEffect.NavigateToActiveRental(
                    rentalId = state.rentalId,
                    vehicleId = state.vehicleId,
                    brand = state.brand,
                    model = state.model,
                    plate = state.plate,
                    pricePerDay = state.pricePerDay,
                ),
            )
        }
    }
}
