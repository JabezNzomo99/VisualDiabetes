package com.jabezmagomere.visualdiabetes

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.Fragment
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Classifier(private val context: Context){
    companion object{
        private val MAX_RESULTS = 3
        private val DIM_BATCH_SIZE = 1
        private val DIM_PIXEL_SIZE = 3
        private val IMAGE_MEAN = 128
        private val IMAGE_STD = 128.0f
    }

    /** Preallocated buffers for storing image data in.  */
    private val intValues = IntArray(ModelConfig.INPUT_IMG_SIZE_HEIGHT * ModelConfig.INPUT_IMG_SIZE_WIDTH)

    /** Options for configuring the Interpreter.  */
    private val tfliteOptions = Interpreter.Options()

    /** The loaded TensorFlow Lite model.  */
    private var tfliteModel: MappedByteBuffer? = null

    /** Labels corresponding to the output of the vision model.  */
    private val labels = listOf<String>("0","1","2","3","4")

    /** An instance of the driver class to run model inference with Tensorflow Lite.  */
    protected var tflite: Interpreter? = null

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs.  */
    protected var imgData: ByteBuffer? = null

    private var labelProbArray: Array<FloatArray>? = null

    init {
        tfliteModel = loadModelFile(context)
        tflite = Interpreter(tfliteModel!!)
        imgData = ByteBuffer.allocate(
            DIM_BATCH_SIZE
                    *ModelConfig.INPUT_IMG_SIZE_WIDTH
                    *ModelConfig.INPUT_IMG_SIZE_HEIGHT
                    * DIM_PIXEL_SIZE
                    *getNumBytesPerChannel()
        )
        imgData!!.order(ByteOrder.nativeOrder())
        Log.d("Message","Created a Tensorflow Lite Image Classifier.")
        labelProbArray = Array(1) { FloatArray(labels.size) }
    }


    /** Memory-map the model file in Assets.  */
    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(ModelConfig.MODEL_FILENAME)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

   fun getNumBytesPerChannel():Int = 4

    fun convertBitmapToByteBuffer(bitmap: Bitmap){
        if(imgData == null){
            return
        }
        imgData!!.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        // Convert the image to floating point.
        var pixel = 0
        for (i in 0 until ModelConfig.INPUT_IMG_SIZE_HEIGHT) {
            for (j in 0 until ModelConfig.INPUT_IMG_SIZE_WIDTH) {
                val x = intValues[pixel++]
                addPixelValue(x)
            }
        }
    }

    private fun addPixelValue(x: Int) {
        imgData?.putFloat(((x shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        imgData?.putFloat(((x shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
        imgData?.putFloat(((x and 0xFF) - IMAGE_MEAN) / IMAGE_STD)

    }

    fun recognizeImage(bitmap:Bitmap):Array<FloatArray>{
        convertBitmapToByteBuffer(bitmap)
        tflite?.run(imgData,labelProbArray)
        labelProbArray?.forEach {array->
            array.forEach {
                Log.d("Value",it.toString())
            }
        }
        return labelProbArray!!
    }



}