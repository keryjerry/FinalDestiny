package com.devil.finaldestiny.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GlobalLocationRepository {

    suspend fun detectCurrentLocation(): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://ip-api.com/json")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "FinalDestinyApp/1.0")
            }
            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val city = json.optString("city", "")
                val region = json.optString("regionName", "")
                val country = json.optString("country", "")
                if (city.isNotBlank()) {
                    return@withContext if (region.isNotBlank()) "$city, $region" else "$city, $country"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        "New Delhi, India"
    }

    suspend fun searchGlobalLocations(query: String): List<String> = withContext(Dispatchers.IO) {
        val locations = mutableListOf<String>()
        if (query.isBlank()) return@withContext emptyList()

        try {
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=10"
            val url = URL(urlString)

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "FinalDestinyApp/1.0")
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(responseText)
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val displayName = item.optString("display_name", "")
                    if (displayName.isNotBlank()) {
                        val shortName = displayName.split(",").take(3).joinToString(",").trim()
                        locations.add(shortName)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (locations.isEmpty()) {
            locations.addAll(
                listOf(
                    "$query City Center",
                    "$query Downtown",
                    "$query Central Avenue",
                    "$query High Street"
                )
            )
        }

        locations.distinct()
    }

    suspend fun generateLocationChipsForCity(cityStr: String): List<String> = withContext(Dispatchers.IO) {
        val baseCity = cityStr.split(",").firstOrNull()?.trim() ?: cityStr
        listOf(
            baseCity,
            "$baseCity City Center",
            "Central $baseCity",
            "Downtown $baseCity"
        )
    }
}
