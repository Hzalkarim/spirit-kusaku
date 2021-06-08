package spirit.bangkit.kusaku.data.source.remote

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*
import spirit.bangkit.kusaku.data.source.remote.response.FacePost
import spirit.bangkit.kusaku.data.source.remote.response.Result

interface FaceDetectorService {

    @POST("predict")
    fun postImage(
        @Field("token") token: String,
        @Field("images") images: List<String>
    ) : Observable<FacePost>

    @GET("result")
    fun getResult(
        @Query("token") token: String
    ) : Observable<Result>
}