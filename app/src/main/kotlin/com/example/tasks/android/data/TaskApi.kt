package com.example.tasks.android.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

// KMP-shared candidate: the interface moves to commonMain; HttpURLConnection impl becomes Ktor.
interface TaskApi {
    suspend fun fetchTodos(): List<TodoDto>
}

class HttpTaskApi(
    private val endpoint: String = DEFAULT_ENDPOINT,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TaskApi {

    override suspend fun fetchTodos(): List<TodoDto> = withContext(Dispatchers.IO) {
        val connection = URI(endpoint).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            json.decodeFromString<List<TodoDto>>(body)
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val DEFAULT_ENDPOINT = "https://jsonplaceholder.typicode.com/todos?_limit=20"
        private const val TIMEOUT_MS = 15_000
    }
}
