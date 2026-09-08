package com.devil.finaldestiny.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.model.Gender
import com.devil.finaldestiny.ui.theme.*

@Composable
fun ProfileAvatarView(
    name: String,
    profilePictureUri: String?,
    gender: Gender = Gender.FEMALE,
    size: Dp = 50.dp,
    showBorder: Boolean = true,
    borderColor: Color = MetallicGold,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loadedBitmap = rememberLoadedAvatarImage(context, profilePictureUri)

    val initial = name.trim().take(1).uppercase().ifEmpty { "U" }
    val avatarEmoji = when (gender) {
        Gender.FEMALE -> if (name.length % 2 == 0) "👩‍🦰" else "👩‍🦱"
        Gender.MALE -> if (name.length % 2 == 0) "👨‍🦱" else "👨‍🦰"
        else -> "✨"
    }

    val bgGradient = if (gender == Gender.FEMALE) {
        Brush.linearGradient(listOf(WineRedMedium, CrimsonVelvet))
    } else {
        Brush.linearGradient(listOf(WineRedDark, WineRedMedium))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgGradient)
            .then(
                if (showBorder) Modifier.border(2.dp, borderColor, CircleShape)
                else Modifier
            )
    ) {
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "$initial $avatarEmoji",
                    fontSize = (size.value * 0.35f).sp,
                    fontWeight = FontWeight.Bold,
                    color = LightGold
                )
            }
        }
    }
}

@Composable
fun rememberLoadedAvatarImage(context: Context, uriString: String?): ImageBitmap? {
    return remember(uriString) {
        if (uriString.isNullOrBlank()) null
        else {
            try {
                if (uriString.startsWith("content://")) {
                    val uri = Uri.parse(uriString)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    bitmap?.asImageBitmap()
                } else if (uriString.startsWith("file://")) {
                    val uri = Uri.parse(uriString)
                    val path = uri.path
                    if (path != null && java.io.File(path).exists()) {
                        BitmapFactory.decodeFile(path)?.asImageBitmap()
                    } else {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        bitmap?.asImageBitmap()
                    }
                } else if (uriString.startsWith("/") || uriString.contains(":\\")) {
                    if (java.io.File(uriString).exists()) {
                        BitmapFactory.decodeFile(uriString)?.asImageBitmap()
                    } else null
                } else {
                    try {
                        val uri = Uri.parse(uriString)
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()
                        bitmap?.asImageBitmap()
                    } catch (e: Exception) {
                        if (java.io.File(uriString).exists()) {
                            BitmapFactory.decodeFile(uriString)?.asImageBitmap()
                        } else null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
