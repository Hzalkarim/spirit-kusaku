package spirit.bangkit.kusaku.data

import spirit.bangkit.kusaku.data.source.remote.RemoteDataSource

class KusakuRemoteRepository(private val dataSource: RemoteDataSource) {

    companion object {
        @Volatile
        private var instance: KusakuRemoteRepository? = null

        fun getInstance(dataSource: RemoteDataSource) =
            instance ?: synchronized(this) {
                instance ?: KusakuRemoteRepository(dataSource).apply {
                    instance = this
                }
            }
    }

    fun postImagesObservable(token: String, images: List<String>) =
        dataSource.postImagesObservable(token, images)

    fun getResultObservable(token: String) = dataSource.getResultsObservable(token)
}