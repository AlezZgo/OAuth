package com.example.oauthapp

import com.example.oauthapp.data.AbstractCloudDataSource
import com.example.oauthapp.data.AccessTokenInvalidException
import com.example.oauthapp.data.CloudObject
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException


class CloudDataSourceTest {

    @Test
    fun `success response`() = runBlocking {

        val service = ExampleService.Fake()

        service.response = Response.success(
            ExampleCloudObject(
                "success"
            )
        )

        val cloudDataSource = ExampleObjectCloudDataSource.Base(service)

        val actual: ExampleCloudObject = cloudDataSource.fetch()

        val expected = ExampleCloudObject(
            "success"
        )

        assertEquals(expected, actual)

    }

    @Test(expected = AccessTokenInvalidException::class)
    fun `token invalid response`() : Unit = runBlocking {

        val service = ExampleService.Fake()

        service.response = Response.error(
            401, "{\"error\" : \"auth invalid\"}".toResponseBody(
                "application/json".toMediaType()
            )
        )

        ExampleObjectCloudDataSource.Base(service).fetch()
    }

    @Test(expected = UnknownHostException::class)
    fun `handle no connection`() : Unit = runBlocking {

        val service = ExampleService.Fake()

        service.exception = UnknownHostException()

        ExampleObjectCloudDataSource.Base(service).fetch()

    }

}

private interface ExampleObjectCloudDataSource {

    suspend fun fetch(): ExampleCloudObject

    class Base(
        private val service: ExampleService
    ) : AbstractCloudDataSource(), ExampleObjectCloudDataSource {

        override suspend fun fetch(): ExampleCloudObject {
            return super.handle {
                service.fetch()
            }
        }
    }

}

private interface ExampleService {

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

private data class ExampleCloudObject(
    val data: String
) : CloudObject