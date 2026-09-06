package com.devil.finaldestiny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.ui.theme.*

@Composable
fun PermissionModalDialog(
    onAcceptPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = MetallicGold, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Granular Privacy & OS Permissions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MetallicGold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Final Destiny requires explicit permission for secure social matching & live broadcasting:",
                    fontSize = 13.sp,
                    color = LightGold.copy(alpha = 0.9f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LightGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera Access: For biometric selfie liveness check & video live streaming.", fontSize = 12.sp, color = LightGold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = LightGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Microphone Access: For 10-Mic sofa room voice broadcasting & 1v1 calls.", fontSize = 12.sp, color = LightGold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = LightGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Storage Access: For playing local MP3 audio tracks via Jukebox & profile photos.", fontSize = 12.sp, color = LightGold)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🔒 Zero Unsolicited Calling Guarantee: 1-on-1 calls require formal in-chat invitation acceptance.",
                    fontSize = 11.sp,
                    color = MetallicGold,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAcceptPermissions,
                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Accept & Grant Access", color = WineRedDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = LightGold.copy(alpha = 0.7f))
            }
        }
    )
}
