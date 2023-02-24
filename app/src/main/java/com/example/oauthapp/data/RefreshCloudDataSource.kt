package com.example.oauthapp.data

import retrofit2.Response

interface RefreshCloudDataSource {

    suspend fun refresh(): RefreshCloud

    class Base(
        private val service: RefreshService
    ) : AbstractCloudDataSource(), RefreshCloudDataSource {

        override suspend fun refresh(): RefreshCloud {
            return super.handle {
                service.fetch()
            }
        }
    }
}

interface RefreshService {

    suspend fun fetch(): Response<RefreshCloud>

}