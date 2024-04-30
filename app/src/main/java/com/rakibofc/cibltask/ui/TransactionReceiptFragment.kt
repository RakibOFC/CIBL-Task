package com.rakibofc.cibltask.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.rakibofc.cibltask.R
import com.rakibofc.cibltask.databinding.FragmentTransactionReceiptBinding
import com.rakibofc.cibltask.model.TransactionDetails
import com.rakibofc.cibltask.util.FileHandler
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionReceiptFragment : DialogFragment() {

    private lateinit var binding: FragmentTransactionReceiptBinding
    private lateinit var transactionDetails: TransactionDetails

    companion object {
        private const val ARG_TRANSACTION_DETAILS = "transaction_details"
        const val TAG = "ReceiptDialogFragment"

        fun newInstance(transactionDetails: TransactionDetails) =
            TransactionReceiptFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_TRANSACTION_DETAILS, transactionDetails)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTransactionReceiptBinding.inflate(inflater, container, false)

        initDataInView()

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        binding.btnDownload.setOnClickListener {
            requestForDownloadReceipt(false)
        }
        binding.btnShare.setOnClickListener {
            requestForDownloadReceipt(true)
        }

        return binding.root
    }

    private fun initDataInView() {
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)

        // Set payment method logo
        binding.ivMethodLogo.setImageResource(
            if (transactionDetails.paymentMethod == "bKash") R.drawable.logo_bkash
            else R.drawable.logo_nagad
        )

        // Payment method name with "Fund transfer" text
        binding.tvFundTransfer.text =
            getString(R.string.s_fund_transfer_text, transactionDetails.paymentMethod)

        binding.tvMethodNumber.text =
            getString(R.string.payment_s_number_text, transactionDetails.paymentMethod)

        binding.tvPhoneNo.text = transactionDetails.phone
        binding.tvAmount.text = transactionDetails.amount.toString()
        binding.tvTnDateTime.text = dateTimeFormat.format(Date())
        binding.tvNarration.text = transactionDetails.narration
        binding.tvMethodName.text =
            getString(R.string.payment_s_name_text, transactionDetails.paymentMethod)
        binding.tvPersonName.text = transactionDetails.name
        binding.tvAddress.text = transactionDetails.address
        binding.tvTotalAmount.text =
            getString(R.string.bdt_s_text, transactionDetails.amount.toString())
    }

    private fun requestForDownloadReceipt(isShareable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            downloadPdf(isShareable)
        else {
            if (hasFileWritePermission())
                downloadPdf(isShareable)
            else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    // Method to check file write permission
    private fun hasFileWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted
            downloadPdf(true)
        } else {
            // Permission denied
            showToast(getString(R.string.permission_denied_message))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    @Suppress("DEPRECATION")
    private fun downloadPdf(isShareable: Boolean) {

        // Inflate the XML layout file
        val rvAyahHighlightList = binding.llcTransactionContainer.rootView
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().display!!.getRealMetrics(displayMetrics)
        } else {
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        }

        rvAyahHighlightList.measure(
            View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
        )

        rvAyahHighlightList.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)

        // Create a new PdfDocument instance
        val document = PdfDocument()

        // Obtain the width and height of the view
        val viewWidth = rvAyahHighlightList.measuredWidth
        val viewHeight = rvAyahHighlightList.measuredHeight

        val pageInfo = PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create()

        // Start a new page
        val page = document.startPage(pageInfo)

        // Get the Canvas object to draw on the page
        val canvas = page.canvas

        // Create a Paint object for styling the view
        val paint = Paint()
        paint.setColor(Color.WHITE)

        // Draw the view on the canvas
        rvAyahHighlightList.draw(canvas)

        // Finish the page
        document.finishPage(page)

        lifecycleScope.launch {
            FileHandler.savePdfFile(requireContext(), document, isShareable)
        }

        // Dismiss alert dialog after download receipt
        dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionDetails =
                it.getSerializableCompat(ARG_TRANSACTION_DETAILS, TransactionDetails::class.java)
        }
    }

    @Suppress("UNCHECKED_CAST", "DEPRECATION")
    private fun <T : Serializable?> Bundle.getSerializableCompat(key: String, clazz: Class<T>): T {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) getSerializable(
            key,
            clazz
        )!! else (getSerializable(key) as T)
    }

    override fun getTheme(): Int {
        return R.style.TransactionReceiptDialog
    }
}