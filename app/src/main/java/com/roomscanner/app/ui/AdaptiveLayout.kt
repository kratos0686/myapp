package com.roomscanner.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class AppDestination(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    SCANS("Scans", Icons.Default.ViewInAr, "Room Scans"),
    NOTES("Notes", Icons.Default.Notes, "Job Notes"),
    SETTINGS("Settings", Icons.Default.Settings, "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveRoomScannerApp(windowSizeClass: WindowSizeClass) {
    val destinations = AppDestination.entries
    var selected by remember { mutableStateOf(AppDestination.SCANS) }

    val useNavRail = windowSizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
    val useExpandedLayout = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (useNavRail) {
        TabletLayout(
            destinations = destinations,
            selected = selected,
            onDestinationSelected = { selected = it },
            useExpandedLayout = useExpandedLayout
        )
    } else {
        PhoneLayout(
            destinations = destinations,
            selected = selected,
            onDestinationSelected = { selected = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhoneLayout(
    destinations: List<AppDestination>,
    selected: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Scanner") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { dest ->
                    NavigationBarItem(
                        icon = { Icon(dest.icon, contentDescription = dest.contentDescription) },
                        label = { Text(dest.label) },
                        selected = selected == dest,
                        onClick = { onDestinationSelected(dest) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selected == AppDestination.SCANS) {
                FloatingActionButton(onClick = { }) {
                    Icon(Icons.Default.Add, contentDescription = "New Scan")
                }
            }
        }
    ) { padding ->
        AppContent(
            destination = selected,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabletLayout(
    destinations: List<AppDestination>,
    selected: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    useExpandedLayout: Boolean
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            header = {
                if (useExpandedLayout) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Room\nScanner",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                FloatingActionButton(
                    onClick = { },
                    modifier = Modifier.padding(vertical = 8.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Scan")
                }
            }
        ) {
            Spacer(modifier = Modifier.weight(1f))
            destinations.forEach { dest ->
                NavigationRailItem(
                    icon = { Icon(dest.icon, contentDescription = dest.contentDescription) },
                    label = { Text(dest.label) },
                    selected = selected == dest,
                    onClick = { onDestinationSelected(dest) }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            TopAppBar(
                title = { Text(selected.label) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
            AppContent(
                destination = selected,
                modifier = Modifier.fillMaxSize(),
                isTablet = true
            )
        }
    }
}

@Composable
private fun AppContent(
    destination: AppDestination,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
) {
    when (destination) {
        AppDestination.SCANS -> ScansScreen(modifier = modifier, isTablet = isTablet)
        AppDestination.NOTES -> NotesScreen(modifier = modifier)
        AppDestination.SETTINGS -> SettingsScreen(modifier = modifier)
    }
}

@Composable
fun ScansScreen(modifier: Modifier = Modifier, isTablet: Boolean = false) {
    if (isTablet) {
        TabletScansScreen(modifier = modifier)
    } else {
        PhoneScansScreen(modifier = modifier)
    }
}

@Composable
private fun PhoneScansScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Room Scans", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Tap + to start a new scan",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        FeatureSummaryList()
    }
}

@Composable
private fun TabletScansScreen(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left pane: scan list
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text("Scan List", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Select or create a scan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureSummaryList()
        }

        VerticalDivider()

        // Right pane: detail / preview
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ViewInAr,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No scan selected",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Choose a scan from the list or tap + to create a new one",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
    )
}

@Composable
private fun FeatureSummaryList() {
    val features = listOf(
        "ARCore 3D scanning",
        "ML Kit object & damage detection",
        "Offline storage with Room DB",
        "Firebase cloud sync",
        "RxJava reactive updates"
    )
    features.forEach { feature ->
        Text(
            text = "✓  $feature",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    }
}

@Composable
fun NotesScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Job Notes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Add notes to your scans for offline storage",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Configure sync, ML models, and ARCore settings",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
