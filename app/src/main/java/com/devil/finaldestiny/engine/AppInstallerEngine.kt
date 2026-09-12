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
    val isMandatory: Boolean = false
)

object AppInstallerEngine {
    private const val TAG = "AppInstallerEngine"

    suspend fun checkSupabaseAppVersion(
        currentVersionCode: Int = 1
    ): UpdateReleaseInfo? = withContext(Dispatchers.IO) {
        val supabaseUrl = "https://twwezpogwtmjavoemdvi.supabase.co"
        val anonKey = "sb_publishable_RiDqsSCPGbWGxd6570P1FA_y_L10U_w"
        val endpoint = "$supabaseUrl/rest/v1/app_version_config?select=*&limit=1"

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
                val jsonArray = org.json.JSONArray(jsonString)
                if (jsonArray.length() > 0) {
                    val obj = jsonArray.getJSONObject(0)
                    val minSupported = obj.optInt("min_supported_version", 1)
                    val fetchedCode = when {
                        obj.has("latest_version_code") -> obj.optInt("latest_version_code", 2)
                        else -> obj.optInt("latest_version", 2)
                    }
                    val downloadUrl = when {
                        obj.has("apk_download_url") -> obj.optString("apk_download_url", "https://github.com/keryjerry/FinalDestiny/releases/latest/download/Final.Destiny.apk")
                        else -> obj.optString("download_url", "https://github.com/keryjerry/FinalDestiny/releases/latest/download/Final.Destiny.apk")
                    }
                    val releaseNotes = when {
                        obj.has("changelog") -> obj.optString("changelog")
                        obj.has("release_notes") -> obj.optString("release_notes")
                        else -> "• New features and performance improvements available!"
                    }

                    Log.d("APP_UPDATE_DEBUG", "Local versionCode: ${BuildConfig.VERSION_CODE}")
                    Log.d("APP_UPDATE_DEBUG", "Supabase fetched latest_version_code: $fetchedCode")
                    Log.d("APP_UPDATE_DEBUG", "Comparison condition met: ${fetchedCode > BuildConfig.VERSION_CODE}")

                    val effectiveLocalCode = if (currentVersionCode > 0) currentVersionCode else BuildConfig.VERSION_CODE
                    if (fetchedCode > BuildConfig.VERSION_CODE || fetchedCode > effectiveLocalCode) {
                        return@withContext UpdateReleaseInfo(
                            versionCode = fetchedCode,
                            versionName = "v$fetchedCode.0",
                            apkDownloadUrl = downloadUrl,
                            changelog = releaseNotes,
                            isMandatory = (effectiveLocalCode < minSupported || BuildConfig.VERSION_CODE < minSupported)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Supabase app version", e)
        }

        return@withContext null
    }

    /**
     * Triggers Android DownloadManager to genuinely download APK directly from GitHub releases or remote URL.
     * Falls back to browser if DownloadManager fails or is blocked.
     */
    fun startDownload(context: Context, downloadUrl: String): Long {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Final Destiny Update")
                setDescription("Downloading latest version v2.0...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "Final.Destiny.apk")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
                setMimeType("application/vnd.android.package-archive")
            }
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            Log.d(TAG, "Enqueueing DownloadManager request for URL: $downloadUrl")
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
                            if (uriIndex != -1) {
                                val uriString = cursor.getString(uriIndex)
                                cursor.close()
                                if (!uriString.isNullOrBlank()) {
                                    val uri = Uri.parse(uriString)
                                    val path = uri.path
                                    if (path != null) {
                                        val apkFile = File(path)
                                        if (apkFile.exists()) {
                                            Log.d("ApkDownloadReceiver", "Download complete. Prompting install for: ${apkFile.absolutePath}")
                                            AppInstallerEngine.promptPackageInstall(context, apkFile)
                                        }
                                    }
                                }
                            } else {
                                cursor.close()
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
