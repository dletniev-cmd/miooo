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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.letify.app.ui.components.NoFeedbackButton
import com.letify.app.ui.components.noFeedbackClick
import com.letify.app.ui.components.LetifyBottomSheet
import com.letify.app.ui.icons.SolarIcon
import com.letify.app.ui.theme.Letify
import com.letify.app.ui.theme.LetifyColors

@Composable
fun RolesQuestionsSection(onOpenMenu: () -> Unit) {
    val data = LocalAnketnica.current
    val scroll = rememberScrollState()
    var showAddRole by remember { mutableStateOf(false) }
    var showAddQuestion by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Letify.colors.bg)) {
        SectionHeader(title = "Роли и вопросы", onOpenMenu = onOpenMenu)

        Column(
            Modifier.fillMaxSize().verticalScroll(scroll).padding(bottom = 40.dp),
        ) {
            // ── Роли ──
            SectionLabelRow(
                title = "РОЛИ",
                onAdd = { showAddRole = true },
            )
            data.roles.forEach { role ->
                RoleCard(role = role, count = data.countForRole(role.id), onDelete = { data.deleteRole(role.id) })
            }

            Spacer(Modifier.height(14.dp))

            // ── Вопросы ──
            SectionLabelRow(
                title = "ВОПРОСЫ",
                onAdd = { showAddQuestion = true },
            )
            data.questions.forEach { q ->
                QuestionCard(question = q, onDelete = { data.deleteQuestion(q.id) })
            }
        }
    }

    if (showAddRole) {
        AddRoleSheet(onDismiss = { showAddRole = false })
    }
    if (showAddQuestion) {
        AddQuestionSheet(onDismiss = { showAddQuestion = false })
    }
}

@Composable
private fun SectionLabelRow(title: String, onAdd: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 28.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = Letify.colors.muted, style = Letify.typography.labelSmall, modifier = Modifier.weight(1f))
        NoFeedbackButton(onClick = onAdd, modifier = Modifier.size(30.dp)) {
            Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                SolarIcon(name = "add-circle-bold-duotone", tint = Letify.colors.accent, size = 26.dp)
            }
        }
    }
}

