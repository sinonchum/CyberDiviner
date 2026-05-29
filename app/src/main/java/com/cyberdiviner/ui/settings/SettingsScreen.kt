package com.cyberdiviner.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cyberdiviner.ui.theme.*
import com.cyberdiviner.engine.Persona
import com.cyberdiviner.data.model.InferenceMode
import com.cyberdiviner.engine.offline.ModelManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val provider by viewModel.provider.collectAsState()
    val modelId by viewModel.modelId.collectAsState()
    val personaId by viewModel.personaId.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val inferenceMode by viewModel.inferenceMode.collectAsState()
    val modelState by viewModel.modelState.collectAsState()

    var showApiKey by remember { mutableStateOf(false) }
    var providerExpanded by remember { mutableStateOf(false) }
    var personaExpanded by remember { mutableStateOf(false) }

    val providers = listOf("OPENAI_COMPATIBLE", "OPENAI", "ANTHROPIC", "OLLAMA")
    val providerDisplayNames = mapOf(
        "OPENAI_COMPATIBLE" to "OpenAI-Compatible",
        "OPENAI" to "OpenAI",
        "ANTHROPIC" to "Anthropic",
        "OLLAMA" to "Ollama (Local)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "算命设置",
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GraySurface,
                    titleContentColor = CyberWhite,
                    navigationIconContentColor = CyberWhite
                )
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Status indicator ──────────────────────────────────────
            if (saved) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = CyberWhite.copy(alpha = 0.15f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "Configuration saved",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = CyberWhite,
                        fontSize = 13.sp,
                        fontFamily = MonoFontFamily
                    )
                }
            }

            // ── Section: Provider ─────────────────────────────────────
            SectionHeader("Provider")

            ExposedDropdownMenuBox(
                expanded = providerExpanded,
                onExpandedChange = { providerExpanded = it }
            ) {
                OutlinedTextField(
                    value = providerDisplayNames[provider] ?: provider,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Provider", fontSize = 13.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = settingsFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = MonoFontFamily,
                        fontSize = 14.sp,
                        color = CyberWhite
                    )
                )

                ExposedDropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false }
                ) {
                    providers.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    providerDisplayNames[p] ?: p,
                                    fontFamily = MonoFontFamily,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                viewModel.setProvider(p)
                                providerExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Section: Persona ──────────────────────────────────────
            SectionHeader("Persona")

            ExposedDropdownMenuBox(
                expanded = personaExpanded,
                onExpandedChange = { personaExpanded = it }
            ) {
                OutlinedTextField(
                    value = Persona.ALL[personaId]?.name ?: "Default",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Persona", fontSize = 13.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = personaExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = settingsFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = MonoFontFamily,
                        fontSize = 14.sp,
                        color = CyberWhite
                    )
                )

                ExposedDropdownMenu(
                    expanded = personaExpanded,
                    onDismissRequest = { personaExpanded = false }
                ) {
                    Persona.ALL.forEach { (id, persona) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    persona.name,
                                    fontFamily = MonoFontFamily,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                viewModel.setPersonaId(id)
                                personaExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                Persona.ALL[personaId]?.voiceDescription ?: "",
                color = GrayMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontFamily = MonoFontFamily
            )

            // ── Section: API Key ──────────────────────────────────────
            SectionHeader("API Key")

            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.setApiKey(it) },
                label = { Text("sk-...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showApiKey) VisualTransformation.None
                    else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = "Toggle visibility",
                            tint = GrayMuted
                        )
                    }
                },
                colors = settingsFieldColors(),
                shape = MaterialTheme.shapes.medium,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = MonoFontFamily,
                    fontSize = 14.sp,
                    color = CyberWhite
                )
            )

            // ── Section: Base URL ─────────────────────────────────────
            SectionHeader("Base URL")

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { viewModel.setBaseUrl(it) },
                label = { Text("https://api.example.com/v1", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = settingsFieldColors(),
                shape = MaterialTheme.shapes.medium,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = MonoFontFamily,
                    fontSize = 14.sp,
                    color = CyberWhite
                )
            )

            // ── Section: Model ────────────────────────────────────────
            SectionHeader("Model")

            OutlinedTextField(
                value = modelId,
                onValueChange = { viewModel.setModelId(it) },
                label = { Text("gpt-4o / claude-sonnet-4-20250514 / ...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = settingsFieldColors(),
                shape = MaterialTheme.shapes.medium,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = MonoFontFamily,
                    fontSize = 14.sp,
                    color = CyberWhite
                )
            )

            // ── Section: Inference Mode ────────────────────────────────
            SectionHeader("推理模式")

            var modeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = it }
            ) {
                OutlinedTextField(
                    value = inferenceMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Inference Mode", fontSize = 13.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = settingsFieldColors(),
                    shape = MaterialTheme.shapes.medium,
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
                                viewModel.setInferenceMode(mode)
                                modeExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Section: Offline Model ─────────────────────────────────
            SectionHeader("离线模型")

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GraySurface
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Model info
                    Text(
                        "Gemma 2 2B (int8) · ~2.7 GB",
                        color = CyberWhite,
                        fontSize = 14.sp,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "无网络时提供基础离线推理能力。下载后可离线生成签文、解卦、牌义。",
                        color = GrayMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        fontFamily = MonoFontFamily
                    )

                    // Status and action
                    when (val state = modelState) {
                        is ModelManager.ModelState.NotDownloaded -> {
                            Button(
                                onClick = { viewModel.downloadModel() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberWhite,
                                    contentColor = CyberBlack
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "下载离线模型",
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        is ModelManager.ModelState.Downloading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LinearProgressIndicator(
                                    progress = { state.percent / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = CyberWhite,
                                    trackColor = GrayMuted.copy(alpha = 0.3f)
                                )
                                Text(
                                    "下载中 ${state.percent}% · ${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)}",
                                    color = GrayMuted,
                                    fontSize = 12.sp,
                                    fontFamily = MonoFontFamily
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
                                    "已就绪",
                                    color = CyberWhite,
                                    fontSize = 14.sp,
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = { viewModel.deleteModel() },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = AccentRed
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, AccentRed.copy(alpha = 0.5f)
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        "删除模型",
                                        fontFamily = MonoFontFamily,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        is ModelManager.ModelState.Error -> {
                            Text(
                                "下载失败: ${state.message}",
                                color = AccentRed,
                                fontSize = 12.sp,
                                fontFamily = MonoFontFamily
                            )
                            Button(
                                onClick = { viewModel.downloadModel() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberWhite,
                                    contentColor = CyberBlack
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    "重试",
                                    fontFamily = MonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Save button ───────────────────────────────────────────
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberWhite,
                    contentColor = CyberBlack
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Save Configuration",
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // ── Info ──────────────────────────────────────────────────
            Text(
                "API key is stored locally on device using Android DataStore.\n" +
                "For OpenAI-Compatible providers, enter your relay base URL.",
                color = GrayMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = MonoFontFamily
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        color = CyberWhite,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = MonoFontFamily,
        letterSpacing = 1.sp
    )
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) "%.1f GB".format(gb)
    else {
        val mb = bytes / (1024.0 * 1024.0)
        "%.0f MB".format(mb)
    }
}

@Composable
private fun settingsFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyberWhite,
    unfocusedBorderColor = GrayMuted,
    focusedContainerColor = GraySurface,
    unfocusedContainerColor = GraySurface,
    focusedLabelColor = CyberWhite,
    unfocusedLabelColor = GrayMuted,
    cursorColor = CyberWhite
)
