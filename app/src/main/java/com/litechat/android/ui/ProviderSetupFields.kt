package com.litechat.android.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.litechat.android.data.prefs.ModelOption
import com.litechat.android.data.prefs.ProviderCatalog
import com.litechat.android.data.prefs.ProviderOption

/**
 * C-033: caveman setup — pick provider, pick model, paste key.
 * Base URL is filled in. Only Custom shows the URL box.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderSetupFields(
    key: String,
    onKeyChange: (String) -> Unit,
    base: String,
    onBaseChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    hostModels: List<String> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var provider by remember(base) { mutableStateOf(ProviderCatalog.fromBaseUrl(base)) }
    var showProviders by remember { mutableStateOf(false) }
    var showModels by remember { mutableStateOf(false) }
    val isCustom = provider.id == "custom"

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("1. Pick who you talk to", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = showProviders,
            onExpandedChange = { showProviders = it },
        ) {
            OutlinedTextField(
                value = provider.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Provider") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProviders) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = showProviders,
                onDismissRequest = { showProviders = false },
            ) {
                ProviderCatalog.PROVIDERS.forEach { p ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(p.name)
                                Text(
                                    p.tagline,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        onClick = {
                            pickProvider(p, onBaseChange, onModelChange)
                            provider = p
                            showProviders = false
                        },
                    )
                }
            }
        }
        Text(
            provider.tagline,
            style = MaterialTheme.typography.bodySmall,
            color = if (provider.paid) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isCustom) {
            OutlinedTextField(
                value = base,
                onValueChange = onBaseChange,
                label = { Text("Address (URL)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }

        if (provider.needsKey) {
            Text("2. Paste your key", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = key,
                onValueChange = onKeyChange,
                label = { Text("API key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            provider.keyUrl?.let { url ->
                TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }) {
                    Text(if (provider.paid) "Get a key (can cost money)" else "Get a free key")
                }
            }
        } else {
            Text(
                "No key needed for this one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text("3. Pick a model", style = MaterialTheme.typography.labelLarge)
        val mergedModels = buildList {
            addAll(provider.models)
            ProviderCatalog.chatModelIds(hostModels).forEach { id ->
                if (none { it.id == id }) add(ModelOption(id, id))
            }
        }
        if (isCustom && mergedModels.isEmpty()) {
            OutlinedTextField(
                value = model,
                onValueChange = onModelChange,
                label = { Text("Model name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        } else {
            val label = mergedModels.firstOrNull { it.id == model }?.label ?: model
            ExposedDropdownMenuBox(
                expanded = showModels,
                onExpandedChange = { showModels = it },
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Model") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showModels) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = showModels,
                    onDismissRequest = { showModels = false },
                ) {
                    mergedModels.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.label) },
                            onClick = {
                                onModelChange(m.id)
                                showModels = false
                            },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(0.dp))
    }
}

private fun pickProvider(
    p: ProviderOption,
    onBaseChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
) {
    onBaseChange(p.baseUrl)
    val first = p.models.firstOrNull()?.id
    if (first != null) onModelChange(first)
}
