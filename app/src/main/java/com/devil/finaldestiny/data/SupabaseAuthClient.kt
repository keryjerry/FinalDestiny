package com.devil.finaldestiny.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(val uid: String, val email: String, val token: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Configured Real Supabase Auth Client for Final Destiny Application.
 * Communicates directly with the remote Supabase GoTrue Auth API.
 */
object SupabaseAuthClient {
    var supabaseUrl: String = "https://twwezpogwtmjavoemdvi.supabase.co"
    var supabaseAnonKey: String = "sb_publishable_RiDqsSCPGbWGxd6570P1FA_y_L10U_w"

    private var currentSessionToken: String? = null
    private var currentUserEmail: String? = null
    private var currentUserId: String? = null

    var isAuthenticated: Boolean = false
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("destiny_auth_prefs", Context.MODE_PRIVATE)
        val token = prefs.getString("session_token", null)
        val uid = prefs.getString("unique_user_id", null)
        val email = prefs.getString("user_email", null)

        if (!token.isNull_or_blank_custom() && !uid.isNull_or_blank_custom()) {
            currentSessionToken = token
            currentUserId = uid
            currentUserEmail = email
            isAuthenticated = true
        }
    }

    /**
     * Real Google OAuth 2.0 Sign-In launcher via Supabase Authorize endpoint
     */
    fun signInWithGoogle(
        context: Context,
        onResult: (isSuccess: Boolean, errorMessage: String?) -> Unit
    ) {
        try {
            val targetUrl = "${supabaseUrl.trimEnd('/')}/auth/v1/authorize?provider=google"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onResult(false, "Redirecting to Google Sign-In in browser...")
        } catch (e: Exception) {
            onResult(false, "Failed to launch Google Sign-In: ${e.localizedMessage}")
        }
    }

    /**
     * Real Supabase GoTrue Password Authentication Call
     * Endpoint: POST /auth/v1/token?grant_type=password
     */
    suspend fun signInWithEmail(
        context: Context,
        email: String,
        pass: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            return@withContext AuthResult.Error("Please enter valid Email and Password.")
        }
        if (!cleanEmail.contains("@") || pass.length < 4) {
            return@withContext AuthResult.Error("Invalid email format or password too short (min 4 characters).")
        }

        try {
            val endpoint = "${supabaseUrl.trimEnd('/')}/auth/v1/token?grant_type=password"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("apikey", supabaseAnonKey)
                setRequestProperty("Authorization", "Bearer $supabaseAnonKey")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", pass)
            }

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (statusCode in 200..299) {
                val json = JSONObject(responseText)
                val token = json.optString("access_token", "sb-token-${System.currentTimeMillis()}")
                val userObj = json.optJSONObject("user")
                val uid = userObj?.optString("id") ?: json.optString("id", "")

                if (uid.isBlank()) {
                    return@withContext AuthResult.Error("Invalid response from Supabase Auth server: Missing User UUID.")
                }

                saveAuthSession(context, uid, cleanEmail, token)
                syncSupabaseProfile(uid, cleanEmail, null)

                return@withContext AuthResult.Success(uid = uid, email = cleanEmail, token = token)
            } else {
                val errMsg = parseSupabaseError(responseText, statusCode)
                return@withContext AuthResult.Error(errMsg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext AuthResult.Error("Network error connecting to Supabase Auth: ${e.localizedMessage}")
        }
    }

    /**
     * Real Supabase GoTrue Registration Call
     * Endpoint: POST /auth/v1/signup
     */
    suspend fun signUpWithEmail(
        context: Context,
        name: String,
        email: String,
        pass: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            return@withContext AuthResult.Error("Please enter valid Email and Password.")
        }
        if (!cleanEmail.contains("@") || pass.length < 4) {
            return@withContext AuthResult.Error("Invalid email format or password too short (min 4 characters).")
        }

        try {
            val endpoint = "${supabaseUrl.trimEnd('/')}/auth/v1/signup"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("apikey", supabaseAnonKey)
                setRequestProperty("Authorization", "Bearer $supabaseAnonKey")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("email", cleanEmail)
                put("password", pass)
                put("data", JSONObject().apply {
                    put("name", if (cleanName.isNotBlank()) cleanName else cleanEmail.substringBefore("@"))
                })
            }

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

            if (statusCode in 200..299) {
                val json = JSONObject(responseText)
                val userObj = json.optJSONObject("user")
                val uid = userObj?.optString("id") ?: json.optString("id", "")
                val token = json.optString("access_token", "sb-token-${System.currentTimeMillis()}")

                if (uid.isNotBlank()) {
                    saveAuthSession(context, uid, cleanEmail, token)
                    syncSupabaseProfile(uid, cleanEmail, cleanName)
                    return@withContext AuthResult.Success(uid = uid, email = cleanEmail, token = token)
                } else {
                    // Supabase created user but email confirmation is pending
                    return@withContext AuthResult.Error("Sign Up submitted. If email confirmation is required, please check your inbox.")
                }
            } else {
                val errMsg = parseSupabaseError(responseText, statusCode)
                return@withContext AuthResult.Error(errMsg)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext AuthResult.Error("Network error connecting to Supabase Auth: ${e.localizedMessage}")
        }
    }

    private fun parseSupabaseError(responseText: String, statusCode: Int): String {
        try {
            if (responseText.isNotBlank()) {
                val json = JSONObject(responseText)
                val msg = json.optString("msg")
                    .ifBlank { json.optString("error_description") }
                    .ifBlank { json.optString("message") }
                    .ifBlank { json.optString("error") }
                if (msg.isNotBlank()) return msg
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return when (statusCode) {
            400 -> "Invalid login credentials or password format."
            401 -> "Invalid credentials or unauthorized request."
            422 -> "User already exists or email is already registered."
            else -> "Supabase Auth HTTP Error ($statusCode)"
        }
    }

    private fun saveAuthSession(context: Context, uid: String, email: String, token: String) {
        currentUserId = uid
        currentUserEmail = email
        currentSessionToken = token
        isAuthenticated = true

        val prefs = context.getSharedPreferences("destiny_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("unique_user_id", uid)
            .putString("user_email", email)
            .putString("session_token", token)
            .apply()
    }

    private fun syncSupabaseProfile(uid: String, email: String, name: String?) {
        try {
            val endpoint = "${supabaseUrl.trimEnd('/')}/rest/v1/profiles"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("apikey", supabaseAnonKey)
                setRequestProperty("Authorization", "Bearer ${currentSessionToken ?: supabaseAnonKey}")
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Prefer", "return=representation,resolution=merge-duplicates")
                doOutput = true
            }

            val handle = "@" + email.substringBefore("@").replace(" ", "_")
            val payload = JSONObject().apply {
                put("id", uid)
                put("email", email)
                put("name", if (!name.isNull_or_blank_custom()) name else email.substringBefore("@"))
                put("handle", handle)
            }

            connection.outputStream.use { os ->
                os.write(payload.toString().toByteArray(Charsets.UTF_8))
            }
            connection.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getOrCreateUserId(context: Context): String {
        if (!currentUserId.isNull_or_blank_custom()) return currentUserId!!
        val prefs = context.getSharedPreferences("destiny_auth_prefs", Context.MODE_PRIVATE)
        var storedId = prefs.getString("unique_user_id", null)
        if (storedId.isNull_or_blank_custom()) {
            val randomId = "u_" + java.util.UUID.randomUUID().toString().replace("-", "").take(8)
            prefs.edit().putString("unique_user_id", randomId).apply()
            storedId = randomId
        }
        currentUserId = storedId
        return storedId!!
    }

    fun getSessionToken(): String? = currentSessionToken
    fun getUserEmail(): String? = currentUserEmail
    fun getUserId(): String? = currentUserId

    fun signOut(context: Context? = null) {
        currentSessionToken = null
        currentUserEmail = null
        currentUserId = null
        isAuthenticated = false
        if (context != null) {
            val prefs = context.getSharedPreferences("destiny_auth_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .remove("unique_user_id")
                .remove("user_email")
                .remove("session_token")
                .apply()
        }
    }

    private fun String?.isNull_or_blank_custom(): Boolean = this == null || this.trim().isEmpty()
}
