package spirit.bangkit.kusaku.data

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.face.Face
import io.reactivex.rxjava3.core.Observable
import spirit.bangkit.kusaku.data.source.local.LocalDataSource

class KusakuLocalRepository private constructor(
    private val dataSource: LocalDataSource){

    companion object {
        @Volatile
        private var instance: KusakuLocalRepository? = null

        fun getInstance(dataSource: LocalDataSource) =
            instance ?: synchronized(this) {
                instance ?: KusakuLocalRepository(dataSource).apply {
                    instance = this
                }
            }
    }

    fun getFaceDetectionResultObserver(faceImages: List<Bitmap>): Observable<String> = dataSource.processFacesToLabel(faceImages)

    fun onClear() {
        dataSource.clear()
    }

}