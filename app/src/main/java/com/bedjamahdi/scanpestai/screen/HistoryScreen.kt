package com.bedjamahdi.scanpestai.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.bedjamahdi.scanpestai.MyApp
import com.bedjamahdi.scanpestai.R
import com.bedjamahdi.scanpestai.ViewModels.detection_result.DetectionSaveViewModel
import com.bedjamahdi.scanpestai.ViewModels.user.LoginViewModel
import com.bedjamahdi.scanpestai.data.user.DetectionWithBoundingBoxes
import com.bedjamahdi.scanpestai.screen.user.LanguagePref
import com.bedjamahdi.scanpestai.ui.theme.AppTypography
import com.bedjamahdi.scanpestai.ui.theme.CustomTextStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun getTranslatedPestName(pestName: String): String {
    val currentLanguage = LocalContext.current.resources.configuration.locales[0].language

    return when (currentLanguage) {
        "ar" -> pestNameTranslationsAr[pestName] ?: pestNameTranslationsFr[pestName] ?: pestName
        "fr" -> pestNameTranslationsFr[pestName] ?: pestNameTranslationsAr[pestName] ?: pestName
        else -> pestName // English fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionHistoryScreen(
    navController: NavController,
    userViewModel: LoginViewModel,
    viewModel: DetectionSaveViewModel, // Receive shared instance instead of creating new one
) {
    val savedUserId by userViewModel.userId.collectAsState()
    val detectionList by viewModel.detections.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncCompletedEvent by viewModel.syncCompletedEvent.collectAsState(initial = null)

    var selectedPest by remember { mutableStateOf("None") }
    var isDescending by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load initial detections
    LaunchedEffect(savedUserId) {
        viewModel.getSortedDetections(savedUserId, isDescending)
    }



    val englishPestNames = mapOf(
        "pest.1" to "rice leaf roller",
        "pest.2" to "rice leaf caterpillar",
        "pest.3" to "paddy stem maggot",
        "pest.4" to "asiatic rice borer",
        "pest.5" to "yellow rice borer",
        "pest.6" to "rice gall midge",
        "pest.7" to "Rice Stem fly",
        "pest.8" to "brown plant hopper",
        "pest.9" to "white backed plant hopper",
        "pest.10" to "small brown plant hopper",
        "pest.11" to "rice water weevil",
        "pest.12" to "rice leaf hopper",
        "pest.13" to "grain spreader thrips",
        "pest.14" to "rice shell pest",
        "pest.15" to "grub",
        "pest.16" to "mole cricket",
        "pest.17" to "wireworm",
        "pest.18" to "white margined moth",
        "pest.19" to "black cutworm",
        "pest.20" to "large cutworm",
        "pest.21" to "yellow cutworm",
        "pest.22" to "red spider",
        "pest.23" to "corn borer",
        "pest.24" to "army worm",
        "pest.25" to "aphids",
        "pest.26" to "Potosiabre vitarsis",
        "pest.27" to "peach borer",
        "pest.28" to "english grain aphid",
        "pest.29" to "green bug",
        "pest.30" to "bird cherry-oat aphid",
        "pest.31" to "wheat blossom midge",
        "pest.32" to "penthaleus major",
        "pest.33" to "long legged spider mite",
        "pest.34" to "wheat phloeo thrips",
        "pest.35" to "wheat sawfly",
        "pest.36" to "cerodonta denticornis",
        "pest.37" to "beet fly",
        "pest.38" to "flea beetle",
        "pest.39" to "cabbage army worm",
        "pest.40" to "beet army worm",
        "pest.41" to "Beet spot flies",
        "pest.42" to "meadow moth",
        "pest.43" to "beet weevil",
        "pest.44" to "sericaorient alismots chulsky",
        "pest.45" to "alfalfa weevil",
        "pest.46" to "flax budworm",
        "pest.47" to "alfalfa plant bug",
        "pest.48" to "tarnished plant bug",
        "pest.49" to "Locustoidea",
        "pest.50" to "lytta polita",
        "pest.51" to "legume blister beetle",
        "pest.52" to "blister beetle",
        "pest.53" to "therioaphis maculata Buckton",
        "pest.54" to "odontothrips loti",
        "pest.55" to "Thrips",
        "pest.56" to "alfalfa seed chalcid",
        "pest.57" to "Pieris canidia",
        "pest.58" to "Apolygus lucorum",
        "pest.59" to "Limacodidae",
        "pest.60" to "Viteus vitifoliae",
        "pest.61" to "Colomerus vitis",
        "pest.62" to "Brevipalpus lewisi McGregor",
        "pest.63" to "oides decempunctata",
        "pest.64" to "Polyphagotarsonemus latus",
        "pest.65" to "Pseudococcus comstocki Kuwana",
        "pest.66" to "parathrene regalis",
        "pest.67" to "Ampelophaga",
        "pest.68" to "Lycorma delicatula",
        "pest.69" to "Xylotrechus",
        "pest.70" to "Cicadella viridis",
        "pest.71" to "Miridae",
        "pest.72" to "Trialeurodes vaporariorum",
        "pest.73" to "Erythroneura apicalis",
        "pest.74" to "Papilio xuthus",
        "pest.75" to "Panonchus citri McGregor",
        "pest.76" to "Phyllocoptes oleiverus ashmead",
        "pest.77" to "Icerya purchasi Maskell",
        "pest.78" to "Unaspis yanonensis",
        "pest.79" to "Ceroplastes rubens",
        "pest.80" to "Chrysomphalus aonidum",
        "pest.81" to "Parlatoria zizyphus Lucus",
        "pest.82" to "Nipaecoccus vastalor",
        "pest.83" to "Aleurocanthus spiniferus",
        "pest.84" to "Tetradacus c Bactrocera minax",
        "pest.85" to "Dacus dorsalis(Hendel)",
        "pest.86" to "Bactrocera tsuneonis",
        "pest.87" to "Prodenia litura",
        "pest.88" to "Adristyrannus",
        "pest.89" to "Phyllocnistis citrella Stainton",
        "pest.90" to "Toxoptera citricidus"
    )

    val currentLanguage = LanguagePref.getLanguage(MyApp.getContext())


    // Usage example for translation:
    fun getPestDisplayName(pestId: String, currentLanguage: String): String {
        val englishName = englishPestNames[pestId] ?: return pestId

        return when (currentLanguage) {
            "ar" -> pestNameTranslationsAr[englishName] ?: englishName
            "fr" -> pestNameTranslationsFr[englishName] ?: englishName
            else -> englishName
        }
    }
    // 🔥 THIS IS THE KEY FIX - Now using the shared ViewModel instance
    LaunchedEffect(syncCompletedEvent) {
        syncCompletedEvent?.let { syncResult ->
            when (syncResult) {
                is DetectionSaveViewModel.SyncResult.Success -> {
                    viewModel.forceRefreshDetections(
                        userId = savedUserId,
                        selectedPest = selectedPest,
                        isDescending = isDescending
                    )
                }

                is DetectionSaveViewModel.SyncResult.Failure -> {
                    viewModel.forceRefreshDetections(
                        userId = savedUserId,
                        selectedPest = selectedPest,
                        isDescending = isDescending
                    )
                }
            }
            viewModel.clearSyncResult()
        }
    }

    Scaffold() { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar with custom typography
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detections_history),
                        style = CustomTextStyles.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )


            if (detectionList.isEmpty()) {
                // Show "No detections" message in center of screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_detection_data),
                        style = CustomTextStyles.sectionTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {

                // Action buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pest Selection Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (selectedPest == "None") stringResource(R.string.select_pest) else selectedPest,
                                style = CustomTextStyles.buttonText,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            listOf(
                                "None",
                                "rice leaf roller",
                                "rice leaf caterpillar",
                                "paddy stem maggot",
                                "asiatic rice borer",
                                "yellow rice borer",
                                "rice gall midge",
                                "Rice Stem fly",
                                "brown plant hopper",
                                "white backed plant hopper",
                                "small brown plant hopper",
                                "rice water weevil",
                                "rice leaf hopper",
                                "grain spreader thrips",
                                "rice shell pest",
                                "grub",
                                "mole cricket",
                                "wireworm",
                                "white margined moth",
                                "black cutworm",
                                "large cutworm",
                                "yellow cutworm",
                                "red spider",
                                "corn borer",
                                "army worm",
                                "aphids",
                                "Potosiabre vitarsis",
                                "peach borer",
                                "english grain aphid",
                                "green bug",
                                "bird cherry-oat aphid",
                                "wheat blossom midge",
                                "penthaleus major",
                                "long legged spider mite",
                                "wheat phloeo thrips",
                                "wheat sawfly",
                                "cerodonta denticornis",
                                "beet fly",
                                "flea beetle",
                                "cabbage army worm",
                                "beet army worm",
                                "Beet spot flies",
                                "meadow moth",
                                "beet weevil",
                                "sericaorient alismots chulsky",
                                "alfalfa weevil",
                                "flax budworm",
                                "alfalfa plant bug",
                                "tarnished plant bug",
                                "Locustoidea",
                                "lytta polita",
                                "legume blister beetle",
                                "blister beetle",
                                "therioaphis maculata Buckton",
                                "odontothrips loti",
                                "Thrips",
                                "alfalfa seed chalcid",
                                "Pieris canidia",
                                "Apolygus lucorum",
                                "Limacodidae",
                                "Viteus vitifoliae",
                                "Colomerus vitis",
                                "Brevipalpus lewisi McGregor",
                                "oides decempunctata",
                                "Polyphagotarsonemus latus",
                                "Pseudococcus comstocki Kuwana",
                                "parathrene regalis",
                                "Ampelophaga",
                                "Lycorma delicatula",
                                "Xylotrechus",
                                "Cicadella viridis",
                                "Miridae",
                                "Trialeurodes vaporariorum",
                                "Erythroneura apicalis",
                                "Papilio xuthus",
                                "Panonchus citri McGregor",
                                "Phyllocoptes oleiverus ashmead",
                                "Icerya purchasi Maskell",
                                "Unaspis yanonensis",
                                "Ceroplastes rubens",
                                "Chrysomphalus aonidum",
                                "Parlatoria zizyphus Lucus",
                                "Nipaecoccus vastalor",
                                "Aleurocanthus spiniferus",
                                "Tetradacus c Bactrocera minax",
                                "Dacus dorsalis(Hendel)",
                                "Bactrocera tsuneonis",
                                "Prodenia litura",
                                "Adristyrannus",
                                "Phyllocnistis citrella Stainton",
                                "Toxoptera citricidus",
                                "Toxoptera aurantii",
                                "Aphis citricola Vander Goot",
                                "Scirtothrips dorsalis Hood",
                                "Dasineura sp",
                                "Lawana imitata Melichar",
                                "Salurnis marginella Guerr",
                                "Deporaus marginatus Pascoe",
                                "Chlumetia transversa",
                                "Mango flat beak leafhopper",
                                "Rhytidodera bowrinii white",
                                "Sternochetus frigidus",
                                "Cicadellidae"
                            ).forEach { pest ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (pest == "None") stringResource(R.string.select_pest) else getTranslatedPestName(
                                                pest
                                            ),
                                            style = AppTypography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        selectedPest = pest
                                        expanded = false
                                        viewModel.getDetectionsByPestName(
                                            if (selectedPest == "None") "" else selectedPest,
                                            isDescending
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Sorting Button
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = {
                                isDescending = !isDescending
                                viewModel.getDetectionsByPestName(
                                    if (selectedPest == "None") "" else selectedPest,
                                    isDescending
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (isDescending) "${stringResource(R.string.date)} ▼" else "${
                                    stringResource(
                                        R.string.date
                                    )
                                } ▲",
                                style = CustomTextStyles.buttonText,
                                color = MaterialTheme.colorScheme.onSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Delete Button
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = if (selectedPest == "None") stringResource(R.string.delete_all) else stringResource(
                                    R.string.delete_all
                                ),
                                style = CustomTextStyles.buttonText,
                                color = MaterialTheme.colorScheme.onError,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Confirmation Dialog for Deleting
                if (showDeleteDialog) {
                    ConfirmDeleteDialog(
                        message = if (selectedPest == "None")
                            stringResource(R.string.confirm_delete_all)
                        else
                            stringResource(R.string.confirm_delete_filtered) + "${
                                getTranslatedPestName(
                                    selectedPest
                                )
                            }?",

                        onConfirm = {
                            savedUserId?.let {
                                if (selectedPest == "None") {
                                    viewModel.deleteAllDetections(it, isDescending)
                                } else {
                                    viewModel.deleteDetectionsByPestName(it, selectedPest)
                                    selectedPest = "None"
                                }
                            }
                            showDeleteDialog = false
                        },
                        onDismiss = { showDeleteDialog = false }
                    )
                }

                // Detection List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(detectionList) { detection ->
                        DetectionItem(
                            navController, detection, viewModel, savedUserId, isDescending, {
                                selectedPest = "None"
                            })
                    }
                }
            }
        }
    }
}
@Composable
fun DetectionItem(
    navController: NavController,
    detection: DetectionWithBoundingBoxes,
    viewModel: DetectionSaveViewModel,
    userId: Int?,
    isDescending: Boolean,
    resetPestSelection: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("detail_screen/${detection.detection.id}") },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Detection Image
                Image(
                    painter = rememberAsyncImagePainter(detection.detection.imageUri),
                    contentDescription = "Detected Pest",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.date) + " ${formatDate(detection.detection.detectionDate)}",
                        style = CustomTextStyles.dateText,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (detection.boundingBoxes.isNotEmpty()) {
                        detection.boundingBoxes.forEachIndexed { index, box ->
                            Column {
                                Text(
                                    text = stringResource(R.string.detected_pest) + " ${getTranslatedPestName(box.clsName)}",
                                    style = CustomTextStyles.pestName,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = stringResource(R.string.confidence) + " ${box.cnf?.times(100)?.toInt() ?: 0}%",
                                    style = CustomTextStyles.confidence,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (index < detection.boundingBoxes.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.no_detection),
                            style = CustomTextStyles.cardTitle,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Delete Button
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Confirmation Dialog for Deleting One Detection
            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    message = stringResource(R.string.confirm_delete_one),
                    onConfirm = {
                        viewModel.deleteDetection(detection.detection.id, userId ?: 0, isDescending)
                        resetPestSelection()
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = message,
                style = AppTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    style = CustomTextStyles.buttonText,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = CustomTextStyles.buttonText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun SortButton(text: String, isDescending: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = if (isDescending) "$text ▼" else "$text ▲",
            style = CustomTextStyles.buttonText,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

// Function to format the timestamp into a readable date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}