package com.imjori.flickrstroll.di

import com.imjori.flickrstroll.data.search.FlickrPhotoSearchApi
import com.imjori.flickrstroll.data.search.FlickrPhotoSearchRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.flickr.com/services/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideFlickrPhotosApi(retrofit: Retrofit): FlickrPhotoSearchApi =
        retrofit.create(FlickrPhotoSearchApi::class.java)

    @Provides
    @Singleton
    fun provideFlickrPhotosRepository(flickrPhotoSearchApi: FlickrPhotoSearchApi): FlickrPhotoSearchRepository =
        FlickrPhotoSearchRepository(flickrPhotoSearchApi)
}
