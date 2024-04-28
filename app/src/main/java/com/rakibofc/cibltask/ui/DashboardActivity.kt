package com.rakibofc.cibltask.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rakibofc.cibltask.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}