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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import androidx.compose.foundation.clickable
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
    val savedOfflineModelVariant by configManager.offlineModelVariant.collectAsState(initial = "BASE_GEMMA_3_1B")
    val savedOfflineModelEnabled by configManager.offlineModelEnabled.collectAsState(initial = false)
    val modelState by modelManager.state.collectAsState()

    var apiKey by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var baseUrl by remember(savedBaseUrl) { mutableStateOf(savedBaseUrl) }
    var saved by remember { mutableStateOf(false) }
    var inferenceMode by remember(savedInferenceMode) { mutableStateOf(InferenceMode.fromName(savedInferenceMode)) }
    var offlineModelVariant by remember(savedOfflineModelVariant) {
        mutableStateOf(ModelManager.OfflineModelVariant.fromName(savedOfflineModelVariant))
    }
    var offlineModelEnabled by remember(savedOfflineModelEnabled) { mutableStateOf(savedOfflineModelEnabled) }

    // SAF launcher for manual .task import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                context.contentResolver.openInputStream(it)?.let { stream ->
                    modelManager.importFromStream(stream)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 78.dp)
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
                    .padding(bottom = 18.dp)
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

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

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
                        fontSize = 13.sp
                    )
                    Text(
                        text = "关闭时释放内存",
                        color = GrayCaption,
                        fontFamily = MonoFontFamily,
                        fontSize = 10.sp
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

            Text(
                text = "离线模型来源",
                color = GrayBody,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModelManager.OfflineModelVariant.entries.forEach { variant ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (offlineModelVariant == variant) {
                                GraySurface.copy(alpha = 0.95f)
                            } else {
                                CyberBlack
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = offlineModelVariant == variant,
                                onClick = {
                                    offlineModelVariant = variant
                                    scope.launch {
                                        GemmaEngine.forceReleaseActive()
                                        modelManager.selectModel(variant)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = CyberWhite,
                                    unselectedColor = GrayCaption
                                )
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = variant.displayName,
                                        color = CyberWhite,
                                        fontFamily = MonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = variant.sizeDisplay,
                                        color = GrayCaption,
                                        fontFamily = MonoFontFamily,
                                        fontSize = 10.sp
                                    )
                                }
                                Text(
                                    text = variant.description,
                                    color = GrayCaption,
                                    fontFamily = MonoFontFamily,
                                    fontSize = 9.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Model info
            Text(
                text = "当前加载目标：${offlineModelVariant.displayName}",
                color = CyberWhite,
                fontFamily = MonoFontFamily,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp)
            )

            Text(
                text = "${offlineModelVariant.sizeDisplay} · 无网络时提供基础离线推理",
                color = GrayCaption,
                fontFamily = MonoFontFamily,
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Model status and actions
            when (val state = modelState) {
                is ModelManager.ModelState.NotDownloaded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CyberButton(
                            text = "导入本地模型",
                            onClick = {
                                importLauncher.launch(arrayOf("application/octet-stream", "application/x-tflite", "*/*"))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        CyberButton(
                            text = "下载模型",
                            onClick = {
                                scope.launch { modelManager.download() }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is ModelManager.ModelState.Downloading -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.sourceName.isNotBlank()) {
                            Text(
                                text = state.sourceName,
                                color = GrayCaption,
                                fontFamily = MonoFontFamily,
                                fontSize = 11.sp
                            )
                        }
                        if (state.totalBytes > 0 && state.percent >= 0) {
                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = CyberWhite,
                                trackColor = GrayBorder.copy(alpha = 0.3f)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = CyberWhite,
                                trackColor = GrayBorder.copy(alpha = 0.3f)
                            )
                        }
                        Text(
                            text = if (state.totalBytes > 0 && state.percent >= 0) {
                                "${state.percent}% · ${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)}"
                            } else {
                                "已导入 ${formatBytes(state.bytesDownloaded)}，请勿离开此页"
                            },
                            color = GrayCaption,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                is ModelManager.ModelState.Ready -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "READY · ${offlineModelVariant.sizeDisplay}",
                            color = CyberWhite,
                            fontFamily = MonoFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CyberButton(
                                text = "导入本地模型",
                                onClick = {
                                    importLauncher.launch(arrayOf("application/octet-stream", "application/x-tflite", "*/*"))
                                },
                                modifier = Modifier.weight(1f)
                            )
                            CyberButton(
                                text = "删除模型",
                                onClick = {
                                    scope.launch { modelManager.delete() }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                is ModelManager.ModelState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = AccentRed,
                            fontFamily = MonoFontFamily,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        CyberButton(
                            text = "RETRY",
                            onClick = {
                                scope.launch { modelManager.download() }
                            }
                        )
                    }
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

            Spacer(modifier = Modifier.height(8.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CyberBlack)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CompactConfigButton(
                text = "SAVE",
                onClick = {
                    scope.launch {
                        configManager.setApiKey(apiKey)
                        configManager.setBaseUrl(baseUrl)
                        configManager.setInferenceMode(inferenceMode.name)
                        saved = true
                    }
                }
            )

            CompactConfigButton(
                text = "BACK",
                onClick = onBack
            )
        }
    }
}

@Composable
private fun CompactConfigButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberBlack)
            .semantics { contentDescription = text }
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = CyberWhite,
            fontFamily = HuiwenFontFamily,
            fontSize = 13.sp,
            letterSpacing = 2.sp
        )
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
