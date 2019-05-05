package com.jabezmagomere.visualdiabetes


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jabezmagomere.visualdiabetes.ModelConfig.Companion.INPUT_IMG_SIZE_HEIGHT
import com.jabezmagomere.visualdiabetes.ModelConfig.Companion.intValues
import kotlinx.android.synthetic.main.fragment_scan.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel





// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class FragmentScan : Fragment() {
    companion object {
        const val PICK_PHOTO_FOR_SCAN=0
    }
    private lateinit var tflite : Interpreter
    private val imgData : ByteBuffer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        try{
            tflite = Interpreter(loadModelFile(context))
        }catch (ex:Exception){

        }
        buttonSelect.setOnClickListener {
            pickImage()
        }

    }

    @Throws(Exception::class)
    private fun loadModelFile(context: Context?): MappedByteBuffer {
        val fileDescriptor = context?.assets?.openFd(ModelConfig.MODEL_FILENAME)
        val inputStream = FileInputStream(fileDescriptor?.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor!!.startOffset
        val declaredLength = fileDescriptor!!.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength)

    }

    private fun pickImage(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type= "image/*"
        startActivityForResult(intent, PICK_PHOTO_FOR_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_PHOTO_FOR_SCAN && resultCode == Activity.RESULT_OK){
            if(data==null){
                Toast.makeText(context,"No Image Selected",Toast.LENGTH_SHORT).show()
                return
            }
            try {
                val uri = data?.data
                if(uri!=null) {
                    val imageBitmap = uriToBitmap(uri)
                    imageView3.setImageBitmap(imageBitmap)
                    buttonClassify.apply {
                        isEnabled = true
                        setOnClickListener {
                            classify(imageBitmap)
                        }
                    }
                }

            }catch (ex:Exception){
                ex.printStackTrace()
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(this.context?.contentResolver, uri)
    }

    private fun classify(bitmap: Bitmap){
        val buffer = ModelConfig.convertBitmapToByteBuffer(bitmap)
        val result = arrayOf<Array<Float>>()
        tflite.run(buffer,result)
        Log.d("Classifications",result.toString())
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

}
