package com.devil.finaldestiny.engine

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import com.devil.finaldestiny.BuildConfig
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateReleaseInfo(
    val versionCode: Int,
    val versionName: String,
    val apkDownloadUrl: String,
    val changelog: String,
    val isMandatory: Boolean = false,
    val isUpdateAvailable: Boolean = true
) {
    val latestVersionCode: Int get() = versionCode
    val apkUrl: String get() = apkDownloadUrl
    val releaseNotes: String get() = changelog
    val isForceUpdate: Boolean get() = isMandatory
}

object AppInstallerEngine {
    private const val TAG = "AppInstallerEngine"

    suspend fun checkSupabaseAppVersion(
        currentVersionCode: Int = 1
    ): UpdateReleaseInfo? = withContext(Dispatchers.IO) {
        val supabaseUrl = "https://twwezpogwtmjavoemdvi.supabase.co"
        val anonKey = "sb_publishable_RiDqsSCPGbWGxd6570P1FA_y_L10U_w"

        var fetchedCode = 0
        var minSupported = 1
        var downloadUrl = "https://github.com/keryjerry/FinalDestiny/releases/latest/download/Final.Destiny.apk"
        var releaseNotes = "• New features and performance improvements available!"
        var isForce = false

        val endpoints = listOf(
            "$supabaseUrl/rest/v1/app_config?select=key,value",
            "$supabaseUrl/rest/v1/app_version_config?select=*&limit=1"
        )

        for (endpoint in endpoints) {
            try {
                val url = URL(endpoint)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                    setRequestProperty("apikey", anonKey)
                    setRequestProperty("Authorization", "Bearer $anonKey")
                    setRequestProperty("Accept", "application/json")
                }

                if (connection.responseCode in 200..299) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.e("UPDATE_FORCE_CHECK", "Raw JSON from Supabase: " + jsonString)
                    val jsonArray = org.json.JSONArray(jsonString)
                    if (jsonArray.length() > 0) {
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            if (obj.has("key") && obj.has("value")) {
                                val k = obj.optString("key")
                                val v = obj.optString("value")
                                when (k) {
                                    "latest_version_code" -> fetchedCode = v.toIntOrNull() ?: 0
                                    "latest_apk_url", "apk_download_url" -> downloadUrl = v
                                    "release_notes", "changelog" -> releaseNotes = v
                                    "force_update" -> isForce = v.toBoolean()
                                }
                            } else {
                                minSupported = obj.optInt("min_supported_version", obj.optInt("min_version", 1))
                                if (obj.has("latest_version_code")) fetchedCode = obj.optInt("latest_version_code", 2)
                                else if (obj.has("latest_version")) fetchedCode = obj.optInt("latest_version", 2)
                                if (obj.has("apk_download_url")) downloadUrl = obj.optString("apk_download_url", downloadUrl)
                                if (obj.has("changelog")) releaseNotes = obj.optString("changelog", releaseNotes)
                            }
                        }
                        if (fetchedCode > 0) break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking endpoint $endpoint", e)
            }
        }

        Log.e("UPDATE_FORCE_CHECK", "Parsed fetchedCode: $fetchedCode vs Local: ${BuildConfig.VERSION_CODE}")
        Log.d("APP_UPDATE_DEBUG", "Local: ${BuildConfig.VERSION_CODE}")
        Log.d("APP_UPDATE_DEBUG", "Remote: $fetchedCode")
        Log.d("APP_UPDATE_DEBUG", "Comparison condition met: ${fetchedCode > BuildConfig.VERSION_CODE}")

        val effectiveLocalCode = if (currentVersionCode > 0) currentVersionCode else BuildConfig.VERSION_CODE
        if (fetchedCode > 0) {
            return@withContext UpdateReleaseInfo(
                versionCode = fetchedCode,
                versionName = "v$fetchedCode.0",
                apkDownloadUrl = downloadUrl,
                changelog = releaseNotes,
                isMandatory = isForce || (effectiveLocalCode < minSupported || BuildConfig.VERSION_CODE < minSupported),
                isUpdateAvailable = (fetchedCode > BuildConfig.VERSION_CODE || fetchedCode > effectiveLocalCode)
            )
        }

        return@withContext null
    }

    /**
     * Triggers Android DownloadManager to genuinely download APK directly from GitHub releases or remote URL.
     * Follows HTTP 302/301 redirects to resolve final S3 binary download URL.
     */
    fun startDownload(context: Context, downloadUrl: String): Long {
        try {
            var finalUrl = downloadUrl
            try {
                var connection = URL(finalUrl).openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 6000
                connection.readTimeout = 6000
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == 307 || responseCode == 308) {
                    val redirectLocation = connection.getHeaderField("Location")
                    if (!redirectLocation.isNullOrEmpty()) {
                        finalUrl = redirectLocation
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Redirect resolution warning, using original URL", e)
            }

            val request = DownloadManager.Request(Uri.parse(finalUrl)).apply {
                setTitle("Final Destiny Update")
                setDescription("Downloading latest version v2.0...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                try {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FinalDestiny_Update.apk")
                } catch (e: Exception) {
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "FinalDestiny_Update.apk")
                }
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setMimeType("application/vnd.android.package-archive")
            }
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            Log.d(TAG, "Enqueueing DownloadManager request for URL: $finalUrl")
            return downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e(TAG, "DownloadManager failed, falling back to external browser", e)
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Log.e(TAG, "Browser fallback also failed", ex)
            }
            return -1L
        }
    }

    fun canInstallUnknownApps(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun promptPackageInstall(context: Context, apkFile: File): Boolean {
        try {
            if (!canInstallUnknownApps(context)) {
                openInstallPermissionSettings(context)
                return false
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prompt package install", e)
            return false
        }
    }
}

class ApkDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId != -1L) {
                try {
                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor != null && cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (statusIndex != -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                            val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                            var apkFile: File? = null
                            if (uriIndex != -1) {
                                val uriString = cursor.getString(uriIndex)
                                if (!uriString.isNullOrBlank()) {
                                    val uri = Uri.parse(uriString)
                                    val path = uri.path
                                    if (path != null && File(path).exists()) {
                                        apkFile = File(path)
                                    }
                                }
                            }
                            if (apkFile == null) {
                                val publicFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FinalDestiny_Update.apk")
                                if (publicFile.exists()) apkFile = publicFile
                            }
                            if (apkFile == null) {
                                val privateFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "FinalDestiny_Update.apk")
                                if (privateFile.exists()) apkFile = privateFile
                            }
                            cursor.close()
                            if (apkFile != null && apkFile.exists()) {
                                Log.d("ApkDownloadReceiver", "Download complete & verified successful. Prompting install for: ${apkFile.absolutePath}")
                                AppInstallerEngine.promptPackageInstall(context, apkFile)
                            }
                        } else {
                            cursor.close()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ApkDownloadReceiver", "Error processing download completion", e)
                }
            }
        }
    }
}
