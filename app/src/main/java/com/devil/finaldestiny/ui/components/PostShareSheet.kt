package com.devil.finaldestiny.ui.components

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.data.SupabaseAuthClient
import com.devil.finaldestiny.model.MomentPost
import com.devil.finaldestiny.model.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostShareSheet(
    post: MomentPost,
    currentUserId: String? = null,
    onDismiss: () -> Unit,
    onAddToStory: ((MomentPost) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val myUid = remember { currentUserId ?: SupabaseAuthClient.getCurrentUserId() ?: "" }

    var usersList by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var sentUserIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var sendingUserId by remember { mutableStateOf<String?>(null) }

    // Fetch live real users from Supabase DB on load
    LaunchedEffect(post.id) {
        isLoadingUsers = true
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val liveUsers = SupabaseAuthClient.fetchShareSheetUsers(myUid)
            usersList = liveUsers
            isLoadingUsers = false
        }
    }

    val filteredUsers = remember(usersList, searchQuery) {
        if (searchQuery.isBlank()) {
            usersList
        } else {
            usersList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.handle.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E2638), // Dark sleek navy background
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✈️ Send Post via Direct Message",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search friends...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2B364E),
                        unfocusedContainerColor = Color(0xFF2B364E),
                        focusedBorderColor = Color(0xFF3897F0),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )

                // 1. LIVE FRIENDS LIST (DM TARGETS)
                Text(
                    text = "SEND TO FRIENDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )

                if (isLoadingUsers) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF3897F0),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No users found for \"$searchQuery\"" else "No users found",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredUsers, key = { it.id }) { user ->
                            val isSent = sentUserIds.contains(user.id)
                            val isSending = sendingUserId == user.id

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF253046))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    ProfileAvatarView(
                                        name = user.name,
                                        profilePictureUri = user.profilePictureUri,
                                        size = 36.dp,
                                        showBorder = false
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = user.handle,
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (!isSent && !isSending) {
                                            sendingUserId = user.id
                                            coroutineScope.launch {
                                                val msgText = "Check out this post on Final Destiny 🚀\nhttps://finaldestiny.app/p/${post.id}\n${post.caption.take(80)}"
                                                val (ok, _) = SupabaseAuthClient.sendDirectMessageToSupabase(
                                                    context = context,
                                                    currentUserId = myUid,
                                                    targetUserId = user.id,
                                                    content = msgText
                                                )
                                                sendingUserId = null
                                                if (ok) {
                                                    sentUserIds = sentUserIds + user.id
                                                    Toast.makeText(context, "✈️ Sent post to ${user.name}!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "❌ Could not send message", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isSent && !isSending,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSent) Color(0xFF22C55E) else Color(0xFF3897F0),
                                        disabledContainerColor = Color(0xFF22C55E).copy(alpha = 0.8f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    if (isSending) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else if (isSent) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Text("Sent", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("Send", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF2B364E), thickness = 1.dp)

                // 2. HORIZONTAL SOCIAL SHARE ACTIONS ROW (INSTAGRAM STYLE)
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Action 1: Add to Story
                    ShareActionButton(
                        icon = Icons.Default.AddCircleOutline,
                        label = "Add to Story",
                        badgeBgColor = Color(0xFFEC4899), // Pink / Story accent
                        onClick = {
                            if (onAddToStory != null) {
                                onAddToStory(post)
                            } else {
                                Toast.makeText(context, "📸 Post added to your Story!", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        }
                    )

                    // Action 2: Copy Link
                    ShareActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Copy Link",
                        badgeBgColor = Color(0xFF6366F1), // Indigo
                        onClick = {
                            val shareUrl = "https://finaldestiny.app/p/${post.id}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Post Deep Link", shareUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "📋 Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    // Action 3: WhatsApp
                    ShareActionButton(
                        icon = Icons.Default.Message,
                        label = "WhatsApp",
                        badgeBgColor = Color(0xFF25D366), // WhatsApp Green
                        onClick = {
                            val shareUrl = "https://finaldestiny.app/p/${post.id}"
                            val text = "Check out this post by ${post.authorName} on Final Destiny: $shareUrl"
                            val waIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                setPackage("com.whatsapp")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            try {
                                context.startActivity(waIntent)
                            } catch (e: ActivityNotFoundException) {
                                val chooserIntent = Intent.createChooser(
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    },
                                    "Share Post via"
                                )
                                context.startActivity(chooserIntent)
                            }
                            onDismiss()
                        }
                    )

                    // Action 4: Share To... (System Chooser)
                    ShareActionButton(
                        icon = Icons.Default.Share,
                        label = "Share To...",
                        badgeBgColor = Color(0xFF3897F0), // Blue
                        onClick = {
                            val shareUrl = "https://finaldestiny.app/p/${post.id}"
                            val text = "Check out this post on Final Destiny: $shareUrl"
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            val chooserIntent = Intent.createChooser(sendIntent, "Share Post via")
                            context.startActivity(chooserIntent)
                            onDismiss()
                        }
                    )

                    // Action 5: Download Media
                    ShareActionButton(
                        icon = Icons.Default.FileDownload,
                        label = "Download",
                        badgeBgColor = Color(0xFF10B981), // Emerald Green
                        onClick = {
                            downloadPostMedia(context, post)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun ShareActionButton(
    icon: ImageVector,
    label: String,
    badgeBgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(badgeBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

private fun downloadPostMedia(context: Context, post: MomentPost) {
    val rawUrl = post.mediaUrl.ifBlank { post.mediaUri ?: "" }
    if (rawUrl.isBlank()) {
        Toast.makeText(context, "❌ No media link available to download", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val isVideo = post.mediaType.name.equals("VIDEO", ignoreCase = true) ||
                rawUrl.endsWith(".mp4", ignoreCase = true) ||
                rawUrl.contains("/videos/", ignoreCase = true)
        val ext = if (isVideo) "mp4" else "jpg"
        val fileName = "FD_Post_${post.id}_${System.currentTimeMillis()}.$ext"

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
        if (downloadManager != null && (rawUrl.startsWith("http://") || rawUrl.startsWith("https://"))) {
            val request = DownloadManager.Request(Uri.parse(rawUrl)).apply {
                setTitle("Final Destiny - ${if (isVideo) "Video" else "Photo"}")
                setDescription("Downloading post by ${post.authorName}")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(
                    if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES,
                    fileName
                )
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }
            downloadManager.enqueue(request)
            Toast.makeText(context, "📥 Downloading ${if (isVideo) "video" else "photo"} to Gallery...", Toast.LENGTH_SHORT).show()

            // Broadcast media scan
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.parse("file://${Environment.getExternalStoragePublicDirectory(if (isVideo) Environment.DIRECTORY_MOVIES else Environment.DIRECTORY_PICTURES)}/$fileName")
            }
            context.sendBroadcast(scanIntent)
        } else {
            Toast.makeText(context, "📋 Media URL saved to clipboard", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("PostShareSheet", "Error downloading media", e)
        Toast.makeText(context, "❌ Download error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
