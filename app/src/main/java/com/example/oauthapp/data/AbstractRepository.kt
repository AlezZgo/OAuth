package com.example.oauthapp.data

import java.net.UnknownHostException

abstract class AbstractRepository(
    private val handleError: HandleError<Exception> = HandleError.Domain(),
    private val handleRefresh: HandleRefresh
//    private val tokenStorage : TokenStorage.Mutable
) {

    protected suspend fun <R : Any> handle(
        block: suspend () -> R
    ): R = try {
        block.invoke()
    } catch (e: TokenInvalidException) {
        handleRefresh.handle(block)
    } catch (e: Exception) {
        throw handleError.handle(e)
    }

}

interface HandleRefresh {
    suspend fun <R> handle(block: suspend () -> R): R

    class Base(
        private val refreshCloudDataSource: RefreshCloudDataSource,
        private val handleError: HandleError<Exception> = HandleError.Domain(),
    ) : HandleRefresh {
        override suspend fun <R> handle(block: suspend () -> R): R {
            return try {
                val tokens = refreshCloudDataSource.refresh(/*tokenStorage.refreshToken()*/)
//              tokens.save(tokenStorage)
                block.invoke()
            } catch (e: Exception) {
                throw handleError.handle(e)
            }
        }

    }
}

interface HandleError<T : Any> {
    fun handle(exception: Exception): T

    class Domain : HandleError<Exception> {

        override fun handle(exception: Exception) = when (exception) {
            is UnknownHostException -> NoConnectionException()
            else -> exception
        }
    }
}

interface TokenStorage {

    interface Save {
        fun updateTokens(
            accessToken: String,
            refreshToken: String
        )
    }

    interface ReadAccess {
        fun accessToken(): String

    }

    interface ReadRefresh {
        fun refreshToken(): String
    }

    interface Read : ReadAccess, ReadRefresh

    interface Mutable : Save, Read

    class Base : Mutable {
        override fun accessToken(): String {
            TODO("Not yet implemented")
        }

        override fun refreshToken(): String {
            TODO("Not yet implemented")
        }

        override fun updateTokens(accessToken: String, refreshToken: String) {
            TODO("Not yet implemented")
        }

    }
}



