package spirit.bangkit.kusaku

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import spirit.bangkit.kusaku.databinding.ActivityMainBinding
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val ORIENTATIONS = SparseIntArray()

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 0)
        ORIENTATIONS.append(Surface.ROTATION_90, 90)
        ORIENTATIONS.append(Surface.ROTATION_180, 180)
        ORIENTATIONS.append(Surface.ROTATION_270, 270)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 7), DataType.FLOAT32)

        var tflite : Interpreter? = null
        try{
            val tfliteModel
            = FileUtil.loadMappedFile(this,
                "face_model_two.tflite");
            tflite = Interpreter(tfliteModel)
        } catch (e : IOException){
            Log.e("tfliteSupport", "Error reading model", e);
        }

        val tImage = loadImage(R.drawable.tiga)
        tflite?.run(tImage.buffer, probabilityBuffer.buffer)

        val axisLabels = try {
            FileUtil.loadLabels(this, "face_label.txt")
        } catch (e: IOException) {
            null
        }

        val probProcessor = TensorProcessor.Builder().add(NormalizeOp(0f, 7f)).build()
        if (!axisLabels.isNullOrEmpty()) {
            val labels = TensorLabel(axisLabels, probProcessor.process(probabilityBuffer))

            val floatMap = labels.mapWithFloatValue
            Log.d("Hasil", floatMap.toString())
        }

        Log.d("TFLite Result", probabilityBuffer.floatArray.toString())
    }

    private fun loadImage(id: Int): TensorImage {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(48, 48, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .build()
        var tImage = TensorImage(DataType.FLOAT32)

        val imageDrawable = getDrawable(id)
        val bitmap = (imageDrawable as BitmapDrawable).bitmap
        tImage.load(bitmap)
        tImage = imageProcessor.process(tImage)
        return tImage
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