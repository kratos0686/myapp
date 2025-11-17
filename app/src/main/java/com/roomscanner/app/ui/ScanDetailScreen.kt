package com.roomscanner.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.roomscanner.app.data.RoomScan
import com.roomscanner.app.data.JobNote
import java.text.SimpleDateFormat
import java.util.*

/**
 * Detail screen for a single room scan with Jetpack Compose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDetailScreen(
    scan: RoomScan,
    notes: List<JobNote>,
    onBackClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onNoteClick: (JobNote) -> Unit,
    onGenerateDryingPlanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(scan.roomName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNoteClick) {
                Icon(Icons.Default.NoteAdd, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scan Information Card
            item {
                ScanInfoCard(scan = scan)
            }
            
            // Dimensions Card
            item {
                DimensionsCard(scan = scan)
            }
            
            // Damaged Areas Card
            if (scan.damagedAreas.isNotEmpty()) {
                item {
                    DamagedAreasCard(scan = scan)
                }
            }
            
            // Material Estimates Card
            if (scan.materialEstimates.isNotEmpty()) {
                item {
                    MaterialEstimatesCard(scan = scan)
                }
            }
            
            // Generate Drying Plan Button
            item {
                Button(
                    onClick = onGenerateDryingPlanClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Drying Plan")
                }
            }
            
            // Job Notes Section
            item {
                Text(
                    text = "Job Notes (${notes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(notes) { note ->
                JobNoteCard(
                    note = note,
                    onClick = { onNoteClick(note) }
                )
            }
        }
    }
}

@Composable
fun ScanInfoCard(scan: RoomScan, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault()) }
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scan Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (scan.syncedToFirebase) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Synced") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(label = "Scanned", value = dateFormat.format(scan.scanDate))
            if (scan.firebaseDocId != null) {
                InfoRow(label = "Document ID", value = scan.firebaseDocId)
            }
        }
    }
}

@Composable
fun DimensionsCard(scan: RoomScan, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dimensions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            InfoRow(label = "Width", value = "%.2f m".format(scan.width))
            InfoRow(label = "Length", value = "%.2f m".format(scan.length))
            InfoRow(label = "Height", value = "%.2f m".format(scan.height))
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            InfoRow(label = "Area", value = "%.2f m²".format(scan.area), emphasized = true)
            InfoRow(label = "Volume", value = "%.2f m³".format(scan.volume), emphasized = true)
        }
    }
}

@Composable
fun DamagedAreasCard(scan: RoomScan, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Damaged Areas (${scan.damagedAreas.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            scan.damagedAreas.forEach { area ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = area.type,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = area.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { area.severity },
                        modifier = Modifier.width(60.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun MaterialEstimatesCard(scan: RoomScan, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Material Estimates",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            scan.materialEstimates.forEach { estimate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = estimate.materialType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${estimate.surfaceType} • ${(estimate.confidence * 100).toInt()}% confidence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobNoteCard(note: JobNote, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(note.updatedDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (note.tags.isNotEmpty()) {
                    AssistChip(
                        onClick = { },
                        label = { Text(note.tags.first()) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    emphasized: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
    }
}
