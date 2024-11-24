package com.kayodedaniel.gogovmobile.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.kayodedaniel.gogovmobile.R
import java.io.File

class PDFViewerActivity : AppCompatActivity() {

    private lateinit var linearLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null

    private var screenWidth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdfviewer)

        linearLayout = findViewById(R.id.linearLayout)
        progressBar = findViewById(R.id.progressBar)

        // Gets screen width
        screenWidth = resources.displayMetrics.widthPixels

        val pdfFile = File(intent.getStringExtra("PDF_PATH") ?: return)
        openPdf(pdfFile)
    }

    private fun openPdf(pdfFile: File) {
        try {
            val parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            renderAllPages() // Renders all pages in the pdf for displaying
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // method for rendering all the pages, using page count for the document
    private fun renderAllPages() {
        progressBar.visibility = View.VISIBLE
        pdfRenderer?.let { renderer ->
            val pageCount = renderer.pageCount
            for (pageIndex in 0 until pageCount) {
                renderPage(pageIndex)
            }
        }
    }

    private fun renderPage(pageIndex: Int) {
        pdfRenderer?.let { renderer ->
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return

            currentPage?.close() // Closes the previous page if it exists
            currentPage = renderer.openPage(pageIndex)

            // Gets the bitmap for the current page
            val originalBitmap = Bitmap.createBitmap(currentPage!!.width, currentPage!!.height, Bitmap.Config.ARGB_8888)
            currentPage?.render(originalBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Calculates the scaling factor to fit the width of the screen
            val scaleFactor = screenWidth.toFloat() / originalBitmap.width

            // Scales the bitmap
            val scaledBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                screenWidth,
                (originalBitmap.height * scaleFactor).toInt(),
                true
            )

            val imageView = ImageView(this)
            imageView.setImageBitmap(scaledBitmap)
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            linearLayout.addView(imageView) // Adds the image view to the layout

            // Hides progress bar after rendering
            progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close()
        pdfRenderer?.close()
    }
}
