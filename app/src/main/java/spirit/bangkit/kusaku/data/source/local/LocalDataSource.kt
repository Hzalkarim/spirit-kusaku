package spirit.bangkit.kusaku.data.source.local

import android.content.Context
import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import io.reactivex.rxjava3.core.Observable
import org.tensorflow.lite.support.image.TensorImage
import spirit.bangkit.kusaku.machinelearning.FaceOnExam
import spirit.bangkit.kusaku.machinelearning.MlModel
import java.lang.Exception

class LocalDataSource(mlModel: MlModel) {

    companion object {
        @Volatile
        private var instance: LocalDataSource? = null

        fun getInstance(context: Context) : LocalDataSource =
            instance ?: synchronized( this) {
                instance ?: LocalDataSource(FaceOnExam.newInstance(context)).apply { instance = this }
            }
    }

    private val detector = FaceDetection.getClient()
    private val model = mlModel as FaceOnExam

    fun detectFacesFromVideoFrames(bitmaps: List<Bitmap>) {
        try {
            for (bitmap in bitmaps) {
                val inputImage = InputImage.fromBitmap(bitmap, 0)
                detector.process(inputImage).addOnSuccessListener { faces ->
                    for (face in faces){
                        bitmap.cropFace(face)
                    }
                }
            }
        } catch (e:Exception) {
        }
    }

    fun processFacesToLabel(list: List<Bitmap>): Observable<String> = Observable.create {
        try {
            for (bitmap in list) {
                val image = if (bitmap.config == Bitmap.Config.ARGB_8888)
                    bitmap
                else
                    bitmap.copy(Bitmap.Config.ARGB_8888, false)

                val tImage = TensorImage.fromBitmap(image)
                val outputs = model.process(tImage)
                val result = outputs.probabilityAsCategoryList.maxByOrNull { c -> c.score }

                it.onNext(result?.label)
            }
            it.onComplete()
        } catch (e: Exception) {
            it.onError(e)
        }
    }

    private fun Bitmap.cropFace(face: Face) : Bitmap {
        with (face.boundingBox) {
            val x = centerX() - width()/2
            val y = centerY() - height()/2
            return Bitmap.createBitmap(this@cropFace, if (x + width() > width()) width() else x ,
                if (y + height() > height()) height() else y, width(), height())
        }
    }

    fun clear() {
        detector.close()
        model.close()
    }
}