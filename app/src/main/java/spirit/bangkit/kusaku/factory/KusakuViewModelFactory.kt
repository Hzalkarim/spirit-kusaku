package spirit.bangkit.kusaku.factory

import android.app.Application
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import spirit.bangkit.kusaku.data.KusakuLocalRepository
import spirit.bangkit.kusaku.data.KusakuRemoteRepository
import spirit.bangkit.kusaku.data.source.remote.ApiConfig
import spirit.bangkit.kusaku.machinelearning.MlModel
import spirit.bangkit.kusaku.ui.main.MainLocalViewModel
import spirit.bangkit.kusaku.ui.main.MainRemoteViewModel
import spirit.bangkit.kusaku.ui.main.MainViewModel
import spirit.bangkit.kusaku.utils.Injection

class KusakuViewModelFactory private constructor(
    private val application: Application,
    private val model: MlModel,
    private val registry: ActivityResultRegistry,
    private val localRepository: KusakuLocalRepository? = null,
    private val remoteRepository: KusakuRemoteRepository): ViewModelProvider.Factory {

    companion object {

        @Volatile
        private var instance : KusakuViewModelFactory? = null

        fun getInstance(application: Application, registry: ActivityResultRegistry) : KusakuViewModelFactory =
            instance ?: synchronized(this) {
                instance ?:
                    KusakuViewModelFactory(application,
                        Injection.provideFaceOnExamModel(application.applicationContext),
                        registry,
                        Injection.provideLocalRepository(application.applicationContext),
                        Injection.provideRemoteRepository(ApiConfig.getApiService())
                    ).apply {
                            instance = this
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                return MainViewModel(application, model, registry) as T
            modelClass.isAssignableFrom(MainLocalViewModel::class.java) ->
                return MainLocalViewModel(application, localRepository!!, registry) as T
            modelClass.isAssignableFrom(MainRemoteViewModel::class.java) ->
                return MainRemoteViewModel(application, remoteRepository, registry) as T
            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
    }
}