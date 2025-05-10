package com.example.stef

import android.graphics.Bitmap
import android.graphics.Color

object SteganographyUtil {

    fun encodeMessage(original: Bitmap, message: String): Bitmap {
        val encoded = original.copy(Bitmap.Config.ARGB_8888, true)
        val binaryMsg = messageToBinary(message) + "1111111111111110" // EOF marker
        var charIndex = 0

        loop@ for (y in 0 until encoded.height) {
            for (x in 0 until encoded.width) {
                if (charIndex >= binaryMsg.length) break@loop

                val pixel = encoded.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val newB = (b and 0xFE) or (binaryMsg[charIndex].digitToInt())
                encoded.setPixel(x, y, Color.rgb(r, g, newB))

                charIndex++
            }
        }
        return encoded
    }

    private fun messageToBinary(msg: String): String =
        msg.map { it.code.toString(2).padStart(8, '0') }.joinToString("")
}
