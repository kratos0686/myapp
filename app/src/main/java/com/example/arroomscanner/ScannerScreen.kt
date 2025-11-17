package com.example.arroomscanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val scanningState by viewModel.scanningState.collectAsState()
    val mlModelLoaded by viewModel.mlModelLoaded.collectAsState()
    val pointCloudData by viewModel.pointCloudData.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // AR Camera view would be rendered here
        // In a production app, this would use ARCore's ArFragment or custom OpenGL surface
        ARCameraPlaceholder()
        
        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top status bar
            StatusBar(
                scanningState = scanningState,
                mlModelLoaded = mlModelLoaded,
                pointCount = pointCloudData.size
            )
            
            // Bottom controls
            ControlPanel(
                scanningState = scanningState,
                onStartScan = { viewModel.startScanning() },
                onStopScan = { viewModel.stopScanning() }
            )
        }
    }
}

@Composable
fun ARCameraPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "AR Camera View",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun StatusBar(
    scanningState: ScanningState,
    mlModelLoaded: Boolean,
    pointCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Scanning status
            val statusText = when (scanningState) {
                is ScanningState.Idle -> "Ready to scan"
                is ScanningState.Ready -> "AR Initialized"
                is ScanningState.Scanning -> stringResource(R.string.scanning_status)
                is ScanningState.Complete -> stringResource(R.string.scan_complete)
                is ScanningState.Error -> "Error: ${scanningState.message}"
            }
            
            Text(
                text = statusText,
                color = when (scanningState) {
                    is ScanningState.Error -> Color.Red
                    is ScanningState.Scanning -> Color.Green
                    else -> Color.White
                },
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ML Model status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ML Model: ",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (mlModelLoaded) "Loaded" else "Not Loaded",
                    color = if (mlModelLoaded) Color.Green else Color.Yellow,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Point cloud info
            if (scanningState is ScanningState.Scanning || scanningState is ScanningState.Complete) {
                Text(
                    text = "Points captured: $pointCount",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ControlPanel(
    scanningState: ScanningState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (scanningState) {
                is ScanningState.Idle, 
                is ScanningState.Ready,
                is ScanningState.Complete -> {
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.scan_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                is ScanningState.Scanning -> {
                    Button(
                        onClick = onStopScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.stop_button),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                is ScanningState.Error -> {
                    Text(
                        text = scanningState.message,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onStartScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}
