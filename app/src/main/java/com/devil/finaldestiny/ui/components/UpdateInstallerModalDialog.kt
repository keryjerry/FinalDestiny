package com.devil.finaldestiny.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devil.finaldestiny.engine.UpdateReleaseInfo
import com.devil.finaldestiny.ui.theme.*

@Composable
fun UpdateInstallerModalDialog(
    updateInfo: UpdateReleaseInfo,
    downloadProgress: Int,
    isDownloading: Boolean,
    onStartDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!updateInfo.isMandatory && !isDownloading) onDismiss() },
        containerColor = SkyBlueCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚀 Update Required / New Features Available", color = NavyTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SkyBlueHeader)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(updateInfo.versionName, fontSize = 10.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Improvements in this update:", fontSize = 12.sp, color = NavyTextPrimary, fontWeight = FontWeight.Bold)

                Card(
                    colors = CardDefaults.cardColors(containerColor = SkyBlueBgLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, SkyBlueBorder, RoundedCornerShape(12.dp)).padding(8.dp)
                ) {
                    Text(updateInfo.changelog, fontSize = 11.sp, color = NavyTextPrimary.copy(0.9f), modifier = Modifier.padding(6.dp))
                }

                if (isDownloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Downloading Update Package...", fontSize = 11.sp, color = SlateTextSecondary)
                            Text("$downloadProgress%", fontSize = 11.sp, color = SkyBluePrimary, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            color = SkyBluePrimary,
                            trackColor = SkyBlueBorder,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onStartDownload,
                enabled = !isDownloading,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
            ) {
                Text(
                    text = if (isDownloading) "Downloading... ⏳" else "Update Now",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!updateInfo.isMandatory && !isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = SlateTextSecondary)
                }
            }
        }
    )
}
