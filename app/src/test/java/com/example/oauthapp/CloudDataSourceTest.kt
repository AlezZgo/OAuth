package com.example.oauthapp

import com.example.oauthapp.data.TokenInvalidException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response
import java.net.UnknownHostException

class CloudDataSourceTest : AbstractTest() {

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

    @Test(expected = TokenInvalidException::class)
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