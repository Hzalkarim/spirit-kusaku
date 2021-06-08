package spirit.bangkit.kusaku.utils

import android.content.Context
import spirit.bangkit.kusaku.data.KusakuLocalRepository
import spirit.bangkit.kusaku.data.KusakuRemoteRepository
import spirit.bangkit.kusaku.data.source.local.LocalDataSource
import spirit.bangkit.kusaku.data.source.remote.FaceDetectorService
import spirit.bangkit.kusaku.data.source.remote.RemoteDataSource
import spirit.bangkit.kusaku.machinelearning.FaceOnExam

object Injection {
    fun provideFaceOnExamModel(context: Context) = FaceOnExam.newInstance(context)

    fun provideLocalRepository(context: Context) : KusakuLocalRepository {
        val localDataSource = LocalDataSource.getInstance(context)

        return KusakuLocalRepository.getInstance(localDataSource)
    }

    fun provideRemoteRepository(service: FaceDetectorService) : KusakuRemoteRepository {
        val remoteDataSource = RemoteDataSource.getInstance(service)

        return KusakuRemoteRepository.getInstance(remoteDataSource)
    }
}