package com.example.pest_detection_app.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel

@Composable
fun GlobalSyncBanner(
    detectionSaveViewModel: DetectionSaveViewModel,
    onDismiss: () -> Unit
) {
    val isSyncing by detectionSaveViewModel.isSyncing.collectAsState()
    var showBanner by remember { mutableStateOf(true) }
    
    // Reset showBanner when sync starts
    LaunchedEffect(isSyncing) {
        if (isSyncing) {
            // Force banner to show when sync starts
            showBanner = true
        }
    }

    AnimatedVisibility(
        visible = showBanner && isSyncing,
        enter = slideInVertically() + expandVertically(),
        exit = slideOutVertically() + shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Syncing data...",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Only show dismiss button if not syncing
                if (!isSyncing) {
                    IconButton(onClick = {
                        showBanner = false
                        onDismiss()
                    }) {
                        Text("✕", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
} 