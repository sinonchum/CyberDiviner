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
                        "AI Settings",
                        fontFamily = FontFamily.Monospace,
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
                    containerColor = CyberGray,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
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
                        containerColor = CyberPrimary.copy(alpha = 0.15f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "Configuration saved",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        color = CyberPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
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
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = TextPrimary
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
                                    fontFamily = FontFamily.Monospace,
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
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = TextPrimary
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
                                    fontFamily = FontFamily.Monospace,
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
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontFamily = FontFamily.Monospace
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
                            tint = TextMuted
                        )
                    }
                },
                colors = settingsFieldColors(),
                shape = MaterialTheme.shapes.medium,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = TextPrimary
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
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = TextPrimary
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
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            )

            // ── Save button ───────────────────────────────────────────
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPrimary,
                    contentColor = CyberBlack
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Save Configuration",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // ── Info ──────────────────────────────────────────────────
            Text(
                "API key is stored locally on device using Android DataStore.\n" +
                "For OpenAI-Compatible providers, enter your relay base URL.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        color = CyberPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
    )
}

@Composable
private fun settingsFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CyberPrimary,
    unfocusedBorderColor = TextMuted,
    focusedContainerColor = CyberGray,
    unfocusedContainerColor = CyberGray,
    focusedLabelColor = CyberPrimary,
    unfocusedLabelColor = TextMuted,
    cursorColor = CyberPrimary
)
