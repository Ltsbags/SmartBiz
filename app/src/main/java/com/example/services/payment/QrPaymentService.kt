package com.example.services.payment.QrPaymentService

import android.graphics.Bitmap
import android.graphics.Color
import com.example.services.payment.models.PaymentEngineRequest
import java.net.URLEncoder

class QrPaymentService {

    fun generateUpiQrPayload(
        vpa: String,
        merchantName: String,
        amount: Double,
        currency: String = "INR",
        invoiceNumber: String = "",
        note: String = ""
    ): String {
        val encodedName = URLEncoder.encode(merchantName.ifBlank { "SmartBiz Store" }, "UTF-8")
        val txnNote = URLEncoder.encode(if (invoiceNumber.isNotBlank()) "Invoice #$invoiceNumber" else note.ifBlank { "Store Payment" }, "UTF-8")
        val txnRef = "QR" + System.currentTimeMillis()
        return "upi://pay?pa=$vpa&pn=$encodedName&am=$amount&cu=$currency&tr=$txnRef&tn=$txnNote"
    }

    fun generateEmvCoQrPayload(
        merchantName: String,
        merchantCity: String,
        amount: Double,
        currencyCode: String = "356" // 356 for INR
    ): String {
        // Standard EMVCo QR String payload structure format for static/dynamic merchant presentation
        val payload = StringBuilder()
        payload.append("000201") // Payload Format Indicator
        payload.append("010212") // Dynamic QR
        payload.append("52045999") // Merchant Category Code
        payload.append("5303$currencyCode") // Currency
        val amountStr = String.format("%.2f", amount)
        payload.append(String.format("54%02d%s", amountStr.length, amountStr)) // Amount
        payload.append("5802IN") // Country Code
        payload.append(String.format("59%02d%s", merchantName.take(25).length, merchantName.take(25))) // Merchant Name
        payload.append(String.format("60%02d%s", merchantCity.take(15).length, merchantCity.take(15))) // Merchant City
        payload.append("6304ABCD") // CRC Placeholder
        return payload.toString()
    }

    /**
     * Helper to convert string payload into boolean matrix for QR visualization in UI
     */
    fun generateQrMatrix(payload: String, size: Int = 21): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) }
        val hash = payload.hashCode()

        // Finder patterns in 3 corners (7x7 top-left, top-right, bottom-left)
        fun drawFinder(top: Int, left: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    matrix[top + r][left + c] = isBorder || isCenter
                }
            }
        }

        drawFinder(0, 0)
        drawFinder(0, size - 7)
        drawFinder(size - 7, 0)

        // Seed data area deterministically from payload hash
        var seed = hash
        for (r in 0 until size) {
            for (c in 0 until size) {
                // Skip finder patterns
                if ((r in 0..7 && c in 0..7) || (r in 0..7 && c >= size - 8) || (r >= size - 8 && c in 0..7)) {
                    continue
                }
                seed = seed * 31 + r * size + c
                matrix[r][c] = (seed % 2 == 0)
            }
        }
        return matrix
    }

    fun generateQrBitmap(payload: String, width: Int = 300, height: Int = 300): Bitmap {
        val size = 25
        val matrix = generateQrMatrix(payload, size)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val cellW = width / size
        val cellH = height / size

        for (y in 0 until height) {
            for (x in 0 until width) {
                val gridY = (y / cellH).coerceAtMost(size - 1)
                val gridX = (x / cellW).coerceAtMost(size - 1)
                val color = if (matrix[gridY][gridX]) Color.BLACK else Color.WHITE
                bitmap.setPixel(x, y, color)
            }
        }
        return bitmap
    }
}
