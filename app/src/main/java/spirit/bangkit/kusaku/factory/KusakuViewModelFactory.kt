package spirit.bangkit.kusaku.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import spirit.bangkit.kusaku.machinelearning.MlModel
import spirit.bangkit.kusaku.ui.MainViewModel
import spirit.bangkit.kusaku.utils.Injection

class KusakuViewModelFactory private constructor(val model: MlModel): ViewModelProvider.Factory {

    companion object {

        @Volatile
        private var instance : KusakuViewModelFactory? = null

        fun getInstance(context: Context) : KusakuViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: KusakuViewModelFactory(Injection.provideFaceOnExamModel(context)).apply {
                    instance = this
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                return MainViewModel(model) as T
            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
    }
}