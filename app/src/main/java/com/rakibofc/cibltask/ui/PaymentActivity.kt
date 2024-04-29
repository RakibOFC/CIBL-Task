package com.rakibofc.cibltask.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.rakibofc.cibltask.R
import com.rakibofc.cibltask.databinding.ActivityPaymentBinding
import com.rakibofc.cibltask.model.TransactionDetails
import com.rakibofc.cibltask.util.Values

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var paymentMethodName =" "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val paymentMethod = intent.getStringExtra(Values.PAYMENT_METHOD_KEY)
        val paymentMethodLogo: Int

        if (paymentMethod.equals(Values.PAYMENT_METHOD_BKASH)) {
            paymentMethodLogo = R.drawable.logo_bkash
            paymentMethodName = "bKash"
        } else {
            paymentMethodLogo = R.drawable.logo_nagad
            paymentMethodName = "Nagad"
        }

        binding.ivMethodLogo.setImageResource(paymentMethodLogo)
        binding.tiLayoutPhone.hint =
            String.format(getString(R.string.payment_s_phone_text), paymentMethodName)
        binding.tiLayoutName.hint =
            String.format(getString(R.string.payment_s_name_text), paymentMethodName)

        binding.btnSubmit.setOnClickListener {
            submitForm()
            /*showTransactionDialog(
                TransactionDetails(
                    "", "", 0.0, "",
                    paymentMethod,
                    "Dhaka"
                )
            )*/
        }
    }

    private fun submitForm() {

        val inputPhone = binding.inputPhone.text.toString().trim()
        val inputName = binding.inputName.text.toString().trim()
        val inputAmount = binding.inputAmount.text.toString().trim()
        val inputNarration = binding.inputNarration.text.toString().trim()

        if (inputPhone.isEmpty()) {
            showError(binding.inputPhone, R.string.enter_phone_no_text)
            return
        }

        if (inputName.isEmpty()) {
            showError(binding.inputName, R.string.enter_name_text)
            return
        }

        if (inputAmount.isEmpty()) {
            showError(binding.inputAmount, R.string.enter_amount_text)
            return
        }

        if (inputNarration.isEmpty()) {
            showError(binding.inputNarration, R.string.enter_narration_text)
            return
        }

        val transactionDetails = TransactionDetails(
            inputPhone,
            inputName,
            inputAmount.toDouble(),
            inputNarration,
            paymentMethodName,
            "Dhaka"
        )

        showTransactionDialog(transactionDetails)
    }

    private fun showTransactionDialog(transactionDetails: TransactionDetails) {

        val transactionReceiptFragment = TransactionReceiptFragment.newInstance(transactionDetails)
        transactionReceiptFragment.show(supportFragmentManager, TransactionReceiptFragment.TAG)
    }

    private fun showError(view: View, @StringRes errorMessageResId: Int) {
        if (view is TextInputEditText) {
            view.error = getString(errorMessageResId)
            view.requestFocus()
        }
    }
}