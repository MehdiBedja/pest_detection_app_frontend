package com.example.pest_detection_app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

// MPAndroidChart imports
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

// Gson imports
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Your app imports
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes
import com.example.pest_detection_app.screen.user.DarkModePref
import com.example.pest_detection_app.ui.theme.*
import com.github.mikephil.charting.formatter.PercentFormatter

// Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Date formatting
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


data class TimeSeriesData(
    val entries: List<Entry>,
    val labels: List<String>,
    val title: String
)

enum class TimePeriod {
    WEEK, MONTH, YEAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsDashboardScreen(
    navController: NavHostController,
    userViewModel: LoginViewModel,
    detectionSaveViewModel: DetectionSaveViewModel
) {
    val context = LocalContext.current
    var statsData by remember { mutableStateOf<StatsData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val userId by userViewModel.userId.collectAsState()

    // Get dark mode preference
    val isDarkMode = remember { mutableStateOf(DarkModePref.getDarkMode(context)) }
    val textColor = if (isDarkMode.value) Color.White else Color.Black

    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.MONTH) }
    var timeOffset by remember { mutableStateOf(0) }

    // Load pest info from JSON
    val pestInfo = remember { loadPestInfo(context) }

    val syncCompletedEvent by detectionSaveViewModel.syncCompletedEvent.collectAsState(initial = null)

    // Initial data fetch
    LaunchedEffect(userId) {
        userId?.let { id ->
            detectionSaveViewModel.getSortedDetections(id, true)
        }
    }

    val detections by detectionSaveViewModel.detections.collectAsState()

    // Recalculate stats on detection list change
    LaunchedEffect(detections) {
        if (detections.isNotEmpty()) {
            statsData = withContext(Dispatchers.Default) {
                calculateStats(detections, pestInfo)
            }
        }
        isLoading = false
    }

