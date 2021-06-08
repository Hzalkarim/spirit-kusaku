package spirit.bangkit.kusaku.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import spirit.bangkit.kusaku.machinelearning.FaceOnExam
import spirit.bangkit.kusaku.machinelearning.MlModel
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(application: Application, mlmodel: MlModel, registry: ActivityResultRegistry) : AndroidViewModel(application) {

    companion object {
        const val READY_EXTRACT = "Ready to Extract"
        const val FAILED = "Failed to Load"
        const val READY_RESULT = "Ready for Result"
    }

    private val _data = MutableLiveData<List<String?>>()
    val data : LiveData<List<String?>> get() = _data

    private val _ready = MutableLiveData<String>()
    val ready : LiveData<String> get() = _ready

    private val _imageIcon = MutableLiveData<Bitmap>()
    val imageIcon : LiveData<Bitmap> = _imageIcon

    private val detector = FaceDetection.getClient()

    private val faceExpressions = ArrayList<String?>()
    private val mlModel = mlmodel as FaceOnExam

    private val mmr = MediaMetadataRetriever()

    private val getVideo = registry.register("vid", ActivityResultContracts.GetContent()) { uri: Uri? ->
        try {
            mmr.setDataSource(application.applicationContext, uri)
            _ready.postValue(READY_EXTRACT)
            _imageIcon.value = mmr.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            Log.d("Gagal", "Tidak Berhasil")
            _ready.postValue(FAILED)
            e.printStackTrace()
        }
    }

    fun clearFaces() {
        faceExpressions.clear()
    }

    fun extractImageAndDetectFace() {
        Log.d("Mulai", "dari ViewModel")
        viewModelScope.launch(Dispatchers.IO) {
            for (i in 1..10) {
                val bitmap = mmr.getFrameAtTime(
                    1000000 * i.toLong(),
                    MediaMetadataRetriever.OPTION_CLOSEST
                )
                Log.d("Config", "${bitmap?.config}")

                detectFaces(bitmap!!, i == 10)
            }
            _ready.postValue(READY_RESULT)
        }
    }

    private fun processFaceImage(bitmap: Bitmap) : String? {
        val image = if (bitmap.config == Bitmap.Config.ARGB_8888)
            bitmap
        else
            bitmap.copy(Bitmap.Config.ARGB_8888, false)

        val tImage = TensorImage.fromBitmap(image)
        val outputs = mlModel.process(tImage)
        val result = outputs.probabilityAsCategoryList.maxByOrNull { c -> c.score }

        return result?.label
    }

    private fun detectFaces(bitmap: Bitmap, updateAfterFinish: Boolean = false) {
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

    private fun updateFaces() {
        _data.postValue(faceExpressions)
    }

    fun getVideo() {
        getVideo.launch("video/*")
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