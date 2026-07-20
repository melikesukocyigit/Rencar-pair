package com.turkcell.rencar_pair.domain.model

sealed interface PaymentRequest {
    data object Wallet : PaymentRequest
    data class Card(val cardId: String) : PaymentRequest
    data class Iyzico(val paymentId: String) : PaymentRequest
}
