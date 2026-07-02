package com.letify.app.anketnica

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.OverlayHost
import com.letify.app.ui.components.RoundedSlideOverlay
import com.letify.app.ui.components.SettingsCard
import com.letify.app.ui.components.SettingsRow
import com.letify.app.ui.components.rememberParallaxProgress
import com.letify.app.ui.components.screenHPad
import com.letify.app.ui.screens.AppearanceScreen
import com.letify.app.ui.screens.NotificationsScreen
import com.letify.app.ui.state.LocalAppState
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors

private enum class SettingsRoute { Root, Notifications, Appearance }

/**
 * Настройки — зеркалят экран профиля Letify: карточка профиля сверху, затем
 * Уведомления и Оформление. Уведомления и Оформление переиспользуют РОДНЫЕ
 * экраны Letify (NotificationsScreen / AppearanceScreen) без изменений — так
 * раздел «Оформление» переносится 1-в-1 со всеми элементами (тема, кружочки
 * акцента, анимация, иконка).
 */
@Composable
fun SettingsSection(onOpenMenu: () -> Unit) {
    var route by remember { mutableStateOf(SettingsRoute.Root) }
    val parallax = rememberParallaxProgress()

    Box(Modifier.fillMaxSize()) {
        OverlayHost(parallaxProgress = parallax) {
            SettingsRoot(
                onOpenMenu = onOpenMenu,
                onNotifications = { route = SettingsRoute.Notifications },
                onAppearance = { route = SettingsRoute.Appearance },
            )
        }
        if (route != SettingsRoute.Root) {
            androidx.compose.runtime.key(route) {
                RoundedSlideOverlay(
                    parallaxProgress = parallax,
                    onDismissed = { route = SettingsRoute.Root },
                ) { animatedBack ->
                    when (route) {
                        SettingsRoute.Notifications -> NotificationsScreen(onBack = animatedBack)
                        SettingsRoute.Appearance -> AppearanceScreen(onBack = animatedBack)
                        SettingsRoute.Root -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRoot(onOpenMenu: () -> Unit, onNotifications: () -> Unit, onAppearance: () -> Unit) {
    val appState = LocalAppState.current
    val scroll = rememberScrollState()
    val displayName = appState.telegramUser?.displayName?.takeIf { it.isNotBlank() } ?: appState.userName
    val username = appState.telegramUser?.username?.let { "@$it" }
        ?: ("@" + displayName.lowercase().replace(" ", "_"))

    Column(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        SectionHeader(title = "Настройки", onOpenMenu = onOpenMenu)

        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(bottom = 40.dp)) {
            // Карточка профиля
            Column(
                Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarCircle(name = displayName, size = 96.dp)
                Spacer(Modifier.height(12.dp))
                Text(displayName, color = Letify.colors.text, style = Letify.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(username, color = Letify.colors.muted, style = Letify.typography.bodyMedium)
            }

            Spacer(Modifier.height(10.dp))

            // Пункты настроек — Telegram-стиль плитки (как в профиле Letify)
            SettingsCard(
                modifier = Modifier.screenHPad(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                SettingsRow(
                    icon = "bell-bold",
                    iconTile = LetifyColors.TileRed,
                    title = "Уведомления",
                    onClick = onNotifications,
                )
                SettingsRow(
                    icon = "moon-stars-bold",
                    iconTile = LetifyColors.TileViolet,
                    title = "Оформление",
                    onClick = onAppearance,
                )
            }
        }
    }
}