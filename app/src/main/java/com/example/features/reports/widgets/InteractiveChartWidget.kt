package com.example.features.reports.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.reports.models.ChartDataPoint
import com.example.features.reports.models.PieChartSegment

enum class ChartType {
    LINE, BAR, PIE, DONUT, AREA
}

@Composable
fun InteractiveChartWidget(
    title: String,
    chartType: ChartType,
    dataPoints: List<ChartDataPoint> = emptyList(),
    pieSegments: List<PieChartSegment> = emptyList(),
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    currencySymbol: String = "$",
    testTag: String = "interactive_chart_widget",
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (chartType) {
                ChartType.LINE, ChartType.AREA -> LineOrAreaChartCanvas(
                    dataPoints = dataPoints,
                    isArea = chartType == ChartType.AREA,
                    lineColor = primaryColor,
                    currencySymbol = currencySymbol
                )
                ChartType.BAR -> BarChartCanvas(
                    dataPoints = dataPoints,
                    barColor = primaryColor,
                    currencySymbol = currencySymbol
                )
                ChartType.PIE, ChartType.DONUT -> PieOrDonutChartCanvas(
                    segments = pieSegments,
                    isDonut = chartType == ChartType.DONUT,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}

@Composable
private fun LineOrAreaChartCanvas(
    dataPoints: List<ChartDataPoint>,
    isArea: Boolean,
    lineColor: Color,
    currencySymbol: String
) {
    if (dataPoints.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No trend data available", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(dataPoints) {
        animationProgress = 1f
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val textStyle = MaterialTheme.typography.labelSmall

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val step = width / maxOf(1, dataPoints.size - 1)
                            val index = (offset.x / step).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val width = size.width
                val height = size.height - 30.dp.toPx() // Reserve space for bottom labels
                val maxVal = maxOf(1f, dataPoints.maxOf { it.value })

                // Draw Horizontal Grid Lines
                val gridLinesCount = 4
                for (i in 0..gridLinesCount) {
                    val y = height * (1f - i.toFloat() / gridLinesCount)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                }

                // Points path
                val stepX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width / 2
                val points = dataPoints.mapIndexed { idx, pt ->
                    val x = if (dataPoints.size > 1) idx * stepX else width / 2
                    val scaledVal = (pt.value / maxVal) * animatedProgress
                    val y = height - (scaledVal * height)
                    Offset(x, y)
                }

                val strokePath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val p0 = points[i - 1]
                            val p1 = points[i]
                            val controlX = (p0.x + p1.x) / 2
                            cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        }
                    }
                }

                // Fill Area gradient
                if (isArea && points.isNotEmpty()) {
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                }

                // Draw Line
                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw Point circles & selection indicator
                points.forEachIndexed { idx, pt ->
                    val isSelected = selectedIndex == idx
                    drawCircle(
                        color = if (isSelected) Color.White else lineColor,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = lineColor,
                        radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Interactive Tooltip Overlay
            selectedIndex?.let { idx ->
                if (idx in dataPoints.indices) {
                    val selectedPoint = dataPoints[idx]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shadowElevation = 4.dp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(
                            text = "${selectedPoint.label}: $currencySymbol${String.format("%.2f", selectedPoint.value)}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEachIndexed { idx, pt ->
                if (dataPoints.size <= 7 || idx % (dataPoints.size / 5) == 0) {
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BarChartCanvas(
    dataPoints: List<ChartDataPoint>,
    barColor: Color,
    currencySymbol: String
) {
    if (dataPoints.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 700)
    )

    LaunchedEffect(dataPoints) { animationProgress = 1f }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val step = width / dataPoints.size
                            val index = (offset.x / step).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val width = size.width
                val height = size.height - 30.dp.toPx()
                val maxVal = maxOf(1f, dataPoints.maxOf { it.value })

                val barWidth = (width / dataPoints.size) * 0.55f
                val spacing = (width / dataPoints.size)

                dataPoints.forEachIndexed { idx, pt ->
                    val barHeight = (pt.value / maxVal) * height * animatedProgress
                    val x = (idx * spacing) + (spacing - barWidth) / 2
                    val y = height - barHeight

                    val isSelected = selectedIndex == idx
                    val currentBarColor = if (isSelected) barColor else barColor.copy(alpha = 0.85f)

                    drawRoundRect(
                        color = currentBarColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            selectedIndex?.let { idx ->
                if (idx in dataPoints.indices) {
                    val pt = dataPoints[idx]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shadowElevation = 4.dp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    ) {
                        Text(
                            text = "${pt.label}: $currencySymbol${String.format("%.2f", pt.value)}",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            dataPoints.forEach { pt ->
                Text(
                    text = pt.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PieOrDonutChartCanvas(
    segments: List<PieChartSegment>,
    isDonut: Boolean,
    currencySymbol: String
) {
    if (segments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No category breakdown data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(segments) { animationProgress = 1f }

    val totalValue = segments.sumOf { it.value.toDouble() }.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                val strokeWidth = if (isDonut) 36.dp.toPx() else size.width / 2

                segments.forEachIndexed { idx, seg ->
                    val sweepAngle = (seg.value / totalValue) * 360f * animatedProgress
                    val isSelected = selectedIndex == idx
                    val inset = if (isSelected) 6.dp.toPx() else 0f

                    drawArc(
                        color = seg.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = !isDonut,
                        style = if (isDonut) Stroke(width = strokeWidth) else androidx.compose.ui.graphics.drawscope.Fill
                    )

                    startAngle += sweepAngle
                }
            }

            if (isDonut) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${String.format("%.0f", totalValue)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEach { seg ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(seg.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${seg.category} (${String.format("%.1f", seg.percentage)}%)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
