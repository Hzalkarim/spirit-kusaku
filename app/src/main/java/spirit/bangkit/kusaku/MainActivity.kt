package spirit.bangkit.kusaku

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import spirit.bangkit.kusaku.databinding.ActivityMainBinding
import spirit.bangkit.kusaku.tensorflow.TensorFlowModel
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }

    private val IMAGE_SQUARE_SIZE = 100

    private val imagesResource = listOf<Int>(
        R.drawable.satu,
        R.drawable.dua,
        R.drawable.tiga,
        R.drawable.empat,
        R.drawable.lima,
        R.drawable.enam,
        R.drawable.tujuh,
        R.drawable.lapan,
    )

    private var imgIndex = 0

    enum class ScrollDirection(i: Int) {LEFT(-1), RIGHT(1)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load(R.drawable.satu)
            .into(binding.imgFace)

        with(binding){
            btnDetect.setOnClickListener(this@MainActivity)
            btnLeft.setOnClickListener(this@MainActivity)
            btnRight.setOnClickListener(this@MainActivity)
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btn_detect -> {
                val bitmap = (binding.imgFace.drawable as BitmapDrawable).bitmap
                val image = InputImage.fromBitmap(bitmap, 0)

                val detector = FaceDetection.getClient()

                binding.btnDetect.isClickable = false
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        //val drawingView = DrawRect(applicationContext, faces)
                        //drawingView.draw(Canvas(bitmap))

                        with (faces[0].boundingBox){
                            val newBitmap = try{
                                Bitmap.createBitmap(bitmap, centerX() - width()/2 , centerY() - height()/2, width(), height())
                            } catch (e: IllegalArgumentException) {
                                binding.tvPredict.text = "Face Error"
                                null
                            }
                            TensorPredict(newBitmap!!)
                            runOnUiThread { binding.imgFace.setImageBitmap(newBitmap) }
                        }
                    }
            }

            R.id.btn_left -> {
                scrollImage(ScrollDirection.LEFT)
                binding.btnDetect.isClickable = true
                binding.tvPredict.text = "NoN"
            }
            R.id.btn_right -> {
                scrollImage(ScrollDirection.RIGHT)
                binding.btnDetect.isClickable = true
                binding.tvPredict.text = "NoN"
            }
        }
    }

    private fun scrollImage(dir: ScrollDirection) {
        when (dir) {
            ScrollDirection.LEFT -> {
                if (imgIndex - 1 < 0) {
                    imgIndex = imagesResource.lastIndex
                    setImage(imagesResource[imgIndex])
                } else {
                    setImage(imagesResource[--imgIndex])
                }
            }
            ScrollDirection.RIGHT -> {
                if (imgIndex + 1 > imagesResource.lastIndex) {
                    setImage(imagesResource[0])
                    imgIndex = 0
                } else {
                    setImage(imagesResource[++imgIndex])
                }
            }
        }
    }

    private fun setImage(id: Int) {
        Glide.with(this)
            .load(id)
            .into(binding.imgFace)
    }

    private fun TensorPredict(bitmap: Bitmap) {
        val tensor = TensorFlowModel(this)

        val tImage = tensor.loadImage(bitmap)
        val labels = tensor.processImageToLabel(tImage)

        val floatMap = labels?.mapWithFloatValue
        val result = floatMap?.maxByOrNull { it.value }?.key
        Log.d("Hasil", floatMap.toString())
        runOnUiThread { binding.tvPredict.text = result }
    }

    private fun predictFaceBitmap() {
        val imageDrawable = getDrawable(R.drawable.picture2)
        val bitmap = (imageDrawable as BitmapDrawable).bitmap
        /*val stream = ByteArrayOutputStream()

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        val image = InputImage.fromByteArray(
            byteArray,
            *//* image width *//* 224,
                *//* image height *//* 224,
                ORIENTATIONS[0],
                InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
            )*/

        val image = InputImage.fromBitmap(bitmap, 0)

        val localModel = LocalModel.Builder()
            .setAssetFilePath("face_model.tflite")
            .build()
        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .setMaxResultCount(5)
            .build()
        val labeler = ImageLabeling.getClient(customImageLabelerOptions)

        labeler.process(image)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    Log.d("Success", "${label.index} - ${label.text} : ${label.confidence}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error process", if (e.message.isNullOrEmpty()) "Hehe" else e.message!!)
            }
    }

    private fun predictFace() {
        val localModel = LocalModel.Builder()
            .setAssetFilePath("object_labeler.tflite")
            .build()
        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .setMaxResultCount(5)
            .build()
        val labeler = ImageLabeling.getClient(customImageLabelerOptions)

        var image: InputImage? = null
        try {
            val uriString = "android.resource://spirit.bangkit.kusaku/" + R.drawable.picture3
            image = InputImage.fromFilePath(this, Uri.parse(uriString))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        labeler.process(image!!)
            .addOnSuccessListener { labels ->
                for (label in labels) {
                    Log.d("Success", "${label.index} - ${label.text} : ${label.confidence}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Error process", if (e.message.isNullOrEmpty()) "Hehe" else e.message!!)
            }
    }
}