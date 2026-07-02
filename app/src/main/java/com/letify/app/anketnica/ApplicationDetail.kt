package com.letify.app.anketnica

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.PrimaryActionButton
import com.letify.app.ui.components.SettingsHeader
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors

@Composable
fun ApplicationDetail(appId: Int, onBack: () -> Unit) {
    val data = LocalAnketnica.current
    val app = data.applicationById(appId)
    val scroll = rememberScrollState()

    Box(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        if (app == null) {
            Column(Modifier.fillMaxSize()) {
                SettingsHeader(title = "Анкета", onBack = onBack)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Анкета не найдена", color = Letify.colors.muted)
                }
            }
            return
        }
        val role = data.roleById(app.roleId)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 120.dp),
        ) {
            SettingsHeader(title = "Анкета", onBack = onBack)

            // Шапка профиля кандидата: аватар + имя + @username из Telegram
            Column(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarCircle(name = app.name, size = 96.dp)
                Spacer(Modifier.height(12.dp))
                Text(
                    app.name,
                    color = Letify.colors.text,
                    style = Letify.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(app.username, color = Letify.colors.muted, style = Letify.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (role != null) RolePill(role = role)
                    Spacer(Modifier.width(8.dp))
                    StatusChip(status = app.status)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Информация
            SectionLabel("ИНФОРМАЦИЯ")
            InfoCard {
                InfoRow(icon = "user-circle-bold-duotone", tile = LetifyColors.TileBlue, label = "Имя", value = app.name)
                InfoRow(icon = "telegram-plane", tile = LetifyColors.TileSky, label = "Telegram", value = app.username)
                if (role != null) {
                    InfoRow(icon = role.icon, tile = role.tile, label = "Роль", value = role.name)
                }
                InfoRow(
                    icon = "clock-circle-bold-duotone",
                    tile = LetifyColors.TileOrange,
                    label = "Подано",
                    value = AnketnicaFormat.relativeTime(app.submittedAt) + " назад",
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("О СЕБЕ")
            InfoCard {
                Text(
                    app.about,
                    color = Letify.colors.text,
                    style = Letify.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionLabel("ОТВЕТЫ")
            app.answers.forEach { answer ->
                val q = data.questionById(answer.questionId)
                AnswerCard(question = q, answer = answer)
            }
        }

        // Нижние кнопки Принять / Отклонить (или сброс, если уже рассмотрено)
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Letify.colors.bg)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            if (app.status == AppStatus.Pending) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RejectButton(modifier = Modifier.weight(1f)) { data.reject(app.id); onBack() }
                    Box(Modifier.weight(1f)) {
                        PrimaryActionButton(label = "Принять", onClick = { data.accept(app.id); onBack() })
                    }
                }
            } else {
                RejectButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = "Вернуть в новые",
                    icon = "restart-bold",
                ) { data.resetStatus(app.id); onBack() }
            }
        }
    }
}

@Composable
private fun StatusChip(status: AppStatus) {
    val (color, text) = when (status) {
        AppStatus.Pending -> LetifyColors.TileOrange to "Новая"
        AppStatus.Accepted -> LetifyColors.TileGreen to "Принята"
        AppStatus.Rejected -> LetifyColors.TileRed to "Отклонена"
    }
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = color, style = Letify.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = Letify.colors.muted,
        style = Letify.typography.labelSmall,
        modifier = Modifier.padding(start = 28.dp, top = 10.dp, bottom = 8.dp),
    )
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .background(Letify.colors.container, RoundedCornerShape(22.dp))
            .padding(vertical = 4.dp),
    ) {
        Column { content() }
    }
}

@Composable
private fun InfoRow(icon: String, tile: Color, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(11.dp)).background(tile, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SolarIcon(name = icon, tint = Color.White, size = 18.dp)
        }
        Spacer(Modifier.width(14.dp))
        Text(label, color = Letify.colors.muted, style = Letify.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = Letify.colors.text,
            style = Letify.typography.titleSmall,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun AnswerCard(question: Question?, answer: Answer) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .background(Letify.colors.container, RoundedCornerShape(20.dp))
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SolarIcon(name = answer.type.icon, tint = Letify.colors.accent, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    question?.text ?: "Вопрос",
                    color = Letify.colors.text,
                    style = Letify.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            when (answer.type) {
                QuestionType.Text -> Text(
                    answer.text ?: "—",
                    color = Letify.colors.muted,
                    style = Letify.typography.bodyLarge,
                )
                QuestionType.Voice -> VoiceBubble(seconds = answer.voiceSeconds ?: 0)
                QuestionType.Quiz -> QuizAnswer(question = question, selected = answer.selectedOption)
            }
        }
    }
}

@Composable
private fun VoiceBubble(seconds: Int) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Letify.colors.accentSoft, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(Letify.colors.accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            SolarIcon(name = "play-bold", tint = Color.White, size = 15.dp)
        }
        Spacer(Modifier.width(10.dp))
        // Псевдо-волна
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val heights = listOf(8, 14, 20, 12, 18, 10, 16, 22, 12, 8, 14, 18, 10, 16, 12)
            heights.forEach { h ->
                Box(
                    Modifier
                        .width(2.dp)
                        .height(h.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Letify.colors.accent.copy(alpha = 0.7f)),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            AnketnicaFormat.voiceLength(seconds),
            color = Letify.colors.accent,
            style = Letify.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuizAnswer(question: Question?, selected: Int?) {
    val options = question?.options ?: emptyList()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, opt ->
            val isSelected = i == selected
            val isCorrect = question?.correctOption == i
            val tint = when {
                isSelected && isCorrect -> LetifyColors.TileGreen
                isSelected && !isCorrect -> LetifyColors.TileRed
                isCorrect -> LetifyColors.TileGreen
                else -> Letify.colors.muted
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) tint.copy(alpha = 0.14f) else Letify.colors.track,
                        RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SolarIcon(
                    name = when {
                        isSelected && isCorrect -> "check-circle-bold"
                        isSelected && !isCorrect -> "close-circle-bold-duotone"
                        isCorrect -> "check-circle-outline"
                        else -> "minus-circle-outline"
                    },
                    tint = tint,
                    size = 18.dp,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    opt,
                    color = if (isSelected) Letify.colors.text else Letify.colors.muted,
                    style = Letify.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun RejectButton(
    modifier: Modifier = Modifier,
    label: String = "Отклонить",
    icon: String = "close-circle-bold-duotone",
    onClick: () -> Unit,
) {
    com.letify.app.ui.components.NoFeedbackButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(LetifyColors.TileRed.copy(alpha = 0.14f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SolarIcon(name = icon, tint = LetifyColors.TileRed, size = 20.dp)
                Spacer(Modifier.width(8.dp))
                Text(label, color = LetifyColors.TileRed, style = Letify.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}