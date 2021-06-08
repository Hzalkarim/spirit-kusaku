package spirit.bangkit.kusaku.data.source.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FacePost(
    @field:SerializedName("token")
    val token: String = "",

    @field:SerializedName("images")
    val images: List<String> = listOf(),

    @field:SerializedName("images_path")
    val imagesPath: List<String> = listOf(),

    @field:SerializedName("message")
    val message: String = "",

    @field:SerializedName("result")
    val result: FaceResult = FaceResult(),

    @field:SerializedName("status")
    val status: Boolean = false,
) : Parcelable