package spirit.bangkit.kusaku.data

import android.graphics.Bitmap
import io.reactivex.rxjava3.core.Observable

interface KusakuDataSource {

    fun getFacesBitmapObserver(videoFrames: List<Bitmap>) : Observable<Bitmap>

    fun getFaceDetectionResultObserver(faceImages: List<Bitmap>) : Observable<String>

    fun onClear()
}