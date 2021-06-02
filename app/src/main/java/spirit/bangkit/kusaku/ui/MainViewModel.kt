package spirit.bangkit.kusaku.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import org.tensorflow.lite.support.image.TensorImage
import spirit.bangkit.kusaku.machinelearning.FaceOnExam
import spirit.bangkit.kusaku.machinelearning.MlModel
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(mlmodel: MlModel) : ViewModel() {

    private var _data = MutableLiveData<List<String?>>()
    val data : LiveData<List<String?>> get() = _data

    private val detector = FaceDetection.getClient()

    private val faceExpressions = ArrayList<String?>()
    private val mlModel = mlmodel as FaceOnExam

    fun processFaceImage(bitmap: Bitmap) : String? {
        val image = if (bitmap.config == Bitmap.Config.ARGB_8888)
            bitmap
        else
            bitmap.copy(Bitmap.Config.ARGB_8888, false)

        val tImage = TensorImage.fromBitmap(image)
        val outputs = mlModel.process(tImage)
        val result = outputs.probabilityAsCategoryList.maxByOrNull { c -> c.score }

        return result?.label
    }

    fun detectFaces(bitmap: Bitmap, updateAfterFinish: Boolean = false) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        detector.process(inputImage).addOnSuccessListener { faces ->
            for (face in faces) {
                faceExpressions.add(
                    processFaceImage(bitmap.cropFace(face)))
            }

            if (updateAfterFinish)
                updateFaces()
        }
    }

    fun updateFaces() {
        _data.postValue(faceExpressions)
    }

    private fun Bitmap.cropFace(face: Face) : Bitmap {
        with (face.boundingBox) {
            val x = centerX() - width()/2
            val y = centerY() - height()/2
            return Bitmap.createBitmap(this@cropFace, if (x + width() > width()) width() else x ,
                if (y + height() > height()) height() else y, width(), height())
        }
    }

    override fun onCleared() {
        super.onCleared()
        mlModel.close()
        detector.close()
    }
}