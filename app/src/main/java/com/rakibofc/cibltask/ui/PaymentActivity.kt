package com.rakibofc.cibltask.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.textfield.TextInputEditText
import com.rakibofc.cibltask.R
import com.rakibofc.cibltask.databinding.ActivityPaymentBinding
import com.rakibofc.cibltask.model.TransactionDetails
import com.rakibofc.cibltask.util.Values
import java.io.IOException
import java.util.Locale


class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var paymentMethodName = ""
    private var address = "Gazipur"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check location permission
        checkLocationPermission()

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
            address
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

    private fun checkLocationPermission() {
        if (hasLocationPermission()) {
            // Location permission granted
            currentLocation()
        } else {
            // If user press OKAY button the dialog will be dismiss;  Request for permission
            locationPermissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private val locationPermissionResult: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted
                currentLocation()
            } else {
                // Permission denied
                showToast(getString(R.string.location_permission_denied_message))
            }
        }

    private fun currentLocation() {
        val fLocationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(applicationContext)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fLocationProviderClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource()
                .token
        ).addOnCompleteListener { task ->

            // Get the location updates from task
            val location: Location = task.result


            // Generate address line
            generateAddressLine(location.latitude, location.longitude)
        }
    }

    @Suppress("DEPRECATION")
    private fun generateAddressLine(latitude: Double, longitude: Double) {

        val geocoder = Geocoder(applicationContext, Locale.ENGLISH)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latitude, longitude, 1
            ) { addresses: List<Address?>? ->
                getPlaceName(addresses)
            }
        } else {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                getPlaceName(addresses)

            } catch (e: IOException) {
                address = "Dhaka"
            }
        }
    }

    private fun getPlaceName(addresses: List<Address?>?): String {

        try {
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val localArea = addresses[0]?.locality
                    val district = addresses[0]?.subAdminArea
                    val division = addresses[0]?.adminArea
                    val addressLine = addresses[0]?.getAddressLine(0)

                    if (localArea != null) {
                        address = localArea
                    } else if (district != null) {
                        address = district
                    } else if (division != null) {
                        address = division
                    } else if (addressLine != null) {
                        address = addressLine
                    }
                }
            }
        } catch (_: Exception) {
        }
        return address
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    // Method to check location permission
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}