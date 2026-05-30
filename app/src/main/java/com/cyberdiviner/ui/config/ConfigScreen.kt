package com.cyberdiviner.ui.config
import com.cyberdiviner.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cyberdiviner.data.model.InferenceMode
import com.cyberdiviner.data.remote.LlmConfigManager
import com.cyberdiviner.engine.offline.GemmaEngine
import com.cyberdiviner.engine.offline.ModelManager
import com.cyberdiviner.ui.shared.CyberButton
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val configManager = remember { LlmConfigManager(context) }
    val modelManager = remember { ModelManager(context) }
    val scope = rememberCoroutineScope()

    val savedApiKey by configManager.apiKey.collectAsState(initial = "")
    val savedBaseUrl by configManager.baseUrl.collectAsState(initial = "")
    val savedInferenceMode by configManager.inferenceMode.collectAsState(initial = "AUTO")
    val savedOfflineModelEnabled by configManager.offlineModelEnabled.collectAsState(initial = false)
    val modelState by modelManager.state.collectAsState()

    var apiKey by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var baseUrl by remember(savedBaseUrl) { mutableStateOf(savedBaseUrl) }
    var saved by remember { mutableStateOf(false) }
    var inferenceMode by remember(savedInferenceMode) { mutableStateOf(InferenceMode.fromName(savedInferenceMode)) }
    var offlineModelEnabled by remember(savedOfflineModelEnabled) { mutableStateOf(savedOfflineModelEnabled) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "CONFIG",
                color = GrayCaption,
                fontFamily = MonoFontFamily,
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
                fontFamily = MonoFontFamily,
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
                OutlinedTextField(
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
                            fontFamily = MonoFontFamily,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite,
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder,
                        cursorColor = CyberWhite,
                        focusedPlaceholderColor = GrayCaption,
                        unfocusedPlaceholderColor = GrayCaption
                    ),
                    trailingIcon = {
                        TextButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Text(
                                text = if (passwordVisible) "HIDE" else "SHOW",
                                color = GrayCaption,
                                fontFamily = MonoFontFamily,
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
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = {
                    baseUrl = it
                    saved = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CyberWhite,
                    unfocusedTextColor = CyberWhite,
                    focusedBorderColor = GrayBorder,
                    unfocusedBorderColor = GrayBorder,
                    cursorColor = CyberWhite
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Inference Mode ──────────────────────────────────────────
            Text(
                text = "INFERENCE MODE",
                color = GrayBody,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            var modeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = it }
            ) {
                OutlinedTextField(
                    value = inferenceMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CyberWhite,
                        unfocusedTextColor = CyberWhite,
                        focusedBorderColor = GrayBorder,
                        unfocusedBorderColor = GrayBorder,
                        cursorColor = CyberWhite
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = MonoFontFamily,
                        fontSize = 14.sp,
                        color = CyberWhite
                    )
                )

                ExposedDropdownMenu(
                    expanded = modeExpanded,
                    onDismissRequest = { modeExpanded = false }
                ) {
                    InferenceMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    mode.displayName,
                                    fontFamily = MonoFontFamily,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                inferenceMode = mode
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Offline Model ───────────────────────────────────────────
            Text(
                text = "OFFLINE MODEL",
                color = GrayBody,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Toggle row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "离线模型",
                        color = CyberWhite,
                        fontFamily = MonoFontFamily,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "启用后可离线使用，关闭可释放约1.5GB内存",
                        color = GrayCaption,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = offlineModelEnabled,
                    onCheckedChange = { enabled ->
                        offlineModelEnabled = enabled
                        scope.launch {
                            configManager.setOfflineModelEnabled(enabled)
                            if (!enabled) {
                                GemmaEngine.forceReleaseActive()
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberWhite,
                        checkedTrackColor = GrayBody,
                        uncheckedThumbColor = GrayCaption,
                        uncheckedTrackColor = CyberBlack
                    )
                )
            }

            // Model info
            Text(
                text = "Qwen2.5 1.5B · ~1.6 GB",
                color = CyberWhite,
                fontFamily = MonoFontFamily,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            Text(
                text = "无网络时提供基础离线推理",
                color = GrayCaption,
                fontFamily = MonoFontFamily,
                fontSize = 11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Model status and actions
            when (val state = modelState) {
                is ModelManager.ModelState.NotDownloaded -> {
                    CyberButton(
                        text = "DOWNLOAD",
                        onClick = {
                            scope.launch { modelManager.download() }
                        }
                    )
                }

                is ModelManager.ModelState.Downloading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { state.percent / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = CyberWhite,
                            trackColor = GrayBorder.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "${state.percent}% · ${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)}",
                            color = GrayCaption,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is ModelManager.ModelState.Ready -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "READY",
                            color = CyberWhite,
                            fontFamily = MonoFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier.weight(1f)
                        )
                        CyberButton(
                            text = "DELETE",
                            onClick = {
                                scope.launch { modelManager.delete() }
                            }
                        )
                    }
                }

                is ModelManager.ModelState.Error -> {
                    Text(
                        text = "ERROR: ${state.message}",
                        color = AccentRed,
                        fontFamily = MonoFontFamily,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    CyberButton(
                        text = "RETRY",
                        onClick = {
                            scope.launch { modelManager.download() }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (saved) {
                Text(
                    text = "SAVED",
                    color = GrayCaption,
                    fontFamily = MonoFontFamily,
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
                        configManager.setInferenceMode(inferenceMode.name)
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

private fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) "%.1f GB".format(gb)
    else {
        val mb = bytes / (1024.0 * 1024.0)
        "%.0f MB".format(mb)
    }
}
