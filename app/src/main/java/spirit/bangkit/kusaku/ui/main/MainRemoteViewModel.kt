package spirit.bangkit.kusaku.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import spirit.bangkit.kusaku.data.KusakuRemoteRepository
import spirit.bangkit.kusaku.ui.main.base.MainBaseViewModel
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class MainRemoteViewModel(
    application: Application,
    private val repository: KusakuRemoteRepository,
    registry: ActivityResultRegistry) : MainBaseViewModel(application, registry) {

    private val bitmapArray = ArrayList<Bitmap>()

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
                    
                }

            })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun bitmapToBase64(bitmap: Bitmap) : String? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val byteArray = baos.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }
}