    // Sync response handler
    LaunchedEffect(syncCompletedEvent) {
        syncCompletedEvent?.let { syncResult ->
            when (syncResult) {
                is DetectionSaveViewModel.SyncResult.Success,
                is DetectionSaveViewModel.SyncResult.Failure -> {
                    userId?.let { detectionSaveViewModel.getSortedDetections(it, true) }
                }
            }
            detectionSaveViewModel.clearSyncResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.stats_dashboard),
                    style = CustomTextStyles.sectionHeader.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (statsData == null || detections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_detection_data),
                            style = CustomTextStyles.sectionTitle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Cards Section (unchanged)
                item {
                    Text(
                        text = stringResource(R.string.summary),
                        style = AppTypography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item {
                            SummaryCard(
                                title = stringResource(R.string.total_detections),
                                value = statsData!!.totalDetections.toString(),
                                icon = painterResource(id = R.drawable.assessment),
                                color = MarronColor
                            )
                        }
                        item {
                            SummaryCard(
                                title = stringResource(R.string.most_frequent_pest),
                                value = statsData!!.mostFrequentPest,
                                icon = painterResource(id = R.drawable.assessment),
                                color = MarronColor
                            )
                        }
                        item {
                            SummaryCard(
                                title = stringResource(R.string.last_detected),
                                value = statsData!!.lastDetectedPest,
                                icon = painterResource(id = R.drawable.assessment),
                                color = Color(0xFF8B5CF6)
                            )
                        }
                        item {
                            SummaryCard(
                                title = stringResource(R.string.detection_trend),
                                value = statsData!!.detectionTrend,
                                icon = if (statsData!!.detectionTrend == "↗ Increasing") painterResource(id = R.drawable.trendingup1) else painterResource(id = R.drawable.assessment),
                                color = if (statsData!!.detectionTrend == "↗ Increasing") Color(0xFFEF4444) else AccentGreen
                            )
                        }
                    }
                }

                // Line Chart - Detections over Time (updated with dynamic colors)
                item {
                    ChartCard(
                        title = stringResource(R.string.detections_over_time),
                        icon = painterResource(id = R.drawable.assessment)
                    ) {
                        Column {
                            // Time Period Selection Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    TimePeriod.values().forEach { period ->
                                        FilterChip(
                                            onClick = {
                                                selectedTimePeriod = period
                                                timeOffset = 0
                                            },
                                            label = {
                                                Text(
                                                    text = when (period) {
                                                        TimePeriod.WEEK -> "Week"
                                                        TimePeriod.MONTH -> "Month"
                                                        TimePeriod.YEAR -> "Year"
                                                    },
                                                )
                                            },
                                            selected = selectedTimePeriod == period,
                                            modifier = Modifier.height(32.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                labelColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            val timeSeriesData = remember(selectedTimePeriod, timeOffset, detections) {
                                generateTimeSeriesData(detections, selectedTimePeriod, timeOffset)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { timeOffset-- }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Previous")
                                }

                                Text(
                                    text = timeSeriesData.title,
                                    style = AppTypography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )

                                IconButton(onClick = { timeOffset++ }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Next")
                                }
                            }

                            AndroidView(
                                factory = { context ->
                                    LineChart(context).apply {
                                        setupEnhancedLineChart(this, timeSeriesData, textColor)
                                    }
                                },
                                update = { chart ->
                                    setupEnhancedLineChart(chart, timeSeriesData, textColor)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            )
                        }
                    }
                }

                // Pie Chart - Detection Distribution (updated with dynamic colors)
                item {
                    ChartCard(
                        title = stringResource(R.string.detection_distribution),
                        icon = painterResource(id = R.drawable.assessment)
                    ) {
                        AndroidView(
                            factory = { context ->
                                PieChart(context).apply {
                                    setupPieChart(this, statsData!!.pestCounts, textColor)
                                }
                            },
                            update = { chart ->
                                setupPieChart(chart, statsData!!.pestCounts, textColor)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                }

                // Crop Type Association Chart (updated with dynamic colors)
                item {
                    ChartCard(
                        title = stringResource(R.string.crop_type_distribution),
                        icon = painterResource(id = R.drawable.assessment)
                    ) {
                        AndroidView(
                            factory = { context ->
                                HorizontalBarChart(context).apply {
                                    setupCropChart(this, statsData!!.cropTypeCounts, textColor)
                                }
                            },
                            update = { chart ->
                                setupCropChart(chart, statsData!!.cropTypeCounts, textColor)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        )
                    }
                }

                // Calendar Heatmap (unchanged)
                item {
                    ChartCard(
                        title = stringResource(R.string.detection_calendar),
                        icon = painterResource(id = R.drawable.assessment)
                    ) {
                        CalendarHeatmapView(statsData!!.dailyDetections)
                    }
                }
            }
        }
    }
}
@Composable
fun SummaryCard(
    title: String,
    value: String,
    icon: Painter,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = AppTypography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = AppTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ChartCard(
    title: String,
    icon: Painter,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}



@Composable
fun CalendarHeatmapView(dailyDetections: Map<String, Int>) {
    var monthOffset by remember { mutableStateOf(0) } // control month change
    val baseCalendar = Calendar.getInstance()
    baseCalendar.add(Calendar.MONTH, monthOffset)

    // Clone the adjusted calendar
    val currentMonth = baseCalendar.clone() as Calendar
    val maxDetections = dailyDetections.values.maxOrNull() ?: 1

    val days = remember(currentMonth.timeInMillis) {
        val daysList = mutableListOf<DayCell>()

        // Start from the 1st of the month
        currentMonth.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = currentMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

        // Pad with empty cells before the first day
        for (i in 0 until firstDayOfWeek) {
            daysList.add(DayCell("", 0))
        }

        val totalDays = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..totalDays) {
            currentMonth.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentMonth.time)
            val count = dailyDetections[dateStr] ?: 0
            daysList.add(DayCell(day.toString(), count))
        }

        daysList
    }

    val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)

    Column(modifier = Modifier.padding(8.dp)) {

        // 🔁 Month header + navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { monthOffset-- }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }
            Text(
                text = monthTitle,
                style = AppTypography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { monthOffset++ }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🗓 Weekday headers
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(text = it, style = AppTypography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 📊 Grid of days
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(days) { day ->
                val intensity = if (maxDetections > 0) day.count.toFloat() / maxDetections else 0f
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (day.label.isNotEmpty()) {
                                AccentGreen.copy(alpha = 0.2f + intensity * 0.8f)
                            } else Color.Transparent
                        )
                        .border(
                            0.5.dp,
                            Color.LightGray.copy(alpha = if (day.label.isNotEmpty()) 0.3f else 0f),
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (day.label.isNotEmpty()) {
                        Text(
                            text = day.label,
                            style = AppTypography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

data class DayCell(
    val label: String,
    val count: Int
)


// Chart setup functions
private fun setupBarChart(chart: BarChart, pestCounts: Map<String, Int>, color: Color) {
    val entries = pestCounts.entries.mapIndexed { index, entry ->
        BarEntry(index.toFloat(), entry.value.toFloat())
    }

    val dataSet = BarDataSet(entries, "").apply {
        colors = ColorTemplate.MATERIAL_COLORS.toList()
        valueTextSize = 12f
    }

    chart.data = BarData(dataSet)
    chart.description.isEnabled = false
    chart.setFitBars(true)
    chart.animateY(1000)

    // X-Axis customization
    val xAxis = chart.xAxis
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.textColor = Color.Red.toArgb() // Custom color for X-axis labels
    xAxis.textSize = 12f // Optional: adjust text size

    xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index < pestCounts.keys.size) {
                pestCounts.keys.elementAt(index)
            } else ""
        }
    }
    xAxis.granularity = 1f
    xAxis.labelRotationAngle = -45f

    // Y-Axis customization (Left axis)
    val leftAxis = chart.axisLeft
    leftAxis.textColor = Color.Blue.toArgb() // Custom color for left Y-axis labels
    leftAxis.textSize = 12f // Optional: adjust text size

    // Y-Axis customization (Right axis) - if you want to disable or customize it
    val rightAxis = chart.axisRight
    rightAxis.isEnabled = false // Disable right axis, or customize it like left axis
    // rightAxis.textColor = Color.GREEN.toArgb() // Uncomment if you want to show right axis

    chart.invalidate()
}

private fun setupEnhancedLineChart(chart: LineChart, timeSeriesData: TimeSeriesData, textColor: Color) {
    if (timeSeriesData.entries.isEmpty()) {
        chart.clear()
        chart.setNoDataText("No data available for this period")
        return
    }

    val dataSet = LineDataSet(timeSeriesData.entries, "Detections").apply {
        color = AccentGreen.toArgb()
        setCircleColor(AccentGreen.toArgb())
        lineWidth = 3f
        circleRadius = 5f
        setDrawValues(false)
        setDrawFilled(true)
        fillAlpha = 50
        fillColor = AccentGreen.toArgb()
        mode = LineDataSet.Mode.LINEAR
    }

    chart.data = LineData(dataSet)
    chart.description.isEnabled = false
    chart.legend.isEnabled = false
    chart.animateX(800)

    // Configure X-axis with dynamic text color
    chart.xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index < timeSeriesData.labels.size) {
                    timeSeriesData.labels[index]
                } else ""
            }
        }
        granularity = 1f
        setDrawGridLines(true)
        gridColor = GrayText.copy(alpha = 0.3f).toArgb()
        textSize = 10f
        this.textColor = textColor.toArgb()
        labelRotationAngle = if (timeSeriesData.labels.size > 7) -45f else 0f
    }

    // Configure Y-axis with dynamic text color
    chart.axisLeft.apply {
        setDrawGridLines(true)
        gridColor = GrayText.copy(alpha = 0.3f).toArgb()
        textSize = 10f
        this.textColor = textColor.toArgb()
    }

    chart.axisRight.isEnabled = false
    chart.setTouchEnabled(true)
    chart.setDragEnabled(true)
    chart.setScaleEnabled(false)
    chart.setPinchZoom(false)

    chart.invalidate()
}

// Add this function to generate time series data based on the selected period
private fun generateTimeSeriesData(
    detections: List<DetectionWithBoundingBoxes>,
    timePeriod: TimePeriod,
    offset: Int
): TimeSeriesData {
    val calendar = Calendar.getInstance()
    val entries = mutableListOf<Entry>()
    val labels = mutableListOf<String>()
    val detectionCounts = mutableMapOf<String, Int>()

    // Count detections by date
    detections.forEach { detection ->
        val date = Date(detection.detection.detectionDate)
        val dateStr = when (timePeriod) {
            TimePeriod.WEEK -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            TimePeriod.MONTH -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            TimePeriod.YEAR -> SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(date)
        }
        detectionCounts[dateStr] = (detectionCounts[dateStr] ?: 0) + detection.boundingBoxes.size
    }

    when (timePeriod) {
        TimePeriod.WEEK -> {
            // Generate 7 days for the selected week
            calendar.add(Calendar.WEEK_OF_YEAR, offset)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            val weekStart = calendar.clone() as Calendar
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("EEE", Locale.getDefault())

            repeat(7) { day ->
                val dateStr = dateFormat.format(calendar.time)
                val displayStr = displayFormat.format(calendar.time)
                val count = detectionCounts[dateStr] ?: 0

                entries.add(Entry(day.toFloat(), count.toFloat()))
                labels.add(displayStr)

                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val weekTitle = SimpleDateFormat("'Week of' MMM dd, yyyy", Locale.getDefault())
                .format(weekStart.time)
            return TimeSeriesData(entries, labels, weekTitle)
        }

        TimePeriod.MONTH -> {
            // Generate weeks for the selected month
            calendar.add(Calendar.MONTH, offset)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val monthStart = calendar.clone() as Calendar
            val monthEnd = calendar.clone() as Calendar
            monthEnd.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))

            // Set calendar to first day of first week of the month
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            if (calendar.after(monthStart)) {
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
            }

            val weeklyData = mutableMapOf<Int, Int>()

            // Count detections by week within the month
            detections.forEach { detection ->
                val detectionDate = Calendar.getInstance()
                detectionDate.timeInMillis = detection.detection.detectionDate

                if (detectionDate.get(Calendar.YEAR) == monthStart.get(Calendar.YEAR) &&
                    detectionDate.get(Calendar.MONTH) == monthStart.get(Calendar.MONTH)) {
                    val weekOfMonth = detectionDate.get(Calendar.WEEK_OF_MONTH)
                    weeklyData[weekOfMonth] = (weeklyData[weekOfMonth] ?: 0) + detection.boundingBoxes.size
                }
            }

            // Generate entries for each week in the month
            val maxWeeks = monthEnd.get(Calendar.WEEK_OF_MONTH)
            repeat(maxWeeks) { week ->
                val weekNum = week + 1
                val count = weeklyData[weekNum] ?: 0
                entries.add(Entry(week.toFloat(), count.toFloat()))
                labels.add("Week $weekNum")
            }

            val monthTitle = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(monthStart.time)
            return TimeSeriesData(entries, labels, monthTitle)
        }

        TimePeriod.YEAR -> {
            // Generate months for the selected year
            calendar.add(Calendar.YEAR, offset)
            calendar.set(Calendar.MONTH, Calendar.JANUARY)
            calendar.set(Calendar.DAY_OF_MONTH, 1)

            val yearStart = calendar.clone() as Calendar
            val monthlyData = mutableMapOf<Int, Int>()

            // Count detections by month within the year
            detections.forEach { detection ->
                val detectionDate = Calendar.getInstance()
                detectionDate.timeInMillis = detection.detection.detectionDate

                if (detectionDate.get(Calendar.YEAR) == yearStart.get(Calendar.YEAR)) {
                    val month = detectionDate.get(Calendar.MONTH)
                    monthlyData[month] = (monthlyData[month] ?: 0) + detection.boundingBoxes.size
                }
            }

            // Generate entries for each month in the year
            val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

            repeat(12) { month ->
                val count = monthlyData[month] ?: 0
                entries.add(Entry(month.toFloat(), count.toFloat()))
                labels.add(monthNames[month])
            }

            val yearTitle = SimpleDateFormat("yyyy", Locale.getDefault())
                .format(yearStart.time)
            return TimeSeriesData(entries, labels, yearTitle)
        }
    }
}

private fun setupPieChart(chart: PieChart, pestCounts: Map<String, Int>, textColor: Color) {
    val entries = pestCounts.map { (pest, count) ->
        PieEntry(count.toFloat(), pest)
    }

    val dataSet = PieDataSet(entries, "").apply {
        colors = ColorTemplate.MATERIAL_COLORS.toList()
        valueTextSize = 12f
        valueFormatter = PercentFormatter(chart)
        valueTextColor = textColor.toArgb() // Dynamic text color for values
    }

    chart.data = PieData(dataSet)
    chart.description.isEnabled = false
    chart.setUsePercentValues(true)

    // Configure legend with dynamic text color
    chart.legend.apply {
        this.textColor = textColor.toArgb()
        textSize = 10f
    }

    chart.animateY(1000)
    chart.invalidate()
}

private fun setupCropChart(chart: HorizontalBarChart, cropCounts: Map<String, Int>, textColor: Color) {
    val cropList = cropCounts.keys.toList()
    val totalDetections = cropCounts.values.sum().toFloat()

    val entries = cropList.mapIndexed { index, crop ->
        val count = cropCounts[crop]?.toFloat() ?: 0f
        val percentage = if (totalDetections > 0) (count / totalDetections) * 100f else 0f
        BarEntry(index.toFloat(), percentage)
    }

    val dataSet = BarDataSet(entries, "").apply {
        colors = ColorTemplate.COLORFUL_COLORS.toList()
        valueTextSize = 12f
        valueTextColor = textColor.toArgb() // Dynamic text color for values
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }
    }

