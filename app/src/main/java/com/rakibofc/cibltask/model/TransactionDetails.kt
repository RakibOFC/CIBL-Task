package com.rakibofc.cibltask.model

import android.os.Parcelable
import java.io.Serializable

data class TransactionDetails(
    val inputPhone: String,
    val inputName: String,
    val inputAmount: Double,
    val inputNarration: String,
    val paymentMethod: String?,
    val location: String
) : Serializable