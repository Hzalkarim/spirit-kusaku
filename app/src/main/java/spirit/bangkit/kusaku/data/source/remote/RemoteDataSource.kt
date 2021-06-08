package spirit.bangkit.kusaku.data.source.remote

import spirit.bangkit.kusaku.data.source.remote.response.FacePost

class RemoteDataSource(private val apiService: FaceDetectorService) {

    companion object {
        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(service: FaceDetectorService) : RemoteDataSource =
            instance ?: synchronized( this) {
                instance ?: RemoteDataSource(service).apply { instance = this }
            }
    }

    fun postImagesObservable(token: String, faceImagesStream: List<String>) =
        apiService.postImage(FacePost(token, faceImagesStream))

    fun getResultsObservable(token: String) = apiService.getResult(token)
}