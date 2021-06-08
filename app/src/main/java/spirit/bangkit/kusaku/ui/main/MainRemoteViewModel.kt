package spirit.bangkit.kusaku.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.result.ActivityResultRegistry
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import spirit.bangkit.kusaku.data.KusakuRemoteRepository
import spirit.bangkit.kusaku.data.source.remote.response.FacePost
import spirit.bangkit.kusaku.data.source.remote.response.FaceResult
import spirit.bangkit.kusaku.ui.main.base.MainBaseViewModel
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainRemoteViewModel(
    application: Application,
    private val repository: KusakuRemoteRepository,
    registry: ActivityResultRegistry) : MainBaseViewModel(application, registry) {

    companion object {
        const val ENCODING_FRAME = "encoding_frame"
        const val WAITING_RESPONSE = "waiting_response"
        const val SERVER_BUSY = "server_busy"
    }

    private val _postResponse = MutableLiveData<FacePost>()
    val postResponse get() = _postResponse

    private val _getResponse = MutableLiveData<FaceResult>()
    val getResponse get() = _getResponse

    var token: String = "halo"

    private val bitmapArray = ArrayList<Bitmap>()
    private val stringArray = ArrayList<String>()

    override fun onCleared() {
        super.onCleared()
        clear()
    }

    fun startProcessingVideo() { extractFrameFromVideo() }

    fun getResultFromRemoteModel() {
        _workingOn.value = LABEL_FACE
        repository.getResultObservable(token)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _getResponse.value = it
                _workingOn.value = if (it.status) DONE else SERVER_BUSY
            }
    }

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
                }

                override fun onNext(t: Bitmap?) {
                    bitmapArray.add(t!!)
                    val newVal = _loading.value?.plus(1)
                    _loading.value = newVal
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }

                override fun onComplete() {
                    encodeVideoFrameBitmaps(bitmapArray.toMutableList())
                }

            })
    }

    private fun encodeVideoFrameBitmaps(videoFrame: List<Bitmap>) {
        stringArray.clear()
        _loading.value = 0
        Observable.create<String> {
            try {
                for (frame in videoFrame) {
                    it.onNext(bitmapToBase64(frame))
                }
                it.onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( object : Observer<String> {
                override fun onSubscribe(d: Disposable?) {
                    _workingOn.value = ENCODING_FRAME
                }

                override fun onNext(t: String?) {
                    stringArray.add(t!!)
                    val newVal = _loading.value?.plus(1)
                    _loading.value = newVal
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }

                override fun onComplete() {
                    postImageToRemoteModel(stringArray.toList())
                }
            })
    }

    private fun postImageToRemoteModel(listString: List<String>) {
        repository.postImagesObservable(token, listString)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe( object : Observer<FacePost> {
                override fun onSubscribe(d: Disposable?) {
                    _workingOn.value = WAITING_RESPONSE
                }

                override fun onNext(t: FacePost?) {
                    if (t != null){
                        _postResponse.value = t
                    }
                }

                override fun onError(e: Throwable?) {
                    e?.printStackTrace()
                }

                override fun onComplete() {
                    getResultFromRemoteModel()
                }

            })
    }

    private fun bitmapToBase64(bitmap: Bitmap) : String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val byteArray = baos.toByteArray()
        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
    }
}