package com.sample.camera

import android.graphics.Bitmap
import android.graphics.Color


object ImageFilters {

    fun setGreyFilter(oldBitmap: Bitmap): Bitmap {
        // Copying to newBitmap for manipulation
        val newBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Height and width of Image
        val imageHeight = newBitmap.height
        val imageWidth = newBitmap.width

        // Array to hold all pixels
        val pixels = IntArray(imageWidth * imageHeight)
        newBitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        for (i in pixels.indices) {
            val oldPixel = pixels[i]

            val oldRed = Color.red(oldPixel)
            val oldBlue = Color.blue(oldPixel)
            val oldGreen = Color.green(oldPixel)
            val oldAlpha = Color.alpha(oldPixel)

            // Calculate intensity
            val intensity = (oldRed + oldBlue + oldGreen) / 3

            // Apply new pixel values
            pixels[i] = Color.argb(oldAlpha, intensity, intensity, intensity)
        }

        newBitmap.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        return newBitmap
    }

    fun setNegativeFilter(oldBitmap: Bitmap): Bitmap {
        // Copying to newBitmap for manipulation
        val newBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Height and width of Image
        val imageHeight = newBitmap.height
        val imageWidth = newBitmap.width

        // Array to hold all pixels
        val pixels = IntArray(imageWidth * imageHeight)
        newBitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        for (i in pixels.indices) {
            val oldPixel = pixels[i]

            val oldRed = Color.red(oldPixel)
            val oldBlue = Color.blue(oldPixel)
            val oldGreen = Color.green(oldPixel)
            val oldAlpha = Color.alpha(oldPixel)

            val newRed = 255 - oldRed
            val newGreen = 255 - oldGreen
            val newBlue = 255 - oldBlue

            pixels[i] = Color.argb(oldAlpha, newRed, newGreen, newBlue)
        }

        newBitmap.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        return newBitmap
    }

    fun setSepiaFilter(oldBitmap: Bitmap): Bitmap {
        // Copying to newBitmap for manipulation
        val newBitmap = oldBitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Height and width of Image
        val imageHeight = newBitmap.height
        val imageWidth = newBitmap.width

        // Array to hold all pixels
        val pixels = IntArray(imageWidth * imageHeight)
        newBitmap.getPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        for (i in pixels.indices) {
            val oldPixel = pixels[i]

            val oldRed = Color.red(oldPixel)
            val oldBlue = Color.blue(oldPixel)
            val oldGreen = Color.green(oldPixel)
            val oldAlpha = Color.alpha(oldPixel)

            // Calculate new sepia values
            val newRed = (0.393 * oldRed + 0.769 * oldGreen + 0.189 * oldBlue).toInt().coerceAtMost(255)
            val newGreen = (0.349 * oldRed + 0.686 * oldGreen + 0.168 * oldBlue).toInt().coerceAtMost(255)
            val newBlue = (0.272 * oldRed + 0.534 * oldGreen + 0.131 * oldBlue).toInt().coerceAtMost(255)

            // Apply new pixel values
            pixels[i] = Color.argb(oldAlpha, newRed, newGreen, newBlue)
        }

        newBitmap.setPixels(pixels, 0, imageWidth, 0, 0, imageWidth, imageHeight)

        return newBitmap
    }
}
