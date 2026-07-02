package com.letify.app.anketnica

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.Chip
import com.letify.app.ui.components.RoundedSlideOverlay
import com.letify.app.ui.components.rememberParallaxProgress
import com.letify.app.ui.components.OverlayHost
import com.letify.app.ui.components.NoFeedbackButton
import com.letify.app.ui.components.noFeedbackClick
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors

private enum class AppFilter(val title: String) {
    New("Новые"),
    All("Все"),
    Accepted("Принятые"),
    Rejected("Отклонённые"),
    Oldest("Сначала старые"),
}

@Composable
fun ApplicationsSection(onOpenMenu: () -> Unit) {
    val data = LocalAnketnica.current
    var filter by remember { mutableStateOf(AppFilter.New) }
    var openAppId by remember { mutableStateOf<Int?>(null) }

    val parallax = rememberParallaxProgress()

    Box(Modifier.fillMaxSize()) {
        OverlayHost(parallaxProgress = parallax) {
            Column(Modifier.fillMaxSize().background(Letify.colors.bg)) {
                SectionHeader(title = "Анкетки", onOpenMenu = onOpenMenu)

                // Чипы фильтра/сортировки
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppFilter.entries.forEach { f ->
                        Chip(text = f.title, active = f == filter, onClick = { filter = f })
                    }
                }

                val list = remember(filter, data.applications.toList()) {
                    val base = data.applications.toList()
                    when (filter) {
                        AppFilter.New -> base.filter { it.status == AppStatus.Pending }
                            .sortedByDescending { it.submittedAt }
                        AppFilter.All -> base.sortedByDescending { it.submittedAt }
                        AppFilter.Accepted -> base.filter { it.status == AppStatus.Accepted }
                            .sortedByDescending { it.submittedAt }
                        AppFilter.Rejected -> base.filter { it.status == AppStatus.Rejected }
                            .sortedByDescending { it.submittedAt }
                        AppFilter.Oldest -> base.sortedBy { it.submittedAt }
                    }
                }

                if (list.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 12.dp, end = 12.dp, top = 6.dp, bottom = 40.dp,
                        ),
                    ) {
                        items(list, key = { it.id }) { app ->
                            ApplicationRow(app = app, onClick = { openAppId = app.id })
                        }
                    }
                }
            }
        }

        // Экран-деталь как выезжающий overlay
        val current = openAppId
        if (current != null) {
            RoundedSlideOverlay(
                parallaxProgress = parallax,
                onDismissed = { openAppId = null },
            ) { animatedBack ->
                ApplicationDetail(appId = current, onBack = animatedBack)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SolarIcon(name = "clipboard-check-outline", tint = Letify.colors.muted, size = 56.dp)
            Spacer(Modifier.height(12.dp))
            Text("Здесь пусто", color = Letify.colors.text, style = Letify.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text("Нет анкет в этой категории", color = Letify.colors.muted, style = Letify.typography.bodyMedium)
        }
    }
}

@Composable
private fun ApplicationRow(app: Application, onClick: () -> Unit) {
    val data = LocalAnketnica.current
    val role = data.roleById(app.roleId)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Letify.colors.container, RoundedCornerShape(20.dp))
            .noFeedbackClick(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarCircle(name = app.name, size = 50.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        app.name,
                        color = Letify.colors.text,
                        style = Letify.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (role != null) {
                        Spacer(Modifier.width(8.dp))
                        RolePill(role = role)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    app.about,
                    color = Letify.colors.muted,
                    style = Letify.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    AnketnicaFormat.relativeTime(app.submittedAt),
                    color = Letify.colors.muted,
                    style = Letify.typography.labelSmall,
                )
                Spacer(Modifier.height(6.dp))
                StatusDot(status = app.status)
            }
        }
    }
}

@Composable
fun AvatarCircle(name: String, size: androidx.compose.ui.unit.Dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(Letify.colors.accent, LetifyColors.TilePink)),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = Letify.typography.titleLarge,
        )
    }
}

@Composable
fun RolePill(role: Role) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(role.tile.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SolarIcon(name = role.icon, tint = role.tile, size = 13.dp)
        Spacer(Modifier.width(4.dp))
        Text(role.name, color = role.tile, style = Letify.typography.labelSmall, maxLines = 1)
    }
}

@Composable
fun StatusDot(status: AppStatus) {
    val (color, icon) = when (status) {
        AppStatus.Pending -> LetifyColors.TileOrange to "clock-circle-bold-duotone"
        AppStatus.Accepted -> LetifyColors.TileGreen to "check-circle-bold"
        AppStatus.Rejected -> LetifyColors.TileRed to "close-circle-bold-duotone"
    }
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        SolarIcon(name = icon, tint = color, size = 15.dp)
    }
}