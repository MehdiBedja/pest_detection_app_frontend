package com.bedjamahdi.scanpestai.screen.pest

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bedjamahdi.scanpestai.R
import com.bedjamahdi.scanpestai.screen.SourceLink
import com.bedjamahdi.scanpestai.screen.user.LanguagePref
import com.bedjamahdi.scanpestai.ui.theme.CustomTextStyles
import org.json.JSONArray
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PestDetailScreen(
    navController: NavController,
    pestName: String
) {
    val context = LocalContext.current
    val currentLanguage = LanguagePref.getLanguage(context)

    var pestInfo by remember { mutableStateOf<PestInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Load pest data
    LaunchedEffect(pestName, currentLanguage) {
        pestInfo = loadSpecificPestData(context, pestName, currentLanguage)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.pest_details),
                            style = CustomTextStyles.sectionHeader.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                actions = {
                    // Invisible spacer for proper centering
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                pestInfo?.let { pest ->
                    PestDetailContent(pest = pest, currentLanguage = currentLanguage)
                } ?: run {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.pest_not_found),
                            style = CustomTextStyles.sectionHeader,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PestDetailContent(pest: PestInfo, currentLanguage: String) {
    val pestImageRes = getPestImageResource(pest.pest)

    val pestNameTranslationsAr1 = mapOf(
        "Grub" to "اليرقة البيضاء",
        "Mole Cricket" to "صرصور الحقل",
        "Wireworm" to "الدودة السلكية",
        "Corn Borer" to "ثاقبة الذرة",
        "Aphids" to "المنّ",
        "Beet Armyworm" to "دودة الجيش",
        "Flax Budworm" to "يرقة براعم الكتان",
        "Lytta Polita" to "ذبابة الزيتون",
        "Legume Blister beetle" to "خنفساء التقرح البقولية",
        "Blister Beetle" to "خنفساء التقرح",
        "Miridae" to "حشرات الميري",
        "Prodenia Litura" to "يرقة الحشد",
        "Cicadellidae" to "النطاطات"
    )

    val pestNameTranslationsFr1 = mapOf(
        "Grub" to "Vers blanc",
        "Mole Cricket" to "Grillon des champs",
        "Wireworm" to "Ver fil de fer",
        "Corn Borer" to "Foreur du maïs",
        "Aphids" to "Pucerons",
        "Beet Armyworm" to "Chenille de la betterave",
        "Flax Budworm" to "Chenille du lin",
        "Lytta Polita" to "Mouche de l'olive",
        "Legume Blister beetle" to "Cantharide des légumineuses",
        "Blister Beetle" to "Cantharide",
        "Miridae" to "Mirides",
        "Prodenia Litura" to "Chenille défoliatrice",
        "Cicadellidae" to "Cicadelles"
    )





    val pestNameTranslationsFr = mapOf(
        "rice leaf roller" to "Enrouleuse des feuilles du riz",
        "rice leaf caterpillar" to "Chenille des feuilles du riz",
        "paddy stem maggot" to "Mouche mineuse de la tige du riz",
        "asiatic rice borer" to "Foreur asiatique du riz",
        "yellow rice borer" to "Foreur jaune du riz",
        "rice gall midge" to "Cécidomyie du riz",
        "Rice Stem fly" to "Mouche de la tige du riz",
        "brown plant hopper" to "Sauteriau brun du riz",
        "white backed plant hopper" to "Sauteriau à dos blanc",
        "small brown plant hopper" to "Petit sauteriau brun",
        "rice water weevil" to "Charançon aquatique du riz",
        "rice leaf hopper" to "Cicadelle du riz",
        "grain spreader thrips" to "Thrips des grains",
        "rice shell pest" to "Parasite de l’enveloppe du riz",
        "grub" to "Vers blanc",
        "mole cricket" to "Grillon-taupe",
        "wireworm" to "Ver fil de fer",
        "white margined moth" to "Noctuelle à marge blanche",
        "black cutworm" to "Noctuelle noire",
        "large cutworm" to "Grande noctuelle coupure",
        "yellow cutworm" to "Noctuelle jaune",
        "red spider" to "Araignée rouge",
        "corn borer" to "Foreur du maïs",
        "army worm" to "Chenille militaire",
        "aphids" to "Pucerons",
        "Potosiabre vitarsis" to "Potosiabre vitarsis",
        "peach borer" to "Foreur du pêcher",
        "english grain aphid" to "Puceron des céréales anglais",
        "green bug" to "Puceron vert",
        "bird cherry-oat aphid" to "Puceron du merisier et de l’avoine",
        "wheat blossom midge" to "Cécidomyie du blé",
        "penthaleus major" to "Penthaleus major",
        "long legged spider mite" to "Acarien rouge à longues pattes",
        "wheat phloeo thrips" to "Thrips du blé",
        "wheat sawfly" to "Tenthrède du blé",
        "cerodonta denticornis" to "Cerodonta denticornis",
        "beet fly" to "Mouche de la betterave",
        "flea beetle" to "Altise",
        "cabbage army worm" to "Chenille de la chou",
        "beet army worm" to "Chenille de la betterave",
        "Beet spot flies" to "Mouches de la tache de la betterave",
        "meadow moth" to "Noctuelle des prés",
        "beet weevil" to "Charançon de la betterave",
        "sericaorient alismots chulsky" to "Sericaorient alismots chulsky",
        "alfalfa weevil" to "Charançon de la luzerne",
        "flax budworm" to "Tordeuse du lin",
        "alfalfa plant bug" to "Punaise de la luzerne",
        "tarnished plant bug" to "Punaise terne",
        "Locustoidea" to "Locustoidea",
        "lytta polita" to "Lytta polita",
        "legume blister beetle" to "Cantharide des légumineuses",
        "blister beetle" to "Cantharide",
        "therioaphis maculata Buckton" to "Therioaphis maculata Buckton",
        "odontothrips loti" to "Odontothrips loti",
        "Thrips" to "Thrips",
        "alfalfa seed chalcid" to "Chalcide de la graine de luzerne",
        "Pieris canidia" to "Pieris canidia",
        "Apolygus lucorum" to "Apolygus lucorum",
        "Limacodidae" to "Limacodidae",
        "Viteus vitifoliae" to "Viteus vitifoliae (Phylloxéra de la vigne)",
        "Colomerus vitis" to "Colomerus vitis",
        "Brevipalpus lewisi McGregor" to "Brevipalpus lewisi McGregor",
        "oides decempunctata" to "Oides decempunctata",
        "Polyphagotarsonemus latus" to "Polyphagotarsonemus latus (Acarien des bourgeons)",
        "Pseudococcus comstocki Kuwana" to "Pseudococcus comstocki Kuwana",
        "parathrene regalis" to "Parathrene regalis",
        "Ampelophaga" to "Ampelophaga",
        "Lycorma delicatula" to "Lycorma delicatula (Lycorme délicate)",
        "Xylotrechus" to "Xylotrechus",
        "Cicadella viridis" to "Cicadelle verte (Cicadella viridis)",
        "Miridae" to "Mirides",
        "Trialeurodes vaporariorum" to "Aleurode des serres",
        "Erythroneura apicalis" to "Erythroneura apicalis",
        "Papilio xuthus" to "Papilio xuthus (Papillon xuthus)",
        "Panonchus citri McGregor" to "Panonchus citri McGregor (Acarien des agrumes)",
        "Phyllocoptes oleiverus ashmead" to "Phyllocoptes oleiverus ashmead",
        "Icerya purchasi Maskell" to "Icerya purchasi Maskell (Cochénille australienne)",
        "Unaspis yanonensis" to "Unaspis yanonensis",
        "Ceroplastes rubens" to "Ceroplastes rubens",
        "Chrysomphalus aonidum" to "Chrysomphalus aonidum",
        "Parlatoria zizyphus Lucus" to "Parlatoria zizyphus Lucus",
        "Nipaecoccus vastalor" to "Nipaecoccus vastalor",
        "Aleurocanthus spiniferus" to "Aleurocanthus spiniferus (Aleurode épineux)",
        "Tetradacus c Bactrocera minax" to "Bactrocera minax",
        "Dacus dorsalis(Hendel)" to "Dacus dorsalis (Hendel)",
        "Bactrocera tsuneonis" to "Bactrocera tsuneonis",
        "Prodenia litura" to "Chenille défoliatrice (Prodenia litura)",
        "Adristyrannus" to "Adristyrannus",
        "Phyllocnistis citrella Stainton" to "Mineuse des agrumes (Phyllocnistis citrella)",
        "Toxoptera citricidus" to "Puceron brun des agrumes",
        "Toxoptera aurantii" to "Puceron noir des agrumes",
        "Aphis citricola Vander Goot" to "Puceron vert des agrumes",
        "Scirtothrips dorsalis Hood" to "Thrips des fruits (Scirtothrips dorsalis)",
        "Dasineura sp" to "Cécidomyie (Dasineura sp.)",
        "Lawana imitata Melichar" to "Lawana imitata Melichar",
        "Salurnis marginella Guerr" to "Salurnis marginella Guerr",
        "Deporaus marginatus Pascoe" to "Deporaus marginatus Pascoe",
        "Chlumetia transversa" to "Chlumetia transversa",
        "Mango flat beak leafhopper" to "Cicadelle à bec plat du manguier",
        "Rhytidodera bowrinii white" to "Rhytidodera bowrinii White",
        "Sternochetus frigidus" to "Sternochetus frigidus",
        "Cicadellidae" to "Cicadelles"
    )

    val pestNameTranslationsAr = mapOf(
        "rice leaf roller" to "حفّار أوراق الأرز",
        "rice leaf caterpillar" to "يرقة أوراق الأرز",
        "paddy stem maggot" to "ذُبابة ساق الأرز",
        "asiatic rice borer" to "حفّار الأرز الآسيوي",
        "yellow rice borer" to "حفّار الأرز الأصفر",
        "rice gall midge" to "بعوضة أورام الأرز",
        "Rice Stem fly" to "ذُبابة ساق الأرز",
        "brown plant hopper" to "نطاط النباتات البني",
        "white backed plant hopper" to "نطاط النباتات أبيض الظهر",
        "small brown plant hopper" to "نطاط النباتات البني الصغير",
        "rice water weevil" to "سوسة ماء الأرز",
        "rice leaf hopper" to "نطاط أوراق الأرز",
        "grain spreader thrips" to "تربس مبعثر الحبوب",
        "rice shell pest" to "آفة قشرة الأرز",
        "grub" to "يرقة الحشرة (الدودة البيضاء)",
        "mole cricket" to "صرصار الخُلد",
        "wireworm" to "الدودة السلكية",
        "white margined moth" to "عُثة بيضاء الحواف",
        "black cutworm" to "دودة قطع سوداء",
        "large cutworm" to "دودة قطع كبيرة",
        "yellow cutworm" to "دودة قطع صفراء",
        "red spider" to "العنكبوت الأحمر",
        "corn borer" to "حفّار الذرة",
        "army worm" to "دودة الحشد",
        "aphids" to "المنّ",
        "Potosiabre vitarsis" to "خنفساء بيتوسيابري",
        "peach borer" to "حفّار الخوخ",
        "english grain aphid" to "منّ الحبوب الإنجليزي",
        "green bug" to "بقّة خضراء",
        "bird cherry-oat aphid" to "منّ الكرز الطائر-الشوفان",
        "wheat blossom midge" to "بعوضة أزهار القمح",
        "penthaleus major" to "عنكبوت بنتاليوس الكبير",
        "long legged spider mite" to "عنكبوت طويل الأرجل",
        "wheat phloeo thrips" to "تربس القمح",
        "wheat sawfly" to "ذُبابة منشارية القمح",
        "cerodonta denticornis" to "سيرودونتا دنتيكورنس",
        "beet fly" to "ذبابة البنجر",
        "flea beetle" to "خنفساء البرغوث",
        "cabbage army worm" to "دودة الحشد للكرنب",
        "beet army worm" to "دودة الحشد للبنجر",
        "Beet spot flies" to "ذباب بقع البنجر",
        "meadow moth" to "عُثة المروج",
        "beet weevil" to "سوسة البنجر",
        "sericaorient alismots chulsky" to "سيريكا أورينتاليس موتس شولسكي",
        "alfalfa weevil" to "سوسة البرسيم",
        "flax budworm" to "دودة براعم الكتان",
        "alfalfa plant bug" to "بقّة البرسيم",
        "tarnished plant bug" to "بقّة النبات الملطخة",
        "Locustoidea" to "الجراديات (لوكوستويديا)",
        "lytta polita" to "خنفساء ليطا بوليتا",
        "legume blister beetle" to "خنفساء فقاعية للبقوليات",
        "blister beetle" to "الخنفساء الفقاعية",
        "therioaphis maculata Buckton" to "منّ ثيريوفيس الملطخ",
        "odontothrips loti" to "أودونتوتربس لوطي",
        "Thrips" to "التربس",
        "alfalfa seed chalcid" to "حشرة البرسيم البذورية",
        "Pieris canidia" to "فراشة بيريس كانيديا",
        "Apolygus lucorum" to "بقّة أبوليغوس لوكوروم",
        "Limacodidae" to "عائلة ليمكوديداي (عُث الحلزون)",
        "Viteus vitifoliae" to "فيتيوس فيتيفوليا (آفة الكرمة)",
        "Colomerus vitis" to "كولوميروس فيتيس (عث العنب)",
        "Brevipalpus lewisi McGregor" to "بريفالبوس لويسي",
        "oides decempunctata" to "أويدس ديكيمبونكتاتا",
        "Polyphagotarsonemus latus" to "عث بوليفاغوتارسونيموس الواسع",
        "Pseudococcus comstocki Kuwana" to "بسيودوكوكوس كومستوكاي (بق الدقيق)",
        "parathrene regalis" to "باراثرين ريجاليس",
        "Ampelophaga" to "أمبلوفاجا",
        "Lycorma delicatula" to "ليكورما ديليكولاتا",
        "Xylotrechus" to "زيلوتريكوس",
        "Cicadella viridis" to "سيكاديلا فيريديس (نطاط أخضر)",
        "Miridae" to "الميريدات (بقّات نباتية)",
        "Trialeurodes vaporariorum" to "الذُبابة البيضاء للبيوت المحمية",
        "Erythroneura apicalis" to "إريثرونيورا أبيكاليس",
        "Papilio xuthus" to "فراشة بابيليو زوثوس",
        "Panonchus citri McGregor" to "بانونكوس الحمضيات",
        "Phyllocoptes oleiverus ashmead" to "فيلوكوبتس الزيتون",
        "Icerya purchasi Maskell" to "البق القشري القطني",
        "Unaspis yanonensis" to "أوناسبيس يانونينسيس",
        "Ceroplastes rubens" to "سيروبلاستيس روبنس (حشرة قشرية شمعية)",
        "Chrysomphalus aonidum" to "كريزومفالوس أونيدوم",
        "Parlatoria zizyphus Lucus" to "بارلاتوريا زيزيڤوس",
        "Nipaecoccus vastalor" to "نيبايكوكوس فاستالور",
        "Aleurocanthus spiniferus" to "أليوروكانثوس سبينيفيروس",
        "Tetradacus c Bactrocera minax" to "تتراداكوس / باكتروسيرا مينكس",
        "Dacus dorsalis(Hendel)" to "داكوس دورساليس",
        "Bactrocera tsuneonis" to "باكتروسيرا تسونيونيس",
        "Prodenia litura" to "برودينيا ليتورا",
        "Adristyrannus" to "أدريستيرانوس",
        "Phyllocnistis citrella Stainton" to "فيلوكنيستيس سيتريلا (حفّار أوراق الحمضيات)",
        "Toxoptera citricidus" to "توكزوبتيرا سيتريسيدوس (منّ الحمضيات الأسود)",
        "Toxoptera aurantii" to "توكزوبتيرا أورانتي (منّ الحمضيات)",
        "Aphis citricola Vander Goot" to "أفيس سيتريكولا (منّ الحمضيات)",
        "Scirtothrips dorsalis Hood" to "سكيرتو تربس دورساليس",
        "Dasineura sp" to "بعوضة داسينورا",
        "Lawana imitata Melichar" to "لاوانا إيميتاتا",
        "Salurnis marginella Guerr" to "سالورنس مارجينيلا",
        "Deporaus marginatus Pascoe" to "ديبوراوس مارجيناتوس",
        "Chlumetia transversa" to "خلوميتيا ترانزفيرسا",
        "Mango flat beak leafhopper" to "نطاط أوراق المانجو عريض المنقار",
        "Rhytidodera bowrinii white" to "ريثيدوديرا بواريني",
        "Sternochetus frigidus" to "ستيرنوكيتوس فريجيدوس",
        "Cicadellidae" to "السيكاديلداي (نطاطات الأوراق)"
    )




    val displayName = when (currentLanguage) {
        "ar" -> pestNameTranslationsAr[pest.pest] ?: pest.pest
        "fr" -> pestNameTranslationsFr[pest.pest] ?: pest.pest
        else -> pest.pest
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = pestImageRes),
                    contentDescription = displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                )

                // Pest name overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = displayName,
                        style = CustomTextStyles.welcomeText.copy(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Pest Information Cards
        PestInfoCard(
            title = stringResource(R.string.crop_category),
            content = pest.category,
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        )

        PestInfoCard(
            title = stringResource(R.string.treatment_recommendation),
            content = pest.recommendation,
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        )


        PestInfoCard1(
            title = stringResource(R.string.source),
            content = {SourceLink1(pest.source) },
            gradientColors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            )
        )


    }
}




@Composable
fun PestInfoCard1(
    title: String,
    content: @Composable () -> Unit, // Changed from String to Composable
    gradientColors: List<Color>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = CustomTextStyles.cardTitle.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )

                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                content() // Call the composable content
            }
        }
    }
}
@Composable
fun PestInfoCard(
    title: String,
    content: String,
    gradientColors: List<Color>,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = CustomTextStyles.cardTitle.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )

                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                Text(
                    text = content,
                    style = CustomTextStyles.noteContent.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}
@Composable
fun SourceLink1(
    url: String
) {
    val context = LocalContext.current

    val displayUrl = remember(url) {
        url.removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("/")
    }

    Row {
        Text(
            text = stringResource(R.string.view_on) + " ",
            style = CustomTextStyles.noteContent.copy( // Match the card's text style
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        )

        ClickableText(
            text = AnnotatedString(displayUrl),
            style = CustomTextStyles.noteContent.copy( // Match the card's text style
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                lineHeight = 20.sp
            ),
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
        )
    }
}

fun loadSpecificPestData(context: Context, pestName: String, language: String): PestInfo? {
    return try {
        val fileName = when (language) {
            "ar" -> "pests_ar3.json"
            "fr" -> "pests_fr3.json"
            else -> "pestInfo3.json"
        }

        val inputStream = context.assets.open(fileName)
        val json = inputStream.bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(json)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val pest = jsonObject.getString("pest")
            if (pest == pestName) {
                return PestInfo(
                    pest = pest,
                    category = jsonObject.getString("category"),
                    recommendation = jsonObject.getString("recommendation"),
                    source = jsonObject.getString("source")
                )
            }
        }
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}