    chart.data = BarData(dataSet)
    chart.description.isEnabled = false
    chart.setFitBars(true)
    chart.animateY(1000)

    // Configure X-axis with dynamic text color
    chart.xAxis.apply {
        granularity = 1f
        setDrawLabels(true)
        position = XAxis.XAxisPosition.BOTTOM
        this.textColor = textColor.toArgb()
        textSize = 10f
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return cropList.getOrNull(index) ?: ""
            }
        }
    }

    // Configure Y-axes with dynamic text color - FORCE START FROM 0, MAX AT 100%
    chart.axisLeft.apply {
        this.textColor = textColor.toArgb()
        textSize = 10f
        axisMinimum = 0f // Force Y-axis to start from 0
        axisMaximum = 100f // Set maximum to 100%
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }
    }

    chart.axisRight.apply {
        this.textColor = textColor.toArgb()
        textSize = 10f
        axisMinimum = 0f // Force Y-axis to start from 0
        axisMaximum = 100f // Set maximum to 100%
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }
        }
    }

    chart.legend.apply {
    isEnabled = false
    }

    chart.invalidate()
}
// Data classes and utility functions
data class StatsData(
    val totalDetections: Int,
    val mostFrequentPest: String,
    val lastDetectedPest: String,
    val detectionTrend: String,
    val pestCounts: Map<String, Int>,
    val timeSeriesData: List<Pair<String, Int>>,
    val cropTypeCounts: Map<String, Int>,
    val dailyDetections: Map<String, Int>
)

