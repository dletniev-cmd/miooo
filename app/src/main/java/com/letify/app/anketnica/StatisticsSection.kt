package com.letify.app.anketnica

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors
import kotlin.math.max

@Composable
fun StatisticsSection(onOpenMenu: () -> Unit) {
    val data = LocalAnketnica.current
    val scroll = rememberScrollState()

    Column(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        SectionHeader(title = "Статистика", onOpenMenu = onOpenMenu)

        Column(
            Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 18.dp).padding(bottom = 40.dp),
        ) {
            // Верхние 2×2 карточки-метрики
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "Всего", data.total, "clipboard-list-bold-duotone", LetifyColors.TileBlue)
                StatCard(Modifier.weight(1f), "На рассмотрении", data.pendingCount, "clock-circle-bold-duotone", LetifyColors.TileOrange)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "Принято", data.acceptedCount, "check-circle-bold", LetifyColors.TileGreen)
                StatCard(Modifier.weight(1f), "Отклонено", data.rejectedCount, "close-circle-bold-duotone", LetifyColors.TileRed)
            }

            Spacer(Modifier.height(16.dp))

            // Кольцо доли принятия
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Letify.colors.container, RoundedCornerShape(24.dp))
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AcceptRateRing(rate = data.acceptRate)
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text("Доля принятия", color = Letify.colors.text, style = Letify.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Из рассмотренных ${data.acceptedCount + data.rejectedCount} анкет принято ${data.acceptedCount}",
                            color = Letify.colors.muted,
                            style = Letify.typography.bodyMedium,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Разбивка по ролям
            Text(
                "ПО РОЛЯМ",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 10.dp, bottom = 8.dp),
            )
            Box(
                Modifier.fillMaxWidth().background(Letify.colors.container, RoundedCornerShape(22.dp)).padding(14.dp),
            ) {
                Column {
                    val maxCount = max(1, data.roles.maxOfOrNull { data.countForRole(it.id) } ?: 1)
                    data.roles.forEachIndexed { i, role ->
                        val count = data.countForRole(role.id)
                        if (i > 0) Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SolarIcon(name = role.icon, tint = role.tile, size = 18.dp)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Row {
                                    Text(role.name, color = Letify.colors.text, style = Letify.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Text("$count", color = Letify.colors.muted, style = Letify.typography.titleSmall, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(6.dp))
                                // Полоска
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Letify.colors.track),
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(count.toFloat() / maxCount)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(role.tile),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: Int, icon: String, color: Color) {
    Box(
        modifier
            .background(Letify.colors.container, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Column {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.16f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                SolarIcon(name = icon, tint = color, size = 22.dp)
            }
            Spacer(Modifier.height(12.dp))
            Text("$value", color = Letify.colors.text, style = Letify.typography.displayMedium, fontWeight = FontWeight.Bold)
            Text(label, color = Letify.colors.muted, style = Letify.typography.bodySmall)
        }
    }
}

@Composable
private fun AcceptRateRing(rate: Float) {
    val accent = Letify.colors.accent
    val track = Letify.colors.track
    Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(Modifier.size(92.dp)) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = 360f * rate.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
            )
        }
        Text(
            "${(rate * 100).toInt()}%",
            color = Letify.colors.text,
            style = Letify.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}