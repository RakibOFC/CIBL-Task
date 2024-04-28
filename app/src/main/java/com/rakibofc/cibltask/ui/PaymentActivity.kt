package com.rakibofc.cibltask.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rakibofc.cibltask.R
import com.rakibofc.cibltask.databinding.ActivityPaymentBinding

class PaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}