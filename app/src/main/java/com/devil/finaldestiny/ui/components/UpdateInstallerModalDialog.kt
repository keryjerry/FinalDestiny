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
        containerColor = CardBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🚀 New Update Available!", color = MetallicGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(WineRedMedium)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(updateInfo.versionName, fontSize = 10.sp, color = LightGold, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("What's New in this Version:", fontSize = 12.sp, color = LightGold, fontWeight = FontWeight.Bold)

                Card(
                    colors = CardDefaults.cardColors(containerColor = WineRedDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, CrimsonVelvet, RoundedCornerShape(12.dp)).padding(8.dp)
                ) {
                    Text(updateInfo.changelog, fontSize = 11.sp, color = LightGold.copy(0.9f), modifier = Modifier.padding(6.dp))
                }

                if (isDownloading) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Downloading Update Package...", fontSize = 11.sp, color = LightGold)
                            Text("$downloadProgress%", fontSize = 11.sp, color = MetallicGold, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { downloadProgress / 100f },
                            color = MetallicGold,
                            trackColor = WineRedMedium,
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
                colors = ButtonDefaults.buttonColors(containerColor = MetallicGold)
            ) {
                Text(
                    text = if (isDownloading) "Downloading... ⏳" else "Download & Install 📦",
                    color = WineRedDark,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!updateInfo.isMandatory && !isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("Later", color = LightGold)
                }
            }
        }
    )
}
