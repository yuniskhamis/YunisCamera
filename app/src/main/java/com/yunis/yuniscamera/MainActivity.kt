package com.yunis.yuniscamera
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var imageView: ImageView
    private lateinit var takePhoto : Button
    private lateinit var contentDesc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        takePhoto = findViewById(R.id.photo_button)
        contentDesc = findViewById(R.id.labels)

        takePhoto.setOnClickListener {
            dispatchTakePictureIntent()
        }

    }
    //to receive the result from the other application
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            val extras = data?.extras
            val imageBitmap = extras!!["data"] as Bitmap? //get bitmap from extras
            imageView.setImageBitmap(imageBitmap) //display the image
            if (imageBitmap != null) {
                runObjectDetection(imageBitmap)
            }
        }
    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }


    /**
     * ML Kit Object Detection Function
     */
    private fun runObjectDetection(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
// Step 2: acquire detector object
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        val objectDetector = ObjectDetection.getClient(options)
        // Step 3: feed given image to detector and setup callback
        objectDetector.process(image)
            .addOnSuccessListener {
                // Task completed successfully
                debugPrint(it)
            }
            .addOnFailureListener {
                // Task failed with an exception
                Log.e(TAG, it.message.toString())
            }
    }
    private fun debugPrint(detectedObjects: List<DetectedObject>) {
        var outputText = " "

        detectedObjects.forEachIndexed { index, detectedObject ->
            val box = detectedObject.boundingBox
            Log.d(TAG, "Detected object: $index")
            Log.d(TAG, " trackingId: ${detectedObject.trackingId}")
            Log.d(TAG, " boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            detectedObject.labels.forEach {

                val text = it.text
                val confidence = (it.confidence*100).roundToInt().toString()

                outputText += "$text : $confidence %\n"
                contentDesc.text = outputText
            }
        }
    }
}

