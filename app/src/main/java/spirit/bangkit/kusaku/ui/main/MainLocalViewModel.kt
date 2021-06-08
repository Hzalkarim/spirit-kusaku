package spirit.bangkit.kusaku.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import spirit.bangkit.kusaku.data.KusakuLocalRepository
import spirit.bangkit.kusaku.ui.main.base.MainBaseViewModel
import java.lang.Exception

class MainLocalViewModel(
    application: Application,
    private val repository: KusakuLocalRepository,
    registry: ActivityResultRegistry) : MainBaseViewModel(application, registry) {

    private val detector = FaceDetection.getClient()

    private val bitmapArray = ArrayList<Bitmap>()
    private val stringArray = ArrayList<String>()
    private var count = 0
    private var maxLoad = 0

    fun startProcessingVideo() { extractFrameFromVideo() }

    private fun extractFrameFromVideo() {
        bitmapArray.clear()
        _loading.value = 0
        Observable.create<Bitmap> {
            try {
                for (i in 1..10) {
                    val bitmap = mmr.getFrameAtTime(1000000 * i.toLong())
                    it.onNext(bitmap)
                }
                it.onComplete()
            } catch (e: Exception) {
                it.onError(e)
            }
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<Bitmap> {
                override fun onSubscribe(d: Disposable?) {
                    _workingOn.value = EXTRACT_FRAME
                    Log.d("Mulai subskep", "cari frem")
                }

                override fun onNext(t: Bitmap?) {
                    bitmapArray.add(t!!)
                    val newVal = _loading.value?.plus(1)
                    _loading.value = newVal
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    _workingOn.value = IDLE
                }

                override fun onComplete() {
                    getFacesFromVideoImages(bitmapArray.toList())
                    Log.d("Selesai", "cari frem")
                }

            })
    }

    private fun getFacesFromVideoImages(videoFrame: List<Bitmap>) {
        bitmapArray.clear()
        stringArray.clear()
        count = 0
        maxLoad = videoFrame.size
        _loading.value = 0
        _workingOn.value = DETECT_FACE
        for (i in videoFrame.indices) {
            val inputImage = InputImage.fromBitmap(videoFrame[i], 0)
            detector.process(inputImage).addOnSuccessListener {
                val arr = ArrayList<Bitmap>()
                for (face in it){
                    arr.add(videoFrame[i].cropFace(face))
                }
                getLabelsFromFaceImages(arr)
            }
            /*if (i == videoFrame.lastIndex)
                getLabelsFromFaceImages(bitmapArray.toMutableList())*/
        }
    }

    private fun getLabelsFromFaceImages(faces: List<Bitmap>) {
        repository.getFaceDetectionResultObserver(faces)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( object : Observer<String> {
                override fun onSubscribe(d: Disposable?) {
                    _workingOn.value = LABEL_FACE
                    Log.d("Mulai subskep", "cari Label ${faces.size}")
                }

                override fun onNext(t: String?) {
                    stringArray.add(t!!)
                    Log.d("Label Muka", t)
                    val newVal = _loading.value?.plus(1)
                    _loading.value = newVal
                    count += 1
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                    _workingOn.value = IDLE

                }

                override fun onComplete() {
                    _data.value = stringArray
                    if (workingOn.value != IDLE && count == maxLoad)
                        _workingOn.value = DONE
                    Log.d("Hasil", stringArray.toString())
                    Log.d("Seles subskep", "cari Label ${stringArray.size} - $count")
                }
            })
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
        detector.close()
        repository.onClear()
    }

}