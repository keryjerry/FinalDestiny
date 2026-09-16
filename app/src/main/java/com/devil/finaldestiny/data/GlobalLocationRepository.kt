package com.devil.finaldestiny.data

import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class LocationPlaceItem(
    val title: String,
    val subtitle: String
)

object GlobalLocationRepository {

    suspend fun detectCurrentLocationWithGpsDetailed(context: Context): LocationPlaceItem = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (provider in providers) {
                    try {
                        val loc = locationManager.getLastKnownLocation(provider) ?: continue
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }

                if (bestLocation != null && Geocoder.isPresent()) {
                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(bestLocation.latitude, bestLocation.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val address = addresses[0]
                            val locality = address.locality ?: address.subAdminArea ?: address.adminArea ?: "City"
                            val subLocality = address.subLocality ?: address.featureName ?: locality
                            val title = if (subLocality != locality) "$subLocality, $locality" else locality
                            val subtitle = listOfNotNull(address.adminArea, address.countryName).distinct().joinToString(", ")
                            return@withContext LocationPlaceItem(title = title, subtitle = if (subtitle.isNotBlank()) subtitle else "Current GPS Location")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val ipLoc = detectCurrentLocation()
        if (ipLoc.isNotBlank()) {
            val parts = ipLoc.split(",")
            LocationPlaceItem(
                title = parts.firstOrNull()?.trim() ?: ipLoc,
                subtitle = parts.drop(1).joinToString(", ").trim().ifBlank { "Detected Location" }
            )
        } else {
            LocationPlaceItem("Select Location", "Tap to search city or landmark")
        }
    }

    suspend fun detectCurrentLocationWithGps(context: Context): String = withContext(Dispatchers.IO) {
        val place = detectCurrentLocationWithGpsDetailed(context)
        place.title
    }

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
        ""
    }

    suspend fun searchGlobalLocationsDetailed(context: Context, query: String): List<LocationPlaceItem> = withContext(Dispatchers.IO) {
        val results = mutableListOf<LocationPlaceItem>()
        if (query.isBlank()) return@withContext emptyList()

        if (Geocoder.isPresent()) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query.trim(), 8)
                if (!addresses.isNullOrEmpty()) {
                    for (addr in addresses) {
                        val title = addr.featureName ?: addr.subLocality ?: addr.locality ?: query
                        val subtitleParts = listOfNotNull(addr.locality, addr.adminArea, addr.countryName).distinct()
                        val subtitle = if (subtitleParts.isNotEmpty()) subtitleParts.joinToString(", ") else "Location Result"
                        results.add(LocationPlaceItem(title = title, subtitle = subtitle))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (results.isEmpty()) {
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
                            val parts = displayName.split(",")
                            val title = parts.firstOrNull()?.trim() ?: query
                            val subtitle = parts.drop(1).take(2).joinToString(",").trim()
                            results.add(LocationPlaceItem(title = title, subtitle = subtitle.ifBlank { "Location" }))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (results.isEmpty()) {
            listOf(
                LocationPlaceItem("$query City Center", "Downtown Area"),
                LocationPlaceItem("Central $query", "Commercial District"),
                LocationPlaceItem("$query High Street", "Shopping & Financial Center"),
                LocationPlaceItem("$query Metro Station", "Transport Hub")
            )
        } else {
            results.distinctBy { it.title }
        }
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

    suspend fun fetchNearbyLandmarks(cityStr: String): List<LocationPlaceItem> = withContext(Dispatchers.IO) {
        val baseCity = cityStr.split(",").firstOrNull()?.trim()?.takeIf { it.isNotBlank() } ?: "City"
        listOf(
            LocationPlaceItem("$baseCity City Center", "Commercial Hub, $baseCity"),
            LocationPlaceItem("Central $baseCity", "Downtown District"),
            LocationPlaceItem("$baseCity Market Place", "Shopping & Retail Area"),
            LocationPlaceItem("$baseCity High Street", "Cultural & Business Center"),
            LocationPlaceItem("$baseCity Metro Junction", "Transit Terminal"),
            LocationPlaceItem("Sector 18, $baseCity", "Commercial Zone"),
            LocationPlaceItem("$baseCity IT Park", "Tech District")
        )
    }

    suspend fun generateLocationChipsForCity(cityStr: String): List<String> = withContext(Dispatchers.IO) {
        val baseCity = cityStr.split(",").firstOrNull()?.trim() ?: cityStr
        listOf(
            baseCity,
            "$baseCity City Center",
            "Central $baseCity",
            "$baseCity Market Place",
            "$baseCity High Street",
            "$baseCity Metro Station",
            "Downtown $baseCity",
            "Sector 18, $baseCity"
        ).distinct()
    }
}
