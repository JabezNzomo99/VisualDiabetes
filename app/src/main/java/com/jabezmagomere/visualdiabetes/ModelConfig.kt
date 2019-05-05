package com.jabezmagomere.visualdiabetes

import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import java.nio.ByteOrder.nativeOrder
import android.R.attr.order
import java.nio.ByteBuffer.allocateDirect
import android.graphics.Bitmap
import java.nio.ByteBuffer
import java.nio.ByteOrder


class ModelConfig{
    companion object {
        val MODEL_FILENAME = "converted_model.tflite"
        val OUTPUT_LABELS = mutableListOf<Int>(1, 2, 3, 4)
        val INPUT_IMG_SIZE_WIDTH = 128
        val INPUT_IMG_SIZE_HEIGHT = 128
        val FLOAT_TYPE_SIZE = 4
        val PIXEL_SIZE = 1
        val MODEL_INPUT_SIZE = FLOAT_TYPE_SIZE * INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT * PIXEL_SIZE
        /** Preallocated buffers for storing image data in.  */
        val intValues = IntArray(INPUT_IMG_SIZE_HEIGHT * INPUT_IMG_SIZE_WIDTH)


        public fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
            val byteBuffer = allocateDirect(MODEL_INPUT_SIZE)
            byteBuffer.order(nativeOrder())
            val pixels = IntArray(INPUT_IMG_SIZE_WIDTH * INPUT_IMG_SIZE_HEIGHT)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            for (pixel in pixels) {
                val rChannel = (pixel shr 16 and 0xFF).toFloat()
                val gChannel = (pixel shr 8 and 0xFF).toFloat()
                val bChannel = (pixel and 0xFF).toFloat()
                val pixelValue = (rChannel + gChannel + bChannel) / 3f / 255f
                byteBuffer.putFloat(pixelValue)
            }
            return byteBuffer
        }
    }



}