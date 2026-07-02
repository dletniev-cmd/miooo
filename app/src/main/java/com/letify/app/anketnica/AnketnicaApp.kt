package com.letify.app.anketnica

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.NoFeedbackButton
import com.letify.app.ui.components.noFeedbackClick
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.state.LocalAppState
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors
import com.letify.app.ui.theme.ThemeMode
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Разделы приложения — пункты бокового меню. */
enum class Section(val title: String, val icon: String, val tile: Color) {
    Applications("Анкетки", "clipboard-list-bold-duotone", LetifyColors.TileBlue),
    RolesQuestions("Роли и вопросы", "checklist-minimalistic-bold-duotone", LetifyColors.TileViolet),
    Statistics("Статистика", "chart-2-bold-duotone", LetifyColors.TileGreen),
    Settings("Настройки", "settings-bold-duotone", LetifyColors.TileTeal),
}

private val DrawerEasing = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)
private const val DrawerMs = 320

/**
 * Корневой экран анкетницы: боковое меню-drawer (свайп от левого края + бургер)
 * поверх контента раздела. Полностью на дизайн-системе Letify.
 */
@Composable
fun AnketnicaApp() {
    val data = rememberAnketnicaData()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var section by remember { mutableStateOf(Section.Applications) }

    // 0 = закрыто, 1 = открыто. Один драйвер и для контента, и для затемнения.
    val drawerProgress = remember { Animatable(0f) }
    val drawerWidthDp = 300.dp
    val drawerWidthPx = with(density) { drawerWidthDp.toPx() }

    val openDrawer: () -> Unit = {
        scope.launch { drawerProgress.animateTo(1f, tween(DrawerMs, easing = DrawerEasing)) }
    }
    val closeDrawer: () -> Unit = {
        scope.launch { drawerProgress.animateTo(0f, tween(DrawerMs, easing = DrawerEasing)) }
    }

    CompositionLocalProviderData(data) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Letify.colors.bg)
                // Свайп от левого края открывает меню; свайп влево по контенту — закрывает.
                .pointerInput(Unit) {
                    val edgePx = with(density) { 24.dp.toPx() }
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val opened = drawerProgress.value > 0.5f
                        if (!opened && down.position.x > edgePx) return@awaitEachGesture
                        var consumedH = false
                        val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, dragAmount ->
                            if (abs(dragAmount) > 0) {
                                consumedH = true
                                change.consume()
                            }
                        } ?: return@awaitEachGesture
                        if (!consumedH) return@awaitEachGesture
                        val startProgress = drawerProgress.value
                        val startX = down.position.x
                        var released = false
                        var last = startProgress
                        while (!released) {
                            val event = awaitPointerEvent()
                            val ch = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!ch.pressed) {
                                released = true
                            } else {
                                val dx = ch.position.x - startX
                                last = (startProgress + dx / drawerWidthPx).coerceIn(0f, 1f)
                                ch.consume()
                                scope.launch { drawerProgress.snapTo(last) }
                            }
                        }
                        val target = if (last > 0.4f) 1f else 0f
                        scope.launch {
                            drawerProgress.animateTo(target, tween(DrawerMs, easing = DrawerEasing))
                        }
                    }
                },
        ) {
            // ── Контент раздела: сдвигается вправо при открытии меню ──
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = drawerProgress.value
                        translationX = p * drawerWidthPx * 0.86f
                        val s = 1f - 0.06f * p
                        scaleX = s
                        scaleY = s
                    }
                    .clip(RoundedCornerShape(if (drawerProgress.value > 0.01f) 28.dp else 0.dp)),
            ) {
                SectionScaffold(
                    section = section,
                    onOpenMenu = openDrawer,
                    data = data,
                )
                // Затемнение + перехват тапа для закрытия (поверх контента).
                if (drawerProgress.value > 0.001f) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .drawBehind {
                                drawRect(Color.Black, alpha = 0.32f * drawerProgress.value)
                            }
                            .noFeedbackClick(onClick = closeDrawer),
                    )
                }
            }

            // ── Само боковое меню (выезжает слева) ──
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(drawerWidthDp)
                    .graphicsLayer {
                        translationX = -drawerWidthPx * (1f - drawerProgress.value)
                    },
            ) {
                DrawerContent(
                    active = section,
                    onSelect = {
                        section = it
                        closeDrawer()
                    },
                )
            }
        }
    }
}

@Composable
private fun CompositionLocalProviderData(data: AnketnicaData, content: @Composable () -> Unit) {
    androidx.compose.runtime.CompositionLocalProvider(LocalAnketnica provides data, content = content)
}

