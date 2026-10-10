package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ApiKeyStore
import com.example.ui.theme.NetflixBlack
import com.example.ui.theme.NetflixCardElevated
import com.example.ui.theme.NetflixDarkSurface
import com.example.ui.theme.NetflixGreen
import com.example.ui.theme.NetflixRed
import com.example.ui.theme.NetflixSurfaceVariant
import com.example.ui.theme.NetflixTextMuted
import com.example.ui.theme.NetflixTextPrimary
import com.example.ui.theme.NetflixTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDrawer(
    isOpen: Boolean,
    currentKeyInput: String,
    isTesting: Boolean,
    validationStatus: String?,
    hasCustomKey: Boolean,
    onKeyInputChanged: (String) -> Unit,
    onSaveKey: (String) -> Unit,
    onUseDemoKey: () -> Unit,
    reportStore: com.example.data.report.ReportStore? = null,
    onTestTelegram: ((String, String, (Boolean, String) -> Unit) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showKeyVisible by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    // Telegram Configuration State
    var botTokenInput by remember { mutableStateOf(reportStore?.getTelegramBotToken() ?: "") }
    var chatIdInput by remember { mutableStateOf(reportStore?.getTelegramChatId() ?: "") }
    var isTelegramTesting by remember { mutableStateOf(false) }
    var telegramStatusMessage by remember { mutableStateOf<String?>(null) }
    var telegramStatusSuccess by remember { mutableStateOf<Boolean?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NetflixDarkSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF444444))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
                .testTag("settings_drawer_content")
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NetflixRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = NetflixRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TMDB Settings (BYOK)",
                            color = NetflixTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bring Your Own TMDB API Key",
                            color = NetflixTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NetflixTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Current Status Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NetflixCardElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasCustomKey) Icons.Default.CheckCircle else Icons.Default.Speed,
                        contentDescription = null,
                        tint = if (hasCustomKey) NetflixGreen else Color(0xFFFFB300),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (hasCustomKey) "Personal TMDB Key Active" else "Public Demo Mode Active",
                            color = NetflixTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (hasCustomKey)
                                "Endpoints authenticated via your custom v3 key."
                            else
                                "Using bundled demo key. Add your own key for unlimited rate limits.",
                            color = NetflixTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Key Input Label
            Text(
                text = "TMDB API Key (v3 Auth)",
                color = NetflixTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Input Field
            OutlinedTextField(
                value = currentKeyInput,
                onValueChange = onKeyInputChanged,
                placeholder = {
                    Text(
                        text = "e.g., 32-character hex string",
                        color = NetflixTextMuted,
                        fontSize = 13.sp
                    )
                },
                visualTransformation = if (showKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showKeyVisible = !showKeyVisible }) {
                            Icon(
                                imageVector = if (showKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = NetflixTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = NetflixBlack,
                    unfocusedContainerColor = NetflixBlack,
                    focusedBorderColor = NetflixRed,
                    unfocusedBorderColor = Color(0xFF383838),
                    focusedTextColor = NetflixTextPrimary,
                    unfocusedTextColor = NetflixTextPrimary,
                    cursorColor = NetflixRed
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tmdb_api_key_input")
            )

            // Validation Message Feedback
            if (validationStatus != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    val isSuccess = validationStatus.startsWith("Success")
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isSuccess) NetflixGreen else Color(0xFFFF5252),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = validationStatus,
                        color = if (isSuccess) NetflixGreen else Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Save & Test, Demo Mode)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save and Validate Key
                Button(
                    onClick = { onSaveKey(currentKeyInput) },
                    enabled = !isTesting && currentKeyInput.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NetflixRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("save_tmdb_key_button")
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying...", fontSize = 13.sp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Verify", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Reset / Use Demo Key
                OutlinedButton(
                    onClick = onUseDemoKey,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NetflixTextPrimary
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("use_demo_key_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = NetflixTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Demo Key", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instructions Guide: How to get a free TMDB Key
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = NetflixSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = NetflixTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How to get a free TMDB API Key:",
                            color = NetflixTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "1. Create a free account at themoviedb.org\n" +
                               "2. Go to Settings > API in your profile\n" +
                               "3. Click 'Create' and choose 'Developer'\n" +
                               "4. Copy your API Key (v3 auth) and paste it here",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Telegram Reports Section
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NetflixCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A2A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Telegram Reports Integration",
                            color = NetflixTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val isConfigured = botTokenInput.isNotBlank() && chatIdInput.isNotBlank()
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isConfigured) NetflixGreen.copy(alpha = 0.15f) else NetflixRed.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isConfigured) "Configured" else "Missing Config",
                                color = if (isConfigured) NetflixGreen else NetflixRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reports sent from movies, series, people, and studios will be forwarded directly to your Telegram bot.",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bot Token
                    Text(
                        text = "Telegram Bot Token",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = botTokenInput,
                        onValueChange = {
                            botTokenInput = it
                            telegramStatusMessage = null
                        },
                        placeholder = { Text("e.g. 123456789:ABCdefGHI...", color = NetflixTextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = NetflixRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_bot_token_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Chat ID
                    Text(
                        text = "Telegram Chat ID",
                        color = NetflixTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = chatIdInput,
                        onValueChange = {
                            chatIdInput = it
                            telegramStatusMessage = null
                        },
                        placeholder = { Text("e.g. 987654321 or -100123456789", color = NetflixTextMuted, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NetflixRed,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = NetflixRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_chat_id_input")
                    )

                    if (telegramStatusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = telegramStatusMessage!!,
                            color = if (telegramStatusSuccess == true) NetflixGreen else NetflixRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Save button
                        Button(
                            onClick = {
                                reportStore?.saveTelegramConfig(botTokenInput, chatIdInput)
                                telegramStatusSuccess = true
                                telegramStatusMessage = "Telegram settings saved!"
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NetflixRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("save_telegram_config_btn")
                        ) {
                            Text("Save Config", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // Test Connection button
                        OutlinedButton(
                            onClick = {
                                if (botTokenInput.isBlank() || chatIdInput.isBlank()) {
                                    telegramStatusSuccess = false
                                    telegramStatusMessage = "Please enter both Bot Token and Chat ID"
                                } else {
                                    isTelegramTesting = true
                                    telegramStatusMessage = "Testing Telegram message..."
                                    if (onTestTelegram != null) {
                                        onTestTelegram(botTokenInput, chatIdInput) { success, msg ->
                                            isTelegramTesting = false
                                            telegramStatusSuccess = success
                                            telegramStatusMessage = msg
                                        }
                                    } else {
                                        reportStore?.saveTelegramConfig(botTokenInput, chatIdInput)
                                        isTelegramTesting = false
                                        telegramStatusSuccess = true
                                        telegramStatusMessage = "Configuration saved"
                                    }
                                }
                            },
                            enabled = !isTelegramTesting,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("test_telegram_btn")
                        ) {
                            if (isTelegramTesting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test Ping", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
