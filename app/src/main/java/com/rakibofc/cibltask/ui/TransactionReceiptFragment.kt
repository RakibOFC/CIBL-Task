package com.rakibofc.cibltask.ui

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.rakibofc.cibltask.R
import com.rakibofc.cibltask.databinding.FragmentTransactionReceiptBinding
import com.rakibofc.cibltask.model.TransactionDetails
import com.rakibofc.cibltask.util.Values
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable

class TransactionReceiptFragment : DialogFragment() {

    private lateinit var binding: FragmentTransactionReceiptBinding
    private lateinit var transactionDetails: TransactionDetails

    companion object {
        private const val ARG_TRANSACTION_DETAILS = "transaction_details"
        const val TAG = "TransactionReceiptDialogFragment"

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

        binding.btnDownload.setOnClickListener {
            downloadAsPdf()
        }

        return binding.root
    }

    private fun downloadAsPdf() {

        if (Build.VERSION.SDK_INT >= 33)
        // convertXmlToPdf()
            downloadPdf()
        else
            requestForFilePermission()
    }

    private fun requestForFilePermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                Values.FILE_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is already granted, proceed with file write
            // convertXmlToPdf()
            downloadPdf()
        }
    }

    @Suppress("DEPRECATION")
    private fun downloadPdf() {

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
        val viewWidth = rvAyahHighlightList.measuredWidth;
        val viewHeight = rvAyahHighlightList.measuredHeight;

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

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "Transaction Receipt.pdf"
        val filePath = File(downloadsDir, fileName)

        try {
            // Save the document to a file
            val fos = FileOutputStream(filePath)
            document.writeTo(fos)
            document.close()
            fos.close()
            // PDF conversion successful
            Toast.makeText(requireContext(), "Download Successfully", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            Log.e("TAG", e.message.toString())
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }

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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Values.FILE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file write
                downloadPdf()
            } else {
                // Permission denied, show a message or handle it gracefully
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}