package com.litechat.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litechat.android.util.DeviceCompat

@Composable
fun DeviceStatusCard(
    snap: DeviceCompat.Snapshot,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = shape,
    ) {
        Column(Modifier.padding(if (compact) 12.dp else 16.dp)) {
            Text(
                if (compact) "This device" else "Device fit",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                snap.summaryLine,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            if (!compact) {
                Spacer(Modifier.height(6.dp))
                Text(
                    snap.headline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (snap.lowMemory) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "System reports low memory right now (threshold ~${snap.thresholdMb} MB).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Band: ${DeviceCompat.bandLabel(snap.band)} · ABI ${snap.abi}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun CompatMatrixTable(
    highlight: DeviceCompat.Band,
    modifier: Modifier = Modifier,
) {
    val bands = DeviceCompat.Band.entries
    val scroll = rememberScrollState()
    val featureW = 148.dp
    val colW = 72.dp

    Column(modifier.fillMaxWidth()) {
        Text(
            "Compatibility (by free RAM)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Installed RAM is marketing. Free RAM is what the OS has left. " +
                "This app is thin chat + remote brain.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))

        Row(Modifier.horizontalScroll(scroll)) {
            Column {
                // Header
                Row(verticalAlignment = Alignment.Bottom) {
                    Box(Modifier.width(featureW).padding(end = 4.dp, bottom = 6.dp)) {
                        Text("Mode", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    bands.forEach { b ->
                        val on = b == highlight
                        Box(
                            Modifier
                                .width(colW)
                                .padding(horizontal = 2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    if (on) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                                    else Modifier,
                                )
                                .padding(vertical = 4.dp, horizontal = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                DeviceCompat.bandLabel(b).replace(" free", ""),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                                color = if (on) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                DeviceCompat.MATRIX.forEachIndexed { idx, row ->
                    val bg = if (idx % 2 == 0) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                    }
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.width(featureW).padding(end = 4.dp)) {
                            Text(
                                row.feature,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp,
                            )
                            if (row.note.isNotBlank()) {
                                Text(
                                    row.note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp,
                                )
                            }
                        }
                        bands.forEach { b ->
                            val v = row.forBand(b)
                            val on = b == highlight
                            Box(
                                Modifier
                                    .width(colW)
                                    .padding(horizontal = 2.dp)
                                    .then(
                                        if (on) {
                                            Modifier.border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                RoundedCornerShape(6.dp),
                                            )
                                        } else Modifier,
                                    )
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    DeviceCompat.verdictGlyph(v),
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Legend(DeviceCompat.Verdict.GO, "Recommended")
            Legend(DeviceCompat.Verdict.CAUTION, "Caveats")
            Legend(DeviceCompat.Verdict.NO, "Don't expect")
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Your column is highlighted from live free RAM (ActivityManager).",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Legend(v: DeviceCompat.Verdict, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(DeviceCompat.verdictGlyph(v), fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