@Composable
private fun RoleCard(role: Role, count: Int, onDelete: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .background(Letify.colors.container, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(role.tile, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                SolarIcon(name = role.icon, tint = Color.White, size = 22.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(role.name, color = Letify.colors.text, style = Letify.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("$count анкет", color = Letify.colors.muted, style = Letify.typography.bodySmall)
            }
            NoFeedbackButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    SolarIcon(name = "trash-bin-trash-bold", tint = Letify.colors.muted, size = 20.dp)
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(question: Question, onDelete: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .background(Letify.colors.container, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Letify.colors.accentSoft, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                SolarIcon(name = question.type.icon, tint = Letify.colors.accent, size = 22.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(question.text, color = Letify.colors.text, style = Letify.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(question.type.title, color = Letify.colors.muted, style = Letify.typography.bodySmall)
            }
            NoFeedbackButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    SolarIcon(name = "trash-bin-trash-bold", tint = Letify.colors.muted, size = 20.dp)
                }
            }
        }
    }
}

// ── Bottom-sheet добавления роли ──
@Composable
private fun AddRoleSheet(onDismiss: () -> Unit) {
    val data = LocalAnketnica.current
    var name by remember { mutableStateOf("") }
    val tiles = listOf(
        LetifyColors.TileRed, LetifyColors.TileBlue, LetifyColors.TileViolet,
        LetifyColors.TileGreen, LetifyColors.TileOrange, LetifyColors.TileTeal,
        LetifyColors.TilePink, LetifyColors.TileSky,
    )
    var tileIdx by remember { mutableIntStateOf(0) }
    val icons = listOf(
        "shield-user-bold-duotone", "user-id-bold-duotone", "star-shine-bold-duotone",
        "medal-star-bold-duotone", "star-bold-duotone", "case-bold-duotone",
    )
    var iconIdx by remember { mutableIntStateOf(0) }

    LetifyBottomSheet(
        title = "Новая роль",
        onDismiss = onDismiss,
        primaryLabel = "Добавить",
        primaryEnabled = name.isNotBlank(),
        onPrimary = { data.addRole(name, icons[iconIdx], tiles[tileIdx]) },
    ) {
        SheetLabel("НАЗВАНИЕ")
        SheetTextField(value = name, onValueChange = { name = it }, placeholder = "Например: Куратор")
        Spacer(Modifier.height(14.dp))
        SheetLabel("ЦВЕТ")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.forEachIndexed { i, c ->
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(c, CircleShape)
                        .noFeedbackClick { tileIdx = i },
                    contentAlignment = Alignment.Center,
                ) {
                    if (i == tileIdx) SolarIcon(name = "check-bold", tint = Color.White, size = 18.dp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        SheetLabel("ИКОНКА")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            icons.forEachIndexed { i, ic ->
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (i == iconIdx) Letify.colors.accentSoft else Letify.colors.track,
                            RoundedCornerShape(12.dp),
                        )
                        .noFeedbackClick { iconIdx = i },
                    contentAlignment = Alignment.Center,
                ) {
                    SolarIcon(name = ic, tint = if (i == iconIdx) Letify.colors.accent else Letify.colors.muted, size = 22.dp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

// ── Bottom-sheet добавления вопроса ──
@Composable
private fun AddQuestionSheet(onDismiss: () -> Unit) {
    val data = LocalAnketnica.current
    var text by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(QuestionType.Text) }
    // Варианты для викторины
    val options = remember { androidx.compose.runtime.mutableStateListOf("", "") }
    var correct by remember { mutableIntStateOf(0) }

    val canAdd = text.isNotBlank() && (type != QuestionType.Quiz || options.count { it.isNotBlank() } >= 2)

    LetifyBottomSheet(
        title = "Новый вопрос",
        onDismiss = onDismiss,
        primaryLabel = "Добавить",
        primaryEnabled = canAdd,
        onPrimary = {
            data.addQuestion(
                text = text,
                type = type,
                options = options.filter { it.isNotBlank() },
                correct = correct.coerceIn(0, (options.count { it.isNotBlank() } - 1).coerceAtLeast(0)),
            )
        },
    ) {
        SheetLabel("ВОПРОС")
        SheetTextField(value = text, onValueChange = { text = it }, placeholder = "Текст вопроса")
        Spacer(Modifier.height(14.dp))
        SheetLabel("ТИП")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestionType.entries.forEach { t ->
                val active = t == type
                Row(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) Letify.colors.accentSoft else Letify.colors.track, RoundedCornerShape(14.dp))
                        .noFeedbackClick { type = t }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SolarIcon(name = t.icon, tint = if (active) Letify.colors.accent else Letify.colors.muted, size = 18.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        t.title,
                        color = if (active) Letify.colors.accent else Letify.colors.muted,
                        style = Letify.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }

        if (type == QuestionType.Quiz) {
            Spacer(Modifier.height(14.dp))
            SheetLabel("ВАРИАНТЫ (отметьте правильный)")
            options.forEachIndexed { i, opt ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == correct) Letify.colors.accent else Letify.colors.track,
                                CircleShape,
                            )
                            .noFeedbackClick { correct = i },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (i == correct) SolarIcon(name = "check-bold", tint = Color.White, size = 15.dp)
                    }
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.weight(1f)) {
                        SheetTextField(
                            value = opt,
                            onValueChange = { options[i] = it },
                            placeholder = "Вариант ${i + 1}",
                        )
                    }
                }
            }
            NoFeedbackButton(onClick = { options.add("") }, modifier = Modifier.padding(top = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SolarIcon(name = "add-circle-outline", tint = Letify.colors.accent, size = 20.dp)
                    Spacer(Modifier.width(6.dp))
                    Text("Добавить вариант", color = Letify.colors.accent, style = Letify.typography.labelMedium)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text,
        color = Letify.colors.muted,
        style = Letify.typography.labelSmall,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
private fun SheetTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Letify.colors.track, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = Letify.colors.muted, style = Letify.typography.bodyLarge)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = Letify.typography.bodyLarge.copy(color = Letify.colors.text),
            cursorBrush = SolidColor(Letify.colors.accent),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}