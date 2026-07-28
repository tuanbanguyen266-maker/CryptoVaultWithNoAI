package com.example.cryptovaultwithnoai.code.security

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

//module to do for generate for qrcode
object QrCodeGenerator {
    //generate QRcode with content: URI setup Totp and size 512x512 bitmap
    fun generateQrCode(content: String, size: Int = 512): Bitmap {
        //create a QRWriter
        val writer = QRCodeWriter()

        //encode the string content to BitMatrix include yes/no value corresponding to
        //white and black color according to QRcode with size
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        //take the width
        val width = bitMatrix.width
        //take the height
        val height = bitMatrix.height

        //create a empty Bitmap image with width and height to avoid issue
        //Bitmap.Config.RGB_565: memory-efficient pixel conf is suitable for white/black QR image
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        //loop through the matrix to set the pixel colors
        for (x in 0 until width) {
            for (y in 0 until height) {
                //if true --> Color.Black else Color.White
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }
}