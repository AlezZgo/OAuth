package com.example.oauthapp

import com.example.oauthapp.data.TokenInvalidException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositoryTest : AbstractTest() {
    //todo 2 cases with no internet connection

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
        val expected = ExampleCloudObject("success")

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
        val expected = ExampleCloudObject("success")

        assertEquals(expected, actual)
        assertEquals(2, fakeExampleObjectCloudDataSource.fetchCount)
        assertEquals(1, fakeRefreshCloudDataSource.refreshCount)

    }

    @Test(expected = TokenInvalidException::class)
    fun `failed to refresh tokens`() : Unit = runBlocking {
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

        suspend fun fetch(): ExampleCloudObject

        class Base(
            refreshCloudDataSource: RefreshCloudDataSource,
            private val exampleObjectCloudDataSource: ExampleObjectCloudDataSource,
        ) : AbstractRepository(
            refreshCloudDataSource
        ), ExampleRepository {

            override suspend fun fetch(): ExampleCloudObject {
                return super.handle {
                    exampleObjectCloudDataSource.fetch()
                }
            }
        }


    }

}

