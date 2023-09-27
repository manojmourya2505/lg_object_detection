package com.lg.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.lg.ml.SsdMobilenetV11Metadata1
import com.lg.R
import com.lg.permission.AppPermissions
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {

    private val _cameraPermissionRequestCode = 101

    private lateinit var _captureTexture: TextureView
    private lateinit var _cameraManager: CameraManager
    private lateinit var _cameraDevice: CameraDevice

    private lateinit var _cameraHandler: Handler

    private lateinit var _pictureBitMap: Bitmap

    private lateinit var _capturedModel: SsdMobilenetV11Metadata1

    private var _drawPaint = Paint()

    private lateinit var _displayImage: ImageView

    private lateinit var labels : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_secondary)


        val appPermissions = AppPermissions(this)

        labels = FileUtil.loadLabels(this, "labels.txt")

        _captureTexture = findViewById(R.id.tv_capture_texture)
        _displayImage = findViewById(R.id.iv_display_image)

        //instance for camera manager
        _cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        _capturedModel = SsdMobilenetV11Metadata1.newInstance(this)

        //capture image handler instance
        val handlerThread = HandlerThread("cameraCapture")
        handlerThread.start()
        _cameraHandler = Handler(handlerThread.looper)

        //check if permissions are already granted
        if (appPermissions.checkCameraPermission()) {

            _captureTexture.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                    //open camera
                    openCamera()
                }

                override fun onSurfaceTextureSizeChanged(
                    surface: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {

                }

                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                    _pictureBitMap = _captureTexture.bitmap!!


                    // Creates inputs for reference.
                    val image = TensorImage.fromBitmap(_pictureBitMap)

                    // Runs model inference and gets result.
                    val outputs = _capturedModel.process(image)
                    val locations = outputs.locationsAsTensorBuffer.floatArray
                    val classes = outputs.classesAsTensorBuffer.floatArray
                    val scores = outputs.scoresAsTensorBuffer.floatArray
                    val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

                    val canvasMutable = _pictureBitMap.copy(Bitmap.Config.ARGB_8888, true)
                    val drawCanvas = Canvas(canvasMutable)

                    val markerHeight = canvasMutable.height
                    val markerWidth = canvasMutable.width

                    _drawPaint.textSize = markerHeight / 15f
                    _drawPaint.strokeWidth = markerHeight / 85f

                    var x = 0


                    scores.forEachIndexed { index, fl ->

                        x = index
                        x *= 4
                        if (fl > 0.5) {
                            _drawPaint.color = resources.getColor(R.color.color_primary)
                            _drawPaint.style = Paint.Style.STROKE
                            drawCanvas.drawRect(RectF(locations.get(x+1)*markerWidth, locations.get(x)*markerHeight, locations.get(x+3)*markerWidth, locations.get(x+2)*markerHeight), _drawPaint)
                            _drawPaint.style = Paint.Style.FILL
                            drawCanvas.drawText(labels[classes[index].toInt()] +" "+fl.toString(),
                                locations[x+1] *markerWidth, locations[x] *markerHeight,_drawPaint)
                        }
                    }
                    _displayImage.setImageBitmap(canvasMutable)
                }

            }

        } else
        //request for camera permissions
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                _cameraPermissionRequestCode
            )

    }

    override fun onDestroy() {
        super.onDestroy()
        // Releases model resources if no longer used.
        _capturedModel.close()

    }

    //opening camera method
    @SuppressLint("MissingPermission")
    private fun openCamera() {

        _cameraManager.openCamera(
            _cameraManager.cameraIdList[0],
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    _cameraDevice = camera

                    val surfaceTexture = _captureTexture.surfaceTexture
                    val surface = Surface(surfaceTexture)

                    val captureRequest =
                        _cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequest.addTarget(surface)

                    _cameraDevice.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                session.setRepeatingRequest(captureRequest.build(), null, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                            }

                        },
                        _cameraHandler
                    )

                }

                override fun onDisconnected(camera: CameraDevice) {
                }

                override fun onError(camera: CameraDevice, error: Int) {
                }

            },
            null
        )
    }

    //check camera permissions
    private fun checkCameraPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }


    //once requested permission, any action performed this method will get triggered
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {

            //if permissions are not granted call check camera permission function
            checkCameraPermission()
        }
    }
}