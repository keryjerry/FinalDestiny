package com.devil.finaldestiny.engine

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
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

    private const val DEFAULT_VERSION_CHECK_ENDPOINT = "https://api.finaldestiny.app/api/app/version"

    suspend fun checkServerVersion(
        endpointUrl: String = DEFAULT_VERSION_CHECK_ENDPOINT,
        currentVersionCode: Int
    ): UpdateReleaseInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(endpointUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val serverVersionCode = json.optInt("versionCode", currentVersionCode)

                if (serverVersionCode > currentVersionCode) {
                    return@withContext UpdateReleaseInfo(
                        versionCode = serverVersionCode,
                        versionName = json.optString("versionName", "v1.1.0"),
                        apkDownloadUrl = json.optString("apkDownloadUrl", "https://finaldestiny.app/downloads/finalconnect-latest.apk"),
                        changelog = json.optString("changelog", "• Improved Live Audio/Video Room performance\n• Enhanced Tinder Swipe card responsiveness\n• Added Razorpay direct UPI payments & Aadhaar KYC"),
                        isMandatory = json.optBoolean("isMandatory", false)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun downloadApkFile(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.externalCacheDir ?: context.cacheDir
            val destinationFile = File(targetDir, "final_connect_update.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10000
            connection.readTimeout = 15000
            connection.connect()

            val fileLength = connection.contentLength
            val input = connection.inputStream
            val output = FileOutputStream(destinationFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()
            return@withContext destinationFile
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
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
            e.printStackTrace()
            return false
        }
    }
}
