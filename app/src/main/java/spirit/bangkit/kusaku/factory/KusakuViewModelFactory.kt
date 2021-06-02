package spirit.bangkit.kusaku.factory

import android.app.Application
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import spirit.bangkit.kusaku.machinelearning.MlModel
import spirit.bangkit.kusaku.ui.MainViewModel
import spirit.bangkit.kusaku.utils.Injection

class KusakuViewModelFactory private constructor(
    private val application: Application,
    private val model: MlModel,
    private val registry: ActivityResultRegistry): ViewModelProvider.Factory {

    companion object {

        @Volatile
        private var instance : KusakuViewModelFactory? = null

        fun getInstance(application: Application, registry: ActivityResultRegistry) : KusakuViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: KusakuViewModelFactory(application, Injection.provideFaceOnExamModel(application.applicationContext), registry).apply {
                    instance = this
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                return MainViewModel(application, model, registry) as T
            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
    }
}