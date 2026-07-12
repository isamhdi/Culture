package com.culture.tracker.data.network

import com.culture.tracker.data.local.entity.SensorSource
import com.culture.tracker.domain.model.SensorSourceType
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

data class SensorFetchResult(
    val temperatureCelsius: Double?,
    val humidityPercent: Double?,
    /** Erreur partielle (ex : une des deux entités Home Assistant a échoué) sans faire échouer tout le relevé. */
    val partialError: String? = null,
)

private const val TIMEOUT_MS = 10_000

/**
 * Interroge un capteur externe (Home Assistant ou webhook générique) en HTTP simple, sans
 * dépendance réseau tierce. N'est jamais appelé si l'utilisateur n'a pas explicitement activé
 * les capteurs externes dans les Réglages (voir SensorRepository).
 */
object SensorReadingFetcher {

    suspend fun fetch(source: SensorSource): Result<SensorFetchResult> = withContext(Dispatchers.IO) {
        try {
            when (source.type) {
                SensorSourceType.HOME_ASSISTANT -> Result.success(fetchHomeAssistant(source))
                SensorSourceType.WEBHOOK -> Result.success(fetchWebhook(source))
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: JSONException) {
            Result.failure(IOException("Réponse inattendue du serveur : ${e.message}", e))
        }
    }

    private fun fetchHomeAssistant(source: SensorSource): SensorFetchResult {
        var temperature: Double? = null
        var humidity: Double? = null
        val errors = mutableListOf<String>()

        source.temperatureEntityId?.takeIf { it.isNotBlank() }?.let { entityId ->
            runCatching { fetchHaEntityState(source.baseUrl, source.accessToken, entityId) }
                .onSuccess { temperature = it }
                .onFailure { errors += "Température (${entityId}) : ${it.message}" }
        }
        source.humidityEntityId?.takeIf { it.isNotBlank() }?.let { entityId ->
            runCatching { fetchHaEntityState(source.baseUrl, source.accessToken, entityId) }
                .onSuccess { humidity = it }
                .onFailure { errors += "Humidité (${entityId}) : ${it.message}" }
        }

        if (temperature == null && humidity == null) {
            throw IOException(errors.joinToString("; ").ifBlank { "Aucune entité de température ou d'humidité configurée" })
        }
        return SensorFetchResult(temperature, humidity, errors.joinToString("; ").ifBlank { null })
    }

    private fun fetchHaEntityState(baseUrl: String, token: String?, entityId: String): Double {
        val url = URL("${baseUrl.trimEnd('/')}/api/states/$entityId")
        val body = httpGet(url) {
            if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
        }
        val state = JSONObject(body).optString("state")
        return state.toDoubleOrNull() ?: throw IOException("État non numérique reçu : \"$state\"")
    }

    private fun fetchWebhook(source: SensorSource): SensorFetchResult {
        val body = httpGet(URL(source.baseUrl)) {}
        val json = JSONObject(body)
        val temperature = if (json.has("temperature") && !json.isNull("temperature")) json.optDouble("temperature").takeIf { !it.isNaN() } else null
        val humidity = if (json.has("humidity") && !json.isNull("humidity")) json.optDouble("humidity").takeIf { !it.isNaN() } else null
        if (temperature == null && humidity == null) {
            throw IOException("Réponse JSON invalide : attendu {\"temperature\": .., \"humidity\": ..}")
        }
        return SensorFetchResult(temperature, humidity)
    }

    private inline fun httpGet(url: URL, configure: HttpURLConnection.() -> Unit): String {
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/json")
            connection.configure()
            connection.connect()
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IOException("HTTP $code (${connection.responseMessage ?: "erreur"})")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } catch (e: SocketTimeoutException) {
            throw IOException("Délai dépassé — vérifie que l'appareil est bien sur le même réseau local", e)
        } finally {
            connection.disconnect()
        }
    }
}
