package com.rakibofc.cibltask.model

import java.io.Serializable

data class TransactionDetails(
    val phone: String,
    val name: String,
    val amount: Double,
    val narration: String,
    val paymentMethod: String,
    val address: String
) : Serializable