data class PestInfo(
    val pest: String,
    val category: String,
    val recommendation: String
)

private fun loadPestInfo(context: android.content.Context): List<PestInfo> {
    return try {
        val json = context.assets.open("pestInfo.json").bufferedReader().use { it.readText() }
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<List<PestInfo>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun calculateStats(
    detections: List<DetectionWithBoundingBoxes>,
    pestInfo: List<PestInfo>
): StatsData {
    val pestCounts = mutableMapOf<String, Int>()
    val cropCounts = mutableMapOf<String, Int>()
    val dailyDetections = mutableMapOf<String, Int>()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val timeSeriesMap = mutableMapOf<String, Int>()

    var lastDetectionDate = 0L
    var lastDetectedPest = ""
    var totalPestsFound = 0 // Track total number of pests found

    detections.forEach { detection ->
        val date = Date(detection.detection.detectionDate)
        val dateStr = dateFormat.format(date)
        val monthStr = monthFormat.format(date)

        if (detection.detection.detectionDate > lastDetectionDate) {
            lastDetectionDate = detection.detection.detectionDate
            lastDetectedPest = detection.boundingBoxes.firstOrNull()?.clsName ?: ""
        }

        // Count the number of pests in this detection (number of bounding boxes)
        val pestsInThisDetection = detection.boundingBoxes.size
        totalPestsFound += pestsInThisDetection

        dailyDetections[dateStr] = (dailyDetections[dateStr] ?: 0) + pestsInThisDetection
        timeSeriesMap[monthStr] = (timeSeriesMap[monthStr] ?: 0) + pestsInThisDetection

        detection.boundingBoxes.forEach { box ->
            pestCounts[box.clsName] = (pestCounts[box.clsName] ?: 0) + 1

            val cropType = pestInfo.find { it.pest == box.clsName }?.category ?: "Unknown"
            cropCounts[cropType] = (cropCounts[cropType] ?: 0) + 1
        }
    }

    val mostFrequentPest = pestCounts.maxByOrNull { it.value }?.key ?: "None"

    // Calculate trend (simple: compare last 2 weeks)
    val calendar = Calendar.getInstance()
    val currentWeek = mutableListOf<String>()
    val previousWeek = mutableListOf<String>()

    repeat(7) {
        currentWeek.add(dateFormat.format(calendar.time))
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }

    repeat(7) {
        previousWeek.add(dateFormat.format(calendar.time))
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }

    val currentWeekDetections = currentWeek.sumOf { dailyDetections[it] ?: 0 }
    val previousWeekDetections = previousWeek.sumOf { dailyDetections[it] ?: 0 }

    val trend = when {
        currentWeekDetections > previousWeekDetections -> "↗ Increasing"
        currentWeekDetections < previousWeekDetections -> "↘ Decreasing"
        else -> "→ Stable"
    }

    return StatsData(
        totalDetections = totalPestsFound, // Use total pests found instead of detections.size
        mostFrequentPest = mostFrequentPest,
        lastDetectedPest = lastDetectedPest.ifEmpty { "None" },
        detectionTrend = trend,
        pestCounts = pestCounts,
        timeSeriesData = timeSeriesMap.toList().sortedBy { it.first },
        cropTypeCounts = cropCounts,
        dailyDetections = dailyDetections
    )
}