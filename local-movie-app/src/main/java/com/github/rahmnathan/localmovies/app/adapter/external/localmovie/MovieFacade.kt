package com.github.rahmnathan.localmovies.app.adapter.external.localmovie

import com.github.rahmnathan.localmovies.app.data.Client
import com.github.rahmnathan.localmovies.app.data.Media
import com.github.rahmnathan.localmovies.app.data.MovieEvent
import com.github.rahmnathan.localmovies.app.data.MovieRequest
import com.google.gson.Gson
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Supplier
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.ArrayList

object MovieFacade {
    private val logger = Logger.getLogger(MovieFacade::class.java.name)
    private const val X_CORRELATION_ID = "x-correlation-id"
    private const val COUNT_HEADER = "Count"
    private val GSON = Gson()

    fun getMovieInfo(client: Client, movieRequest: MovieRequest): List<Media> {
        val xCorrelationId = UUID.randomUUID().toString()
        logger.info("Requesting movies with x-correlation-id: $xCorrelationId")

        val movieInfoJson = getMovieInfoJson(client, movieRequest, xCorrelationId)
        return movieInfoJson.map { obj: JSONArray -> JSONtoMovieMapper.jsonArrayToMovieInfoList(obj) }.orElseGet { ArrayList() }
    }

    @JvmStatic
    fun getMovieEvents(client: Client, page: Int, size: Int): List<MovieEvent> {
        val xCorrelationId = UUID.randomUUID().toString()
        logger.info("Requesting media events with x-correlation-id: $xCorrelationId")

        val movieInfoJson = getMovieEventJson(client, xCorrelationId, page, size)
        return movieInfoJson.map { obj: JSONArray -> JSONtoMovieMapper.jsonArrayToMovieEventList(obj) }.orElseGet{ ArrayList() }
    }

    private fun getMovieInfoJson(client: Client, movieRequest: MovieRequest, xCorrelationId: String): Optional<JSONArray> {
        var urlConnection: HttpURLConnection? = null
        val url = client.computerUrl + "/localmovie/v2/media"
        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "POST"
            urlConnection.setRequestProperty(X_CORRELATION_ID, xCorrelationId)
            urlConnection.doOutput = true
            urlConnection.doInput = true
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Authorization", "bearer " + client.accessToken)
            urlConnection.connectTimeout = 10000
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Failed connecting to media info service", e)
        }
        if (urlConnection != null) {
            val movieRequestBody = GSON.toJson(movieRequest)
            try {
                OutputStreamWriter(urlConnection.outputStream, StandardCharsets.UTF_8).use { outputStream -> outputStream.write(movieRequestBody) }
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "Failed writing to media info service", e)
            }
            if (movieRequest.page == 0) {
                logger.fine("Reading page count")
                client.movieCount = Integer.valueOf(urlConnection.getHeaderField("Count"))
            }
            val result = StringBuilder()
            try {
                BufferedReader(InputStreamReader(urlConnection.inputStream)).use { br -> br.lines().forEachOrdered { str: String? -> result.append(str) } }
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "Failed reading from media info service", e)
            } finally {
                urlConnection.disconnect()
            }
            try {
                return Optional.of(JSONArray(result.toString()))
            } catch (e: JSONException) {
                logger.log(Level.SEVERE, "Failure unmarhalling json.", e)
            }
        }

        return Optional.empty()
    }

    @JvmStatic
    fun getMovieEventCount(client: Client): Optional<Long> {
        val xCorrelationId = UUID.randomUUID().toString()
        logger.info("Requesting media event count with x-correlation-id: $xCorrelationId")
        var urlConnection: HttpURLConnection? = null
        if (client.lastUpdate == null) {
            client.lastUpdate = System.currentTimeMillis()
        }
        val url = (client.computerUrl
                + "/localmovie/v2/media/events/count?timestamp=" + client.lastUpdate)
        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty(X_CORRELATION_ID, xCorrelationId)
            urlConnection.setRequestProperty("Authorization", "bearer " + client.accessToken)
            urlConnection.connectTimeout = 10000
            return Optional.of(urlConnection.getHeaderField(COUNT_HEADER).toLong())
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Failed connecting to media info service", e)
        } finally {
            urlConnection?.disconnect()
        }
        return Optional.empty()
    }

    private fun getMovieEventJson(client: Client, xCorrelationId: String, page: Int, size: Int): Optional<JSONArray> {
        var urlConnection: HttpURLConnection? = null
        if (client.lastUpdate == null) {
            client.lastUpdate = System.currentTimeMillis()
        }

        val url = (client.computerUrl
                + "/localmovie/v2/media/events?timestamp=" + client.lastUpdate
                + "&page=" + page
                + "&size=" + size)

        try {
            urlConnection = URL(url).openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.setRequestProperty(X_CORRELATION_ID, xCorrelationId)
            urlConnection.setRequestProperty("Authorization", "bearer " + client.accessToken)
            urlConnection.connectTimeout = 10000
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Failed connecting to media info service", e)
        }

        if (urlConnection != null) {
            val result = StringBuilder()
            try {
                BufferedReader(InputStreamReader(urlConnection.inputStream)).use { br ->
                    br.lines().forEachOrdered { str: String? -> result.append(str) }
                }
            } catch (e: IOException) {
                logger.log(Level.SEVERE, "Failed reading from media info service", e)
            } finally {
                urlConnection.disconnect()
            }
            try {
                return Optional.of(JSONArray(result.toString()))
            } catch (e: JSONException) {
                logger.log(Level.SEVERE, "Failure unmarshalling json.", e)
            }
        }

        return Optional.empty()
    }
}