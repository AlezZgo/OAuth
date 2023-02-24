package com.example.oauthapp

import com.example.oauthapp.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.UnknownHostException

class RepositoryTest : AbstractTest() {

    @Test
    fun `success with valid access token`() = runBlocking {

        val fakeRefreshCloudDataSource = FakeRefreshCloudDataSource()
        val fakeExampleObjectCloudDataSource = FakeExampleObjectCloudDataSource()

        fakeExampleObjectCloudDataSource.list.add(ExampleCloudObject("success"))

        val repository = ExampleRepository.Base(
            fakeRefreshCloudDataSource,
            fakeExampleObjectCloudDataSource
        )

        val actual = repository.fetch()
        val expected = ExampleRepository.ExampleDomainObject.Base(ExampleCloudObject("success"))

        assertEquals(expected, actual)
        assertEquals(1, fakeExampleObjectCloudDataSource.fetchCount)
        assertEquals(0, fakeRefreshCloudDataSource.refreshCount)
    }

    @Test
    fun `success with invalid access token`() = runBlocking {

        val fakeRefreshCloudDataSource = FakeRefreshCloudDataSource()
        val fakeExampleObjectCloudDataSource = FakeExampleObjectCloudDataSource()

        fakeExampleObjectCloudDataSource.list.add(
            TokenInvalidException()
        )
        fakeExampleObjectCloudDataSource.list.add(ExampleCloudObject("success"))

        fakeRefreshCloudDataSource.response = RefreshCloud(
            accessToken = "dfsgsw45g4",
            refreshToken = "2198741928u19uesddcwef"
        )

        val repository = ExampleRepository.Base(
            fakeRefreshCloudDataSource,
            fakeExampleObjectCloudDataSource
        )

        val actual = repository.fetch()
        val expected = ExampleRepository.ExampleDomainObject.Base(ExampleCloudObject("success"))

        assertEquals(expected, actual)
        assertEquals(2, fakeExampleObjectCloudDataSource.fetchCount)
        assertEquals(1, fakeRefreshCloudDataSource.refreshCount)
    }

    @Test(expected = TokenInvalidException::class)
    fun `failed to refresh tokens`(): Unit = runBlocking {
        val fakeRefreshCloudDataSource = FakeRefreshCloudDataSource()
        val fakeExampleObjectCloudDataSource = FakeExampleObjectCloudDataSource()

        fakeExampleObjectCloudDataSource.list.add(
            TokenInvalidException()
        )

        fakeRefreshCloudDataSource.exception = TokenInvalidException()

        val repository = ExampleRepository.Base(
            fakeRefreshCloudDataSource,
            fakeExampleObjectCloudDataSource
        )

        repository.fetch()
    }

    @Test(expected = NoConnectionException::class)
    fun `internet failed`(): Unit = runBlocking {
        val fakeRefreshCloudDataSource = FakeRefreshCloudDataSource()
        val fakeExampleObjectCloudDataSource = FakeExampleObjectCloudDataSource()

        fakeExampleObjectCloudDataSource.list.add(
            TokenInvalidException()
        )

        fakeRefreshCloudDataSource.exception = UnknownHostException()

        val repository = ExampleRepository.Base(
            fakeRefreshCloudDataSource,
            fakeExampleObjectCloudDataSource
        )

        repository.fetch()
    }

    @Test
    fun `CloudDataSource no internet connection`(): Unit = runBlocking {
        val fakeRefreshCloudDataSource = FakeRefreshCloudDataSource()
        val fakeExampleObjectCloudDataSource = FakeExampleObjectCloudDataSource()

        fakeExampleObjectCloudDataSource.list.add(
            UnknownHostException()
        )

        val repository = ExampleRepository.Base(
            fakeRefreshCloudDataSource,
            fakeExampleObjectCloudDataSource
        )

        try {
            repository.fetch()
        } catch (e: Exception) {
            assertEquals(NoConnectionException::class, e::class)
            assertEquals(0, fakeRefreshCloudDataSource.refreshCount)
        }

    }

    private class FakeRefreshCloudDataSource() : RefreshCloudDataSource {

        var response: RefreshCloud? = null
        var exception: Exception? = null

        var refreshCount = 0

        override suspend fun refresh(): RefreshCloud {
            refreshCount++
            return if (exception == null)
                response!!
            else
                throw exception!!
        }
    }

    private class FakeExampleObjectCloudDataSource : ExampleObjectCloudDataSource {

        val list = mutableListOf<Any>()

        var fetchCount = 0

        override suspend fun fetch(): ExampleCloudObject {
            val obj = list[fetchCount++]

            return if (obj is ExampleCloudObject)
                obj
            else
                throw obj as Exception
        }
    }

    private interface ExampleRepository {

        suspend fun fetch(): ExampleDomainObject

        class Base(
            private val refreshCloudDataSource: RefreshCloudDataSource,
            private val exampleObjectCloudDataSource: ExampleObjectCloudDataSource,
            handleError: HandleError<Exception> = HandleError.Domain(),
            handleRefresh: HandleRefresh = HandleRefresh.Base(refreshCloudDataSource)
        ) : AbstractRepository(
            handleError, handleRefresh
        ), ExampleRepository {

            override suspend fun fetch() = super.handle {

                val result = exampleObjectCloudDataSource.fetch()

                ExampleDomainObject.Base(result)
            }
        }

        interface ExampleDomainObject {
            data class Base(
                val cloudObject: ExampleCloudObject
            ) : ExampleDomainObject
        }

    }
}