/** Тело бокового меню: шапка (аватар + @username), анимированная смена темы, список разделов. */
@Composable
private fun DrawerContent(active: Section, onSelect: (Section) -> Unit) {
    val appState = LocalAppState.current
    val tgUser = appState.telegramUser
    val displayName = tgUser?.displayName?.takeIf { it.isNotBlank() } ?: appState.userName
    val username = tgUser?.username?.let { "@$it" } ?: "@" + displayName.lowercase().replace(" ", "_")

    Column(
        Modifier
            .fillMaxSize()
            .background(Letify.colors.container)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 20.dp),
    ) {
        // Шапка меню: аватар + имя/username слева, тема + (место под бургер) справа
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Круглый аватар
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Letify.colors.accent, LetifyColors.TilePink)),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = displayName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = Letify.typography.headlineMedium,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    displayName,
                    color = Letify.colors.text,
                    style = Letify.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    username,
                    color = Letify.colors.muted,
                    style = Letify.typography.bodySmall,
                )
            }
            // Анимированная иконка смены темы
            ThemeToggleButton()
        }

        Spacer(Modifier.height(10.dp))

        // Список пунктов меню — круглые контурные иконки
        Section.entries.forEach { s ->
            DrawerItem(
                section = s,
                active = s == active,
                onClick = { onSelect(s) },
            )
        }

        Spacer(Modifier.weight(1f))

        // Низ: подпись сервиса
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SolarIcon(name = "hand-shake-bold-duotone", tint = Letify.colors.muted, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                "Сервис поддержки · Анкетница",
                color = Letify.colors.muted,
                style = Letify.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DrawerItem(section: Section, active: Boolean, onClick: () -> Unit) {
    val bg = if (active) Letify.colors.accentSoft else Color.Transparent
    val iconTint = if (active) Letify.colors.accent else Letify.colors.text
    val textColor = if (active) Letify.colors.accent else Letify.colors.text
    // Hoisted OUT of drawBehind: Letify.colors is a @Composable-only accessor and
    // cannot be read inside the DrawScope lambda (that was the compile error).
    val strokeColor = if (active) Letify.colors.accent else Letify.colors.muted
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg, RoundedCornerShape(16.dp))
            .noFeedbackClick(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Круглая контурная иконка
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = strokeColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                SolarIcon(name = section.icon, tint = iconTint, size = 22.dp)
            }
            Spacer(Modifier.width(14.dp))
            Text(
                section.title,
                color = textColor,
                style = Letify.typography.titleMedium,
                fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Кнопка смены темы с АНИМИРОВАННОЙ иконкой: солнце ⇄ луна.
 * Плавно вращается и меняет масштаб/прозрачность двух глифов, драйвится одним
 * прогрессом (0 = светлая, 1 = тёмная).
 */
@Composable
private fun ThemeToggleButton() {
    val appState = LocalAppState.current
    val isDark = appState.themeMode == ThemeMode.Dark
    val progress = remember { Animatable(if (isDark) 1f else 0f) }
    val scope = rememberCoroutineScope()

    // Синхронизируем прогресс с внешними изменениями темы.
    androidx.compose.runtime.LaunchedEffect(isDark) {
        progress.animateTo(if (isDark) 1f else 0f, tween(420, easing = DrawerEasing))
    }

    NoFeedbackButton(
        onClick = {
            appState.themeMode = if (isDark) ThemeMode.Light else ThemeMode.Dark
        },
        modifier = Modifier.size(44.dp),
    ) {
        Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            val p = progress.value
            // Солнце: видно при p→0
            Box(
                Modifier.graphicsLayer {
                    alpha = 1f - p
                    val s = 1f - 0.4f * p
                    scaleX = s; scaleY = s
                    rotationZ = -90f * p
                },
            ) {
                SolarIcon(name = "sun-bold", tint = LetifyColors.TileLemon, size = 26.dp)
            }
            // Луна: видно при p→1
            Box(
                Modifier.graphicsLayer {
                    alpha = p
                    val s = 0.6f + 0.4f * p
                    scaleX = s; scaleY = s
                    rotationZ = 90f * (1f - p)
                },
            ) {
                SolarIcon(name = "moon-bold", tint = LetifyColors.TileViolet, size = 24.dp)
            }
        }
    }
}

/** Обёртка раздела с общей шапкой (бургер + заголовок) и переключением контента. */
@Composable
private fun SectionScaffold(section: Section, onOpenMenu: () -> Unit, data: AnketnicaData) {
    Box(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        when (section) {
            Section.Applications -> ApplicationsSection(onOpenMenu = onOpenMenu)
            Section.RolesQuestions -> RolesQuestionsSection(onOpenMenu = onOpenMenu)
            Section.Statistics -> StatisticsSection(onOpenMenu = onOpenMenu)
            Section.Settings -> SettingsSection(onOpenMenu = onOpenMenu)
        }
    }
}

/** Общая шапка раздела: слева бургер, по центру заголовок, справа опциональный слот. */
@Composable
fun SectionHeader(
    title: String,
    onOpenMenu: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoFeedbackButton(onClick = onOpenMenu, modifier = Modifier.size(44.dp)) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                SolarIcon(name = "menu-hamburger", tint = Letify.colors.text, size = 26.dp)
            }
        }
        Spacer(Modifier.width(4.dp))
        Text(
            title,
            color = Letify.colors.text,
            style = Letify.typography.displayMedium,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) trailing()
    }
}