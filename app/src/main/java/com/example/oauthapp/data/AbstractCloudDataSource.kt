package com.example.oauthapp.data

import retrofit2.Response

abstract class AbstractCloudDataSource {

    protected suspend fun <T : CloudObject> handle(
        block: suspend () -> Response<T>
    ): T {

        var response: Response<T>? = null

        try {
            response = block.invoke()
            return response.body()!!
        } catch (e: Exception) {
            if (response?.code() == 401)
                throw AccessTokenInvalidException()
            throw e
        }
    }
}