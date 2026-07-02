package com.letify.app.ui.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.letify.app.ui.AppIconVariant
import com.letify.app.ui.applyAppIcon
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.AccentSwitch
import com.letify.app.ui.components.ColorPickerGrid
import com.letify.app.ui.components.NoFeedbackButton
import com.letify.app.ui.components.OverlayHost
import com.letify.app.ui.components.RoundedSlideOverlay
import com.letify.app.ui.components.SettingsCard
import com.letify.app.ui.components.SettingsHeader
import com.letify.app.ui.components.SettingsRow
import com.letify.app.ui.components.overlayHostShiftFraction
import com.letify.app.ui.components.rememberParallaxProgress
import com.letify.app.ui.components.noFeedbackClick
import com.letify.app.ui.components.screenHPad
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.state.LocalAppState
import com.letify.app.ui.state.TransitionStyle
import com.letify.app.ui.theme.ThemeMode
import com.letify.app.ui.theme.ThemePalette
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors

private enum class AppearanceRoute { Root, Animation }

@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    var route by remember { mutableStateOf(AppearanceRoute.Root) }
    // Same transition mechanic as the OUTER settings overlays —
    // RoundedSlideOverlay + OverlayHost give us the iOS-style stack push.
    val parallax = rememberParallaxProgress()
    Box(Modifier.fillMaxSize()) {
        OverlayHost(parallaxProgress = parallax) {
            AppearanceRoot(
                onBack = onBack,
                onAnimation = { route = AppearanceRoute.Animation },
            )
        }
        if (route != AppearanceRoute.Root) {
            key(route) {
                RoundedSlideOverlay(
                    parallaxProgress = parallax,
                    onDismissed = { route = AppearanceRoute.Root },
                ) { animatedBack ->
                    when (route) {
                        AppearanceRoute.Animation -> AnimationSettingsScreen(onBack = animatedBack)
                        AppearanceRoute.Root -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceRoot(onBack: () -> Unit, onAnimation: () -> Unit) {
    val state = LocalAppState.current
    val scroll = rememberScrollState()
    Box(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 6.dp, bottom = 60.dp),
        ) {
            SettingsHeader(title = "Оформление", onBack = onBack)

            // Theme toggle
            Text(
                "ТЕМА",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 28.dp, top = 14.dp, bottom = 8.dp),
            )
            SettingsCard(
                modifier = Modifier.screenHPad(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = "moon-bold",
                    iconTile = LetifyColors.TileViolet,
                    title = "Тёмная тема",
                    value = if (state.themeMode == ThemeMode.Dark) "Включена" else "Выключена",
                    showChevron = false,
                    trailing = {
                        AccentSwitch(
                            checked = state.themeMode == ThemeMode.Dark,
                            onCheckedChange = { dark ->
                                state.themeMode = if (dark) ThemeMode.Dark else ThemeMode.Light
                            },
                        )
                    },
                )
            }

            // Accent color picker
            Text(
                "АКЦЕНТНЫЙ ЦВЕТ",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 28.dp, top = 22.dp, bottom = 8.dp),
            )
            SettingsCard(
                modifier = Modifier.screenHPad(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            ) {
                ColorPickerGrid(
                    colors = ThemePalette,
                    selected = state.accent,
                    onSelect = { state.accent = it },
                )
            }

            // Animation / transition settings entry
            Text(
                "АНИМАЦИЯ",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 28.dp, top = 22.dp, bottom = 8.dp),
            )
            SettingsCard(
                modifier = Modifier.screenHPad(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = "magic-stick-3-bold-duotone",
                    iconTile = LetifyColors.TilePink,
                    title = "Переходы",
                    value = transitionStyleTitle(state.transitionStyle),
                    onClick = onAnimation,
                )
            }

            // ---- App icon picker --------------------------------------------
            // One row of rounded launcher-icon thumbnails. Tapping picks one;
            // the chosen variant's manifest alias is enabled and the home-screen
            // icon swaps to it.
            Text(
                "ИКОНКА",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 28.dp, top = 22.dp, bottom = 8.dp),
            )
            Box(modifier = Modifier.screenHPad().padding(top = 2.dp, bottom = 4.dp)) {
                AppIconPicker(
                    selectedKey = state.appIcon,
                    onSelect = { state.appIcon = it.key },
                )
            }
        }
    }
}

