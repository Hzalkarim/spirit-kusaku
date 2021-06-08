package spirit.bangkit.kusaku.util

import android.os.Build
import androidx.annotation.RequiresApi
import spirit.bangkit.kusaku.data.source.remote.response.FaceResult

class ConverterHelper {
    companion object {
        @RequiresApi(Build.VERSION_CODES.N)
        fun stringListToFaceResult(list: List<String>) : FaceResult {
            val grouped = list.groupingBy { it }.eachCount()
            return FaceResult(
                grouped.getOrDefault("Angry", 0),
                grouped.getOrDefault("Disgust", 0),
                grouped.getOrDefault("Fear", 0),
                grouped.getOrDefault("Happy", 0),
                grouped.getOrDefault("Neutral", 0),
                grouped.getOrDefault("Sad", 0),
                grouped.getOrDefault("Surprise", 0))
        }

        fun faceResultToMap(faceResult: FaceResult) : Map<String, Int> =
            mapOf(
                "Angry" to faceResult.Angry,
                "Disgust" to faceResult.Disgust,
                "Fear" to faceResult.Fear,
                "Happy" to faceResult.Happy,
                "Neutral" to faceResult.Neutral,
                "Sad" to faceResult.Sad,
                "Surprise" to faceResult.Surprise
            )
    }
}