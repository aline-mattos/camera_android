package com.sample.camera

import android.Manifest
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sample.camera.ImageFilters.setGreyFilter
import com.sample.camera.ImageFilters.setNegativeFilter
import com.sample.camera.ImageFilters.setSepiaFilter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView

    private lateinit var captureButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var closeButton: ImageButton

    private lateinit var filter1Button: ImageButton
    private lateinit var filter2Button: ImageButton
    private lateinit var filter3Button: ImageButton

    private lateinit var takenView: FrameLayout
    private lateinit var takenPhoto: ImageView

    private var imageCapture: ImageCapture? = null

    private var originalBitmap: Bitmap? = null
    private var filteredBitmap: Bitmap? = null

    companion object {
        private const val TAG = "TDECamera"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        requestPermissions()
    }

    //region Setup Methods
    private fun setupViews() {
        previewView = findViewById(R.id.preview)

        takenView = findViewById(R.id.taken_view)
        takenPhoto = findViewById(R.id.taken_photo)

        captureButton = findViewById(R.id.capture_button)
        saveButton = findViewById(R.id.save_button)
        closeButton = findViewById(R.id.close_button)
        filter1Button = findViewById(R.id.filter_1_button)
        filter2Button = findViewById(R.id.filter_2_button)
        filter3Button = findViewById(R.id.filter_3_button)

        setupButtons()
    }

    private fun setupButtons() {
        captureButton.setOnClickListener { takePhoto() }

        saveButton.setOnClickListener {
            if (filteredBitmap != null) {
                saveBitmapToPicturesDirectory(filteredBitmap!!)
            } else {
                Toast.makeText(this, "No bitmap found", Toast.LENGTH_LONG).show()
            }
        }

        closeButton.setOnClickListener {
            takenView.visibility = View.GONE
            takenPhoto.setImageURI(null)
            originalBitmap = null
        }

        filter1Button.setOnClickListener {
            if(originalBitmap != null) {
                filteredBitmap = setGreyFilter(originalBitmap!!)
                takenPhoto.setImageBitmap(filteredBitmap)
            }
        }

        filter2Button.setOnClickListener {
            if(originalBitmap != null) {
                filteredBitmap = setNegativeFilter(originalBitmap!!)
                takenPhoto.setImageBitmap(filteredBitmap)
            }
        }

        filter3Button.setOnClickListener {
            if(originalBitmap != null) {
                filteredBitmap = setSepiaFilter(originalBitmap!!)
                takenPhoto.setImageBitmap(filteredBitmap)
            }
        }
    }
    //endregion

    //region Camera methods
    private fun startCamera() {
        val processCameraProvider = ProcessCameraProvider.getInstance(this)

        processCameraProvider.addListener({
            val cameraProvider = processCameraProvider.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (e: Exception) {
                Log.d(TAG,"Error on starting the camera!")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/${TAG}")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    if (output.savedUri != null) {
                        val path = Utils.getPath(this@MainActivity, output.savedUri!!)
                        if (path != null) {
                            originalBitmap = rotateBitmapIfRequired(BitmapFactory.decodeFile(path), path)
                            takenView.visibility = View.VISIBLE
                            takenPhoto.setImageBitmap(originalBitmap)
                        }
                    }
                }
            }
        )
    }
    //endregion

    //region Permission methods
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var permissionGranted = true

        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value) permissionGranted = false
        }

        if (permissionGranted) {
            startCamera()
        } else Toast.makeText(baseContext, "Permission request denied", Toast.LENGTH_SHORT).show()
    }
    //endregion

    //region Support methods
    fun saveBitmapToPicturesDirectory(bitmap: Bitmap) {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val tdeCameraDir = File(imagesDir, "TDECamera")
        val fileName = "${UUID.randomUUID()}.jpg"

        // Create the directory if it doesn't exist
        if (!tdeCameraDir.exists()) {
            tdeCameraDir.mkdirs()
        }

        val file = File(tdeCameraDir, fileName)

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

            MediaStore.Images.Media.insertImage(contentResolver, file.absolutePath, fileName, null)

            Toast.makeText(this, "Image saved!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun rotateBitmapIfRequired(bitmap: Bitmap, path: String): Bitmap {
        val ei = ExifInterface(path)
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
    //endregion
}