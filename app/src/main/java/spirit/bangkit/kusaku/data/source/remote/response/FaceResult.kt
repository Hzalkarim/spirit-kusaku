package spirit.bangkit.kusaku.data.source.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaceResult(
    @field:SerializedName("Angry")
    val Angry: Int = 0,

    @field:SerializedName("Disgust")
    val Disgust: Int = 0,

    @field:SerializedName("Fear")
    val Fear: Int = 0,

    @field:SerializedName("Happy")
    val Happy: Int = 0,

    @field:SerializedName("Neutral")
    val Neutral: Int = 0,

    @field:SerializedName("Sad")
    val Sad: Int = 0,

    @field:SerializedName("Surprise")
    val Surprise: Int = 0,

    @field:SerializedName("status")
    val status: Boolean = false,

    @field:SerializedName("message")
    val message: String = ""
) : Parcelable
