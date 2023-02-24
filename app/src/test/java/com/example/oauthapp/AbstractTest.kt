package com.example.oauthapp

import com.example.oauthapp.data.AbstractCloudDataSource
import com.example.oauthapp.data.CloudObject
import retrofit2.Response

abstract class AbstractTest {

    protected interface ExampleObjectCloudDataSource {

        suspend fun fetch(): ExampleCloudObject

        class Base(
            private val service: ExampleService,
//            private val tokenStorage : TokenStorage.ReadAccess
        ) : AbstractCloudDataSource(), ExampleObjectCloudDataSource {

            override suspend fun fetch(): ExampleCloudObject {
                return super.handle {
                    service.fetch(/*tokenStorage.accessToken()*/)
                }
            }
        }
    }

    protected interface ExampleService {

        suspend fun fetch(): Response<ExampleCloudObject>

        class Fake : ExampleService {

            var response: Response<ExampleCloudObject>? = null
            var exception: Exception? = null

            override suspend fun fetch(): Response<ExampleCloudObject> {
                return if (exception == null)
                    response!!
                else
                    throw exception!!
            }
        }
    }

    protected data class ExampleCloudObject(
        val data: String
    ) : CloudObject
}