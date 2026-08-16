package com.litechat.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.litechat.android.data.prefs.ChatFolder
import com.litechat.android.data.prefs.PersonaPacks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FolderBar(
    folders: List<ChatFolder>,
    activeFolderId: String?,
    isPro: Boolean,
    onSelect: (String?) -> Unit,
    onCreate: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FolderChip("All", activeFolderId == null) { onSelect(null) }
            folders.forEach { f ->
                FolderChip(f.name, activeFolderId == f.id, onClick = { onSelect(f.id) })
            }
        }
        if (isPro) {
            Row(
                Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("New folder") },
                )
                TextButton(onClick = {
                    onCreate(newName)
                    newName = ""
                }) { Text("Add") }
            }
            val active = folders.find { it.id == activeFolderId }
            if (active != null) {
                TextButton(onClick = { onDelete(active.id) }) {
                    Text("Delete folder ${active.name}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun FolderChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
    }
}

@Composable
fun PersonaRow(
    activeId: String,
    isPro: Boolean,
    onPick: (String) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            onClick = { onPick("") },
            color = if (activeId.isBlank()) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
        ) { Text("None", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall) }
        PersonaPacks.ALL.forEach { p ->
            Surface(
                onClick = { onPick(p.id) },
                color = if (activeId == p.id) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    p.name,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        if (!isPro) {
            Text(
                "Pro for personas",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun RegisteredCard(proSinceMillis: Long) {
    val date = if (proSinceMillis > 0) {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(proSinceMillis))
    } else {
        "today"
    }
    Column(Modifier.padding(vertical = 4.dp)) {
        Text("Registered", fontWeight = FontWeight.SemiBold)
        Text(
            "Registered — BYO AI · $date · no renewal, ever",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun MemoryList(
    facts: List<com.litechat.android.data.context.MemoryManager.MemoryEntry>,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column {
        Text("Memory", fontWeight = FontWeight.SemiBold)
        if (facts.isEmpty()) {
            Text("Nothing stored yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        facts.forEach { entry ->
            var edit by remember(entry.fact) { mutableStateOf(entry.fact) }
            Column(Modifier.padding(vertical = 6.dp)) {
                OutlinedTextField(
                    value = edit,
                    onValueChange = { edit = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row {
                    TextButton(onClick = { onEdit(entry.fact, edit) }) { Text("Save") }
                    TextButton(onClick = { onDelete(entry.fact) }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