/**
 * Horizontal row of rounded launcher-icon thumbnails. Supports tap-to-pick.
 * The selected thumbnail is at full strength, the others are dimmed.
 * Selecting also swaps the real launcher icon.
 */
@Composable
private fun AppIconPicker(selectedKey: String, onSelect: (AppIconVariant) -> Unit) {
    val context = LocalContext.current
    val variants = AppIconVariant.entries

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        variants.forEach { v ->
            AppIconThumb(
                variant = v,
                selected = v.key == selectedKey,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (v.key != selectedKey) {
                        onSelect(v)
                        applyAppIcon(context, v)
                    }
                },
            )
        }
    }
}

@Composable
private fun AppIconThumb(
    variant: AppIconVariant,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val dim by animateFloatAsState(
        targetValue = if (selected) 1f else 0.4f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconDim",
    )
    val corner = 16.dp
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(corner))
            .noFeedbackClick(onClick = onClick),
    ) {
        Image(
            painter = painterResource(id = variant.thumbRes),
            contentDescription = variant.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = dim },
        )
    }
}

private fun transitionStyleTitle(style: TransitionStyle): String = when (style) {
    TransitionStyle.Push -> "Сдвиг"
    TransitionStyle.Cover -> "Наплыв"
}

// ── Анимация ────────────────────────────────────────────────────────────────

// Same gentle ease-out the real transitions use, so the looping previews move
// with exactly the feel the user gets in the app.
private val PreviewEasing = CubicBezierEasing(0.32f, 0.72f, 0.0f, 1.0f)

@Composable
private fun AnimationSettingsScreen(onBack: () -> Unit) {
    val state = LocalAppState.current
    val scroll = rememberScrollState()
    Box(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 6.dp, bottom = 60.dp),
        ) {
            SettingsHeader(title = "Анимация", onBack = onBack)

            Text(
                "ПЕРЕХОД МЕЖДУ ЭКРАНАМИ",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 12.dp),
            )

            Row(
                modifier = Modifier.screenHPad().fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TransitionOptionCard(
                    style = TransitionStyle.Push,
                    title = "Сдвиг",
                    subtitle = "Единое полотно",
                    selected = state.transitionStyle == TransitionStyle.Push,
                    onSelect = { state.transitionStyle = TransitionStyle.Push },
                    modifier = Modifier.weight(1f),
                )
                TransitionOptionCard(
                    style = TransitionStyle.Cover,
                    title = "Наплыв",
                    subtitle = "Наезжает поверх",
                    selected = state.transitionStyle == TransitionStyle.Cover,
                    onSelect = { state.transitionStyle = TransitionStyle.Cover },
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                "ЖЕСТЫ",
                color = Letify.colors.muted,
                style = Letify.typography.labelSmall,
                modifier = Modifier.padding(start = 28.dp, top = 24.dp, bottom = 8.dp),
            )
            SettingsCard(
                modifier = Modifier.screenHPad(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = "arrow-left-bold",
                    iconTile = LetifyColors.TileViolet,
                    title = "Свайп назад",
                    value = if (state.swipeBackEnabled) "Включён" else "Выключен",
                    showChevron = false,
                    trailing = {
                        AccentSwitch(
                            checked = state.swipeBackEnabled,
                            onCheckedChange = { state.swipeBackEnabled = it },
                        )
                    },
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 8.dp),
            ) {
                Text(
                    "Когда выключено, экраны нельзя закрывать свайпом от левого края — только кнопкой «Назад».",
                    color = Letify.colors.muted,
                    style = Letify.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun TransitionOptionCard(
    style: TransitionStyle,
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val borderColor =
        if (selected) Letify.colors.accent
        else Letify.colors.text.copy(alpha = 0.07f)
    Column(
        modifier
            .clip(shape)
            .background(Letify.colors.container)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = borderColor,
                shape = shape,
            )
            .noFeedbackClick(onClick = onSelect),
    ) {
        // Full-bleed preview, flush to the card top/sides (no inner framing gap).
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.92f)
                .clip(RoundedCornerShape(topStart = 19.dp, topEnd = 19.dp)),
        ) {
            TransitionPreview(style = style, modifier = Modifier.fillMaxSize())
            // Selection badge in the corner.
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) Letify.colors.accent
                        else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.35f),
                    )
                    .let {
                        if (selected) it
                        else it.border(
                            1.5.dp,
                            androidx.compose.ui.graphics.Color.White.copy(alpha = 0.55f),
                            RoundedCornerShape(999.dp),
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    SolarIcon(
                        name = "check-bold",
                        tint = androidx.compose.ui.graphics.Color.White,
                        size = 13.dp,
                    )
                }
            }
        }
        // Fixed single-line subtitle keeps BOTH cards exactly the same height.
        Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)) {
            Text(title, color = Letify.colors.text, style = Letify.typography.titleSmall)
            Box(Modifier.height(2.dp))
            Text(
                subtitle,
                color = Letify.colors.muted,
                style = Letify.typography.bodySmall,
                maxLines = 1,
            )
        }
    }
}

