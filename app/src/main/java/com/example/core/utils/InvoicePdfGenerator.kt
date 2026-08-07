package com.example.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.core.constants.AppConstants
import com.example.core.database.entity.InvoiceWithItems
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvoicePdfGenerator {

    fun generatePdf(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        businessName: String = AppConstants.DEFAULT_BUSINESS_NAME,
        businessTagline: String = AppConstants.DEFAULT_BUSINESS_TAGLINE,
        currencySymbol: String = AppConstants.DEFAULT_CURRENCY
    ): File? {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size at 72 DPI
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()

        var y = 40

        // 1. Header Banner
        paint.color = Color.parseColor("#1976D2")
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Business Name
        titlePaint.color = Color.WHITE
        titlePaint.textSize = 22f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(businessName, 30f, 42f, titlePaint)

        // Subtitle / Tagline
        titlePaint.textSize = 10f
        titlePaint.typeface = Typeface.DEFAULT
        canvas.drawText(businessTagline, 30f, 58f, titlePaint)

        // "TAX INVOICE" label on right
        titlePaint.textSize = 18f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TAX INVOICE", 565f, 48f, titlePaint)
        titlePaint.textAlign = Paint.Align.LEFT

        y = 105

        // 2. Invoice Meta Info
        paint.color = Color.BLACK
        paint.textSize = 10f
        val boldPaint = Paint(paint).apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }

        canvas.drawText("Invoice No:", 30f, y.toFloat(), boldPaint)
        canvas.drawText(invoice.invoiceNumber, 100f, y.toFloat(), paint)

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        canvas.drawText("Date:", 30f, (y + 16).toFloat(), boldPaint)
        canvas.drawText(dateFormat.format(Date(invoice.date)), 100f, (y + 16).toFloat(), paint)

        canvas.drawText("Status:", 30f, (y + 32).toFloat(), boldPaint)
        canvas.drawText("${invoice.status} (${invoice.paymentStatus})", 100f, (y + 32).toFloat(), paint)

        // Customer Info (Right Side)
        canvas.drawText("Billed To:", 350f, y.toFloat(), boldPaint)
        canvas.drawText(invoice.customerName.ifEmpty { "Walk-in Customer" }, 350f, (y + 16).toFloat(), boldPaint)
        if (invoice.customerPhone.isNotEmpty()) {
            canvas.drawText("Ph: ${invoice.customerPhone}", 350f, (y + 32).toFloat(), paint)
        }
        if (invoice.customerGst.isNotEmpty()) {
            canvas.drawText("GSTIN: ${invoice.customerGst}", 350f, (y + 48).toFloat(), paint)
        }
        if (invoice.billingAddress.isNotEmpty()) {
            canvas.drawText(invoice.billingAddress, 350f, (y + 64).toFloat(), paint)
        }

        y += 90

        // 3. Table Header
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawRect(30f, y.toFloat(), 565f, (y + 24).toFloat(), paint)

        boldPaint.color = Color.BLACK
        boldPaint.textSize = 10f
        canvas.drawText("#", 40f, (y + 16).toFloat(), boldPaint)
        canvas.drawText("Item / Description", 70f, (y + 16).toFloat(), boldPaint)
        canvas.drawText("Qty", 320f, (y + 16).toFloat(), boldPaint)
        canvas.drawText("Price", 380f, (y + 16).toFloat(), boldPaint)
        canvas.drawText("GST", 450f, (y + 16).toFloat(), boldPaint)
        canvas.drawText("Total", 500f, (y + 16).toFloat(), boldPaint)

        y += 24

        // 4. Table Items
        val numberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = java.util.Currency.getInstance("USD")
        }

        paint.color = Color.BLACK
        paint.textSize = 9.5f

        items.forEachIndexed { index, item ->
            y += 20
            canvas.drawText("${index + 1}", 40f, y.toFloat(), paint)

            val name = if (item.productName.length > 35) item.productName.take(32) + "..." else item.productName
            canvas.drawText(name, 70f, y.toFloat(), paint)

            val qtyStr = if (item.quantity % 1.0 == 0.0) "${item.quantity.toInt()} ${item.unit}" else "${item.quantity} ${item.unit}"
            canvas.drawText(qtyStr, 320f, y.toFloat(), paint)

            canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", item.sellingPrice)}", 380f, y.toFloat(), paint)
            canvas.drawText("${item.gstPercentage.toInt()}%", 450f, y.toFloat(), paint)
            canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", item.lineTotal)}", 500f, y.toFloat(), paint)

            // Divider Line
            paint.color = Color.parseColor("#EEEEEE")
            canvas.drawLine(30f, (y + 6).toFloat(), 565f, (y + 6).toFloat(), paint)
            paint.color = Color.BLACK
        }

        y += 30

        // 5. Totals Section (Right Aligned)
        val rightXLabel = 380f
        val rightXVal = 500f

        canvas.drawText("Subtotal:", rightXLabel, y.toFloat(), paint)
        canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", invoice.subtotal)}", rightXVal, y.toFloat(), paint)

        if (invoice.discountAmount > 0) {
            y += 18
            canvas.drawText("Discount:", rightXLabel, y.toFloat(), paint)
            canvas.drawText("-$currencySymbol${String.format(Locale.US, "%.2f", invoice.discountAmount)}", rightXVal, y.toFloat(), paint)
        }

        y += 18
        canvas.drawText("Tax / GST:", rightXLabel, y.toFloat(), paint)
        canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", invoice.taxAmount)}", rightXVal, y.toFloat(), paint)

        y += 22
        // Highlight Grand Total
        paint.color = Color.parseColor("#1976D2")
        canvas.drawRect(360f, (y - 14).toFloat(), 565f, (y + 10).toFloat(), paint)

        boldPaint.color = Color.WHITE
        boldPaint.textSize = 12f
        canvas.drawText("Grand Total:", 370f, y.toFloat(), boldPaint)
        canvas.drawText("$currencySymbol${String.format(Locale.US, "%.2f", invoice.totalAmount)}", rightXVal, y.toFloat(), boldPaint)

        y += 40

        // 6. Notes & Terms
        paint.color = Color.BLACK
        paint.textSize = 9f
        if (invoice.notes.isNotEmpty()) {
            canvas.drawText("Notes: ${invoice.notes}", 30f, y.toFloat(), paint)
            y += 16
        }
        if (invoice.terms.isNotEmpty()) {
            canvas.drawText("Terms & Conditions: ${invoice.terms}", 30f, y.toFloat(), paint)
        }

        // Footer
        boldPaint.color = Color.GRAY
        boldPaint.textSize = 8.5f
        canvas.drawText("Generated by BillNova • Smart Billing. Smarter Business.", 150f, 810f, boldPaint)

        pdfDocument.finishPage(page)

        // Save PDF to file
        return try {
            val pdfDir = File(context.cacheDir, "invoices")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val file = File(pdfDir, "${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${file.nameWithoutExtension}")
            putExtra(Intent.EXTRA_TEXT, "Please find attached invoice ${file.nameWithoutExtension}.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Invoice PDF via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun printPdf(context: Context, file: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service not available", Toast.LENGTH_SHORT).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = android.print.PrintDocumentInfo.Builder("${file.nameWithoutExtension}.pdf")
                    .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out android.print.PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    val input = FileInputStream(file)
                    val output = FileOutputStream(destination?.fileDescriptor)
                    input.copyTo(output)
                    input.close()
                    output.close()
                    callback?.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }

        printManager.print("Invoice_${file.nameWithoutExtension}", printAdapter, PrintAttributes.Builder().build())
    }
}
