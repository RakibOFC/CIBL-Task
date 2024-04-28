package com.rakibofc.cibltask.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.rakibofc.cibltask.R
import com.rakibofc.cibltask.databinding.ActivityDashboardBinding
import com.rakibofc.cibltask.util.Values

class DashboardActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mcvBkash.setOnClickListener(this)
        binding.mcvNagad.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        Log.e("TAG", "onClick: ")

        val intent = Intent(applicationContext, PaymentActivity::class.java)

        when (v?.id) {
            R.id.mcvBkash -> intent.putExtra(Values.PAYMENT_METHOD_KEY, Values.PAYMENT_METHOD_BKASH)
            R.id.mcvNagad -> intent.putExtra(Values.PAYMENT_METHOD_KEY, Values.PAYMENT_METHOD_NAGAD)
        }
        startActivity(intent)
    }
}