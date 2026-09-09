package com.devil.finaldestiny.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramMoreOptionsScreen(
    isScheduled: Boolean,
    scheduledDateTimeStr: String?,
    onScheduleToggle: (Boolean) -> Unit,
    isCommentsDisabled: Boolean,
    onCommentsDisabledToggle: (Boolean) -> Unit,
    isHideLikesEnabled: Boolean,
    onHideLikesToggle: (Boolean) -> Unit,
    isHideSharesEnabled: Boolean,
    onHideSharesToggle: (Boolean) -> Unit,
    isFacebookAutoShare: Boolean,
    onFacebookAutoShareToggle: (Boolean) -> Unit,
    altTextValue: String,
    onOpenAltText: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showAltDialog by remember { mutableStateOf(false) }
    var currentAltInput by remember { mutableStateOf(altTextValue) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "More options",
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
                .verticalScroll(rememberScrollState())
        ) {
            // SECTION 1: SHARING PREFERENCES
            Text(
                "Sharing preferences",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
            )

            ListItem(
                headlineContent = { Text("Schedule this post", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        if (scheduledDateTimeStr != null) "Scheduled for: $scheduledDateTimeStr" else "Some features may be unavailable with scheduled content.",
                        fontSize = 11.sp,
                        color = if (scheduledDateTimeStr != null) Color(0xFF3897F0) else Color(0xFF8E8E93)
                    )
                },
                leadingContent = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Switch(
                        checked = isScheduled,
                        onCheckedChange = onScheduleToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                    )
                }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // SECTION 2: HOW OTHERS CAN INTERACT WITH YOUR POST
            Text(
                "How others can interact with your post",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
            )

            // Turn off commenting
            ListItem(
                headlineContent = { Text("Turn off commenting", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                leadingContent = { Icon(Icons.Default.CommentsDisabled, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Switch(
                        checked = isCommentsDisabled,
                        onCheckedChange = onCommentsDisabledToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                    )
                }
            )

            // Hide like count on this post
            ListItem(
                headlineContent = { Text("Hide like count on this post", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        "Only you will see the total number of likes and views on this post. You can change this later by going to the ••• menu at the top of the post. Learn more",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                },
                leadingContent = { Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Switch(
                        checked = isHideLikesEnabled,
                        onCheckedChange = onHideLikesToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                    )
                }
            )

            // Hide share count on this post
            ListItem(
                headlineContent = { Text("Hide share count on this post", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        "Only you will see the number of likes and shares on this post. You can change this later by going to the ••• menu at the top of the post. You can hide the number of likes and shares on this post from other accounts by going to your account settings. Learn more",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                },
                leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Switch(
                        checked = isHideSharesEnabled,
                        onCheckedChange = onHideSharesToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                    )
                }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // SECTION 3: AUTOMATIC SHARING
            Text(
                "Automatic sharing",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
            )

            // Facebook
            ListItem(
                headlineContent = { Text("Facebook", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        "Automatically share your photo and video posts to Facebook.",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                },
                leadingContent = { Icon(Icons.Default.Public, contentDescription = null, tint = Color.Black) },
                trailingContent = {
                    Switch(
                        checked = isFacebookAutoShare,
                        onCheckedChange = onFacebookAutoShareToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3897F0))
                    )
                }
            )

            HorizontalDivider(color = Color(0xFFE5E5EA), thickness = 0.5.dp)

            // SECTION 4: ACCESSIBILITY AND TRANSLATION
            Text(
                "Accessibility and translation",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
            )

            // Write alt text
            ListItem(
                headlineContent = { Text("Write alt text", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        "Alt text describes your photos for people with visual impairments. Alt text will be created automatically for your photos or you can choose to write your own.",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )
                },
                leadingContent = { Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.Black) },
                trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF8E8E93)) },
                modifier = Modifier.clickable { showAltDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAltDialog) {
        AlertDialog(
            onDismissRequest = { showAltDialog = false },
            title = { Text("Write Alt Text", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = currentAltInput,
                    onValueChange = { currentAltInput = it },
                    label = { Text("Alt Text Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAltDialog = false
                        Toast.makeText(context, "Saved Alt Text", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save")
                }
            }
        )
    }
}