/**
 * A small looping schematic of a transition. Two fake "screens" are driven by
 * the SAME 0..1 progress formula the real overlay uses.
 */
@Composable
private fun TransitionPreview(style: TransitionStyle, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "transitionPreview")
    val restP = if (style == TransitionStyle.Cover) 0.16f else 0f
    val p by transition.animateFloat(
        initialValue = 1f,
        targetValue = restP,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1700
                1f at 0
                1f at 350 using PreviewEasing
                restP at 1200
                restP at 1700
            },
            repeatMode = RepeatMode.Reverse,
        ),
        label = "p",
    )
    val shift = overlayHostShiftFraction(style)
    val radiusPx = with(LocalDensity.current) {
        (if (style == TransitionStyle.Cover) 13.dp else 0.dp).toPx()
    }
    Box(modifier.background(Letify.colors.bg)) {
        MiniScreen(
            accent = false,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = -(1f - p) * size.width * shift },
        )
        if (style == TransitionStyle.Cover) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = -(1f - p) * size.width * shift
                        alpha = (1f - p) * 0.16f
                    }
                    .background(Color.Black),
            )
        }
        MiniScreen(
            accent = true,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = p * size.width
                    shape = RoundedCornerShape(radiusPx)
                    clip = true
                    shadowElevation = 10f
                },
        )
    }
}

/** A schematic fake app screen used inside [TransitionPreview]. */
@Composable
private fun MiniScreen(accent: Boolean, modifier: Modifier) {
    val screenBg = if (accent) Letify.colors.container else Letify.colors.bg
    val headerColor = if (accent) Letify.colors.accent else Letify.colors.muted
    val avatarColor =
        if (accent) Letify.colors.accent.copy(alpha = 0.55f)
        else Letify.colors.text.copy(alpha = 0.14f)
    val cardColor =
        if (accent) Letify.colors.accent.copy(alpha = 0.16f)
        else Letify.colors.text.copy(alpha = 0.06f)
    val lineColor = Letify.colors.text.copy(alpha = 0.08f)
    Column(
        modifier
            .background(screenBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.size(14.dp).clip(RoundedCornerShape(999.dp)).background(avatarColor))
            Box(
                Modifier
                    .width(48.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(headerColor),
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(cardColor),
        )
        repeat(3) {
            Box(
                Modifier
                    .fillMaxWidth(if (it == 2) 0.55f else 1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(lineColor),
            )
        }
    }
}