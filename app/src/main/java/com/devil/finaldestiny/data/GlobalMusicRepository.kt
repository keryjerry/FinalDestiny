package com.devil.finaldestiny.data

import com.devil.finaldestiny.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object GlobalMusicRepository {

    suspend fun searchGlobalMusic(query: String): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioTrack>()
        val searchTerm = if (query.isBlank()) "trending bollywood" else query.trim()
        
        try {
            val encodedQuery = URLEncoder.encode(searchTerm, "UTF-8")
            val urlString = "https://itunes.apple.com/search?term=$encodedQuery&media=music&entity=song&limit=30"
            val url = URL(urlString)
            
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "FinalDestinyAndroidApp/1.0")
            }
            
            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(responseText)
                val resultsArray = jsonObject.getJSONArray("results")
                
                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    val trackId = item.optString("trackId", "tr_$i")
                    val trackName = item.optString("trackName", "Unknown Track")
                    val artistName = item.optString("artistName", "Unknown Artist")
                    val artworkUrl = item.optString("artworkUrl100", item.optString("artworkUrl60", ""))
                    val previewUrl = item.optString("previewUrl", "")
                    val millis = item.optLong("trackTimeMillis", 30000L)
                    val seconds = (millis / 1000) % 60
                    val minutes = (millis / 1000) / 60
                    val durationStr = String.format("%d:%02d", minutes, seconds)
                    
                    tracks.add(
                        AudioTrack(
                            id = trackId,
                            title = trackName,
                            artist = artistName,
                            albumCoverUrl = artworkUrl,
                            audioUrl = previewUrl,
                            duration = durationStr
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback trending music list if network fails
        if (tracks.isEmpty()) {
            tracks.addAll(
                listOf(
                    AudioTrack("tr_f1", "Ye Meera Deewanapan", "Susheela Raman", "https://picsum.photos/200/200?random=801", duration = "0:30"),
                    AudioTrack("tr_f2", "Kesariya", "Arijit Singh", "https://picsum.photos/200/200?random=802", duration = "0:30"),
                    AudioTrack("tr_f3", "Pasoori", "Ali Sethi & Shae Gill", "https://picsum.photos/200/200?random=803", duration = "0:30"),
                    AudioTrack("tr_f4", "Velvet Hour", "Thunder", "https://picsum.photos/200/200?random=804", duration = "0:30"),
                    AudioTrack("tr_f5", "Living My Life", "Aarav Sharma", "https://picsum.photos/200/200?random=805", duration = "0:30")
                )
            )
        }
        
        tracks
    }
}
