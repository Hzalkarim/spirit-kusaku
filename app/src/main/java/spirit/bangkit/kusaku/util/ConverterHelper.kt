package spirit.bangkit.kusaku.util

import spirit.bangkit.kusaku.data.source.remote.response.FaceResult

class ConverterHelper {
    companion object {
        fun stringListToFaceResult(list: List<String>) : FaceResult {
            val grouped = list.groupingBy { it }.eachCount()
            return FaceResult(
                grouped.getOrElse("Angry", this::getZero),
                grouped.getOrElse("Disgust", this::getZero),
                grouped.getOrElse("Fear", this::getZero),
                grouped.getOrElse("Happy", this::getZero),
                grouped.getOrElse("Neutral", this::getZero),
                grouped.getOrElse("Sad", this::getZero),
                grouped.getOrElse("Surprise", this::getZero))
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

        private fun getZero() = 0
    }
}