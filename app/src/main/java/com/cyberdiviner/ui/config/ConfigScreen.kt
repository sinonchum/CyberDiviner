package com.cyberdiviner.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.ui.shared.CyberButton
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.CyberWhite
import com.cyberdiviner.ui.theme.GrayBody
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.theme.GrayBorder
import kotlinx.coroutines.launch

@Composable
fun ConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configManager = remember { LlmConfigManager(context) }
    val scope = rememberCoroutineScope()

    val savedApiKey by configManager.apiKey.collectAsState(initial = "")
    val savedBaseUrl by configManager.baseUrl.collectAsState(initial = "")

    var apiKey by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var baseUrl by remember(savedBaseUrl) { mutableStateOf(savedBaseUrl) }
    var saved by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "CONFIG",
                color = GrayCaption,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )

            // API Key field
            Text(
                text = "API KEY",
                color = GrayBody,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBlack)
                    .padding(bottom = 4.dp)
            ) {
                var passwordVisible by remember { mutableStateOf(false) }
                androidx.compose.material3.OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        saved = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "sk-...",
                            color = GrayCaption,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite,
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder,
                        cursorColor = CyberWhite,
                        focusedPlaceholderColor = GrayCaption,
                        unfocusedPlaceholderColor = GrayCaption
                    ),
                    trailingIcon = {
                        androidx.compose.material3.TextButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                color = GrayCaption,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Base URL field
            Text(
                text = "BASE URL",
                color = GrayBody,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            androidx.compose.material3.OutlinedTextField(
                value = baseUrl,
                onValueChange = {
                    baseUrl = it
                    saved = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CyberWhite,
                    unfocusedTextColor = CyberWhite,
                    focusedBorderColor = GrayBorder,
                    unfocusedBorderColor = GrayBorder,
                    cursorColor = CyberWhite
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (saved) {
                Text(
                    text = "SAVED",
                    color = GrayCaption,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            CyberButton(
                text = "SAVE",
                onClick = {
                    scope.launch {
                        configManager.setApiKey(apiKey)
                        configManager.setBaseUrl(baseUrl)
                        saved = true
                    }
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Back button
            CyberButton(
                text = "BACK",
                onClick = onBack
            )
        }
    }
}
