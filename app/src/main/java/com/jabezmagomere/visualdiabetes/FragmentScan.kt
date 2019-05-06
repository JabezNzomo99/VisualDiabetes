package com.jabezmagomere.visualdiabetes


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_scan.*
import java.lang.Exception


/**
 * A simple [Fragment] subclass.
 *
 */
class FragmentScan : Fragment() {
    private lateinit var classifier: Classifier
    companion object {
        const val PICK_PHOTO_FOR_SCAN=0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        classifier  = Classifier(context!!)
        buttonSelect.setOnClickListener {
            pickImage()
        }

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
                    Log.d("Image Bitmap",imageBitmap.toString())
                    imageView3.setImageBitmap(imageBitmap)
                    buttonClassify.apply {
                        isEnabled = true
                        setOnClickListener {
                            val results = classifier.recognizeImage(getResizedBitmap(imageBitmap,ModelConfig.INPUT_IMG_SIZE_WIDTH,ModelConfig.INPUT_IMG_SIZE_HEIGHT))
                            cardView.visibility = View.VISIBLE
                            var count = 0
                            results.forEach { array->
                                array.forEach {
                                    when(count){
                                        0->{
                                            textView4.text = it.toString()
                                        }
                                        1->{
                                            textView6.text = it.toString()
                                        }
                                        2->{
                                            textView8.text = it.toString()
                                        }
                                        3->{
                                            textView10.text = it.toString()
                                        }
                                        4->{
                                            textView12.text = it.toString()
                                        }
                                        else->{

                                        }
                                    }
                                    count++

                                }
                            }
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
