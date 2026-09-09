package com.devil.finaldestiny.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAndArchiveScreen(
    userSettings: UserSettingsState,
    onUpdateSettings: (UserSettingsState) -> Unit,
    onClearSearchHistory: () -> Unit,
    onRequestDataExport: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Saved, 1: Archive Vault, 2: Activity Log
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderCategory by remember { mutableStateOf("POSTS") }

    val tabs = listOf("Saved Collections", "Archive Vault", "Your Activity")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Saved & Activity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TAB ROW
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF3897F0)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTab == index),
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: SAVED COLLECTIONS (3x3 Grid / Folders)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("All Saved Collections", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Button(
                                onClick = { showCreateFolderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Folder", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(userSettings.savedCollections) { folder ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F2F7)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clickable {
                                            Toast.makeText(context, "Opening collection: ${folder.name}", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFE5E5EA))
                                        ) {
                                            Icon(
                                                when (folder.category) {
                                                    "REELS" -> Icons.Default.Videocam
                                                    "AUDIO" -> Icons.Default.MusicNote
                                                    else -> Icons.Default.Bookmark
                                                },
                                                contentDescription = null,
                                                tint = Color(0xFF3897F0),
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(folder.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
                                        Text("${folder.itemCount} items • ${folder.category}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: ARCHIVE VAULT (Stories & Live archives, 30-day auto-purge countdown)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text("Story & Live Archive Vault", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Only you can see archived items. Items in vault auto-purge after 30 days.", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        userSettings.archiveItems.forEach { item ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF006494))
                                    ) {
                                        Icon(
                                            if (item.type == "LIVE") Icons.Default.Videocam else Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("Archived on: ${item.dateLabel} • ${item.daysRemaining} days left", fontSize = 11.sp, color = Color(0xFFE65100))
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                Toast.makeText(context, "✅ Restored ${item.title} to profile!", Toast.LENGTH_SHORT).show()
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Restore", fontSize = 10.sp)
                                        }
                                        Button(
                                            onClick = {
                                                val updated = userSettings.archiveItems.filter { it.id != item.id }
                                                onUpdateSettings(userSettings.copy(archiveItems = updated))
                                                Toast.makeText(context, "Permanently deleted", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Delete", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: YOUR ACTIVITY LOG & DOWNLOAD DATA EXPORT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Activity Log & History", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            OutlinedButton(
                                onClick = onClearSearchHistory,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Clear History", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        userSettings.activityLogs.forEach { log ->
                            ListItem(
                                headlineContent = { Text(log.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text("${log.subtitle} • ${log.timestamp}", fontSize = 11.sp, color = Color.Gray) },
                                leadingContent = {
                                    Icon(
                                        when (log.category) {
                                            "WATCH" -> Icons.Default.PlayCircle
                                            "SEARCH" -> Icons.Default.Search
                                            "LIKE" -> Icons.Default.Favorite
                                            else -> Icons.Default.History
                                        },
                                        contentDescription = null,
                                        tint = Color(0xFF3897F0)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

                        // DOWNLOAD DATA EXPORT TRIGGER
                        Text(
                            "Download Your Information",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        Text(
                            "Request a download of your posts, comments, profile information, and activity logs as an asynchronous export file.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestDataExport,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3897F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Data Export", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // CREATE FOLDER DIALOG
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create Saved Collection Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("POSTS", "REELS", "AUDIO").forEach { cat ->
                            FilterChip(
                                selected = (newFolderCategory == cat),
                                onClick = { newFolderCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            val newFolder = SavedCollectionFolder(
                                id = "sc_${System.currentTimeMillis()}",
                                name = newFolderName.trim(),
                                category = newFolderCategory,
                                itemCount = 0,
                                thumbnailUrls = emptyList()
                            )
                            onUpdateSettings(userSettings.copy(savedCollections = userSettings.savedCollections + newFolder))
                            showCreateFolderDialog = false
                            newFolderName = ""
                            Toast.makeText(context, "✅ Created collection folder!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
