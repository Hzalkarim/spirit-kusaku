package spirit.bangkit.kusaku.utils

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResultRegistry
import spirit.bangkit.kusaku.machinelearning.FaceOnExam

object Injection {
    fun provideFaceOnExamModel(context: Context) = FaceOnExam.newInstance(context)
}