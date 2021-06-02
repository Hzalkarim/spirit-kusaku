package spirit.bangkit.kusaku.utils

import android.content.Context
import spirit.bangkit.kusaku.machinelearning.FaceOnExam

object Injection {
    fun provideFaceOnExamModel(context: Context) = FaceOnExam.newInstance(context)
}