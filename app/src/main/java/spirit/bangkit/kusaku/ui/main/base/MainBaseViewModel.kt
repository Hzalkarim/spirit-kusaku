package spirit.bangkit.kusaku.ui.main.base

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class MainBaseViewModel(application: Application, registry: ActivityResultRegistry) : AndroidViewModel(application) {

    companion object {
        const val EXTRACT_FRAME = "extract_frame"
        const val DETECT_LABEL_FACE = "detect_label_face"
        const val IDLE = "idle"
    }

    protected val _imageIcon = MutableLiveData<Bitmap>()
    val imageIcon get() = _imageIcon

    protected val _loading = MutableLiveData<Int>()
    val loading: LiveData<Int> get() = _loading

    protected val _workingOn = MutableLiveData<String>()
    val workingOn: LiveData<String> get() = _workingOn

    protected val _ready = MutableLiveData<String>()
    val ready get() : LiveData<String> = _ready

    protected val _data = MutableLiveData<List<String>>()
    val data : LiveData<List<String>> get() = _data

    protected val mmr = MediaMetadataRetriever()
    protected val getVideo = registry.register("vid", ActivityResultContracts.GetContent()) { uri: Uri? ->
        mmr.setDataSource(application.applicationContext, uri)
        _ready.value = "READY"
    }

    fun getVideo() {
        getVideo.launch("video/*")
    }
}