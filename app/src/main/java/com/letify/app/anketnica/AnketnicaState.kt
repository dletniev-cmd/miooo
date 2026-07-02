package com.letify.app.anketnica

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import com.letify.app.ui.theme.LetifyColors

/**
 * Тип вопроса анкеты. Иконка берётся из набора Solar (assets/icons).
 * microphone-bold-duotone добавлен в проект отдельно (в Letify его не было).
 */
enum class QuestionType(val key: String, val title: String, val icon: String) {
    Text("text", "Текстовой", "notes-bold-duotone"),
    Voice("voice", "Голосовой", "microphone-bold-duotone"),
    Quiz("quiz", "Викторина", "checklist-minimalistic-bold-duotone");

    companion object {
        fun fromKey(key: String?): QuestionType =
            entries.firstOrNull { it.key == key } ?: Text
    }
}

data class Question(
    val id: Int,
    val text: String,
    val type: QuestionType,
    // Только для викторины:
    val options: List<String> = emptyList(),
    val correctOption: Int? = null,
)

data class Role(
    val id: Int,
    val name: String,
    val icon: String,
    val tile: Color,
)

enum class AppStatus(val key: String, val title: String) {
    Pending("pending", "Новая"),
    Accepted("accepted", "Принята"),
    Rejected("rejected", "Отклонена"),
}

/** Ответ кандидата на конкретный вопрос. */
data class Answer(
    val questionId: Int,
    val type: QuestionType,
    val text: String? = null,        // текстовой ответ / расшифровка
    val voiceSeconds: Int? = null,   // длина голосового
    val selectedOption: Int? = null, // выбранный вариант викторины
)

/** Заявка (анкетка) — данные подтягиваются из Telegram. */
data class Application(
    val id: Int,
    val name: String,
    val username: String,   // @username из Telegram
    val roleId: Int,
    val submittedAt: Long,   // epoch millis
    val status: AppStatus,
    val about: String,
    val answers: List<Answer>,
)

/**
 * Всё изменяемое состояние анкетницы: роли, вопросы и заявки. Тема/акцент/
 * настройки живут в общем [com.letify.app.ui.state.AppState] и переиспользуются
 * как есть — здесь только доменные данные сервиса.
 *
 * Пока это in-memory демо-данные (как и в Letify до подключения бэкенда):
 * реальные заявки будут приходить из Telegram-бота.
 */
class AnketnicaData {

    val roles: SnapshotStateList<Role> = mutableStateListOf(
        Role(1, "Админ", "shield-user-bold-duotone", LetifyColors.TileRed),
        Role(2, "Модератор", "user-id-bold-duotone", LetifyColors.TileBlue),
        Role(3, "Идейный вдохновитель", "star-shine-bold-duotone", LetifyColors.TileViolet),
    )

    val questions: SnapshotStateList<Question> = mutableStateListOf(
        Question(1, "Почему хочешь присоединиться к команде?", QuestionType.Text),
        Question(2, "Расскажи голосом о своём опыте", QuestionType.Voice),
        Question(
            3,
            "Сколько часов в неделю готов уделять?",
            QuestionType.Quiz,
            options = listOf("1–3 часа", "4–7 часов", "8+ часов"),
            correctOption = 2,
        ),
    )

    val applications: SnapshotStateList<Application> = mutableStateListOf()

    private var nextRoleId = 4
    private var nextQuestionId = 4
    private var nextAppId = 1

    init {
        val now = System.currentTimeMillis()
        val min = 60_000L
        val hour = 60 * min
        val day = 24 * hour
        applications.addAll(
            listOf(
                Application(
                    id = nextAppId++,
                    name = "Алина Ковалёва",
                    username = "@alina_k",
                    roleId = 2,
                    submittedAt = now - 8 * min,
                    status = AppStatus.Pending,
                    about = "Модерирую пару тематических чатов, люблю наводить порядок и помогать новичкам.",
                    answers = listOf(
                        Answer(1, QuestionType.Text, text = "Хочу приносить пользу сообществу и расти вместе с командой."),
                        Answer(2, QuestionType.Voice, voiceSeconds = 34),
                        Answer(3, QuestionType.Quiz, selectedOption = 1),
                    ),
                ),
                Application(
                    id = nextAppId++,
                    name = "Дмитрий Соколов",
                    username = "@dm_sokol",
                    roleId = 1,
                    submittedAt = now - 40 * min,
                    status = AppStatus.Pending,
                    about = "5 лет администрирую сервера и Discord-сообщества на 10к+ человек.",
                    answers = listOf(
                        Answer(1, QuestionType.Text, text = "Есть опыт и время помогать с инфраструктурой."),
                        Answer(2, QuestionType.Voice, voiceSeconds = 58),
                        Answer(3, QuestionType.Quiz, selectedOption = 2),
                    ),
                ),
                Application(
                    id = nextAppId++,
                    name = "Мария Лебедева",
                    username = "@mari_leb",
                    roleId = 3,
                    submittedAt = now - 5 * hour,
                    status = AppStatus.Accepted,
                    about = "Придумываю форматы активностей и контент, фонтанирую идеями.",
                    answers = listOf(
                        Answer(1, QuestionType.Text, text = "Обожаю генерить идеи и доводить их до запуска."),
                        Answer(2, QuestionType.Voice, voiceSeconds = 21),
                        Answer(3, QuestionType.Quiz, selectedOption = 2),
                    ),
                ),
                Application(
                    id = nextAppId++,
                    name = "Игорь Панов",
                    username = "@igorpanov",
                    roleId = 2,
                    submittedAt = now - 1 * day - 3 * hour,
                    status = AppStatus.Rejected,
                    about = "Хочу попробовать себя в модерации.",
                    answers = listOf(
                        Answer(1, QuestionType.Text, text = "Пока опыта немного, но готов учиться."),
                        Answer(2, QuestionType.Voice, voiceSeconds = 12),
                        Answer(3, QuestionType.Quiz, selectedOption = 0),
                    ),
                ),
                Application(
                    id = nextAppId++,
                    name = "Софья Титова",
                    username = "@sofia_t",
                    roleId = 1,
                    submittedAt = now - 2 * day,
                    status = AppStatus.Pending,
                    about = "Организатор комьюнити-ивентов, умею разруливать конфликты.",
                    answers = listOf(
                        Answer(1, QuestionType.Text, text = "Верю в проект и хочу усилить команду админов."),
                        Answer(2, QuestionType.Voice, voiceSeconds = 46),
                        Answer(3, QuestionType.Quiz, selectedOption = 2),
                    ),
                ),
            ),
        )
    }

    fun roleById(id: Int): Role? = roles.firstOrNull { it.id == id }

    fun questionById(id: Int): Question? = questions.firstOrNull { it.id == id }

    fun applicationById(id: Int): Application? = applications.firstOrNull { it.id == id }

    private fun setStatus(appId: Int, status: AppStatus) {
        val idx = applications.indexOfFirst { it.id == appId }
        if (idx >= 0) applications[idx] = applications[idx].copy(status = status)
    }

    fun accept(appId: Int) = setStatus(appId, AppStatus.Accepted)
    fun reject(appId: Int) = setStatus(appId, AppStatus.Rejected)
    fun resetStatus(appId: Int) = setStatus(appId, AppStatus.Pending)

    fun addRole(name: String, icon: String, tile: Color) {
        roles.add(Role(nextRoleId++, name.trim(), icon, tile))
    }

    fun deleteRole(id: Int) {
        roles.removeAll { it.id == id }
    }

    fun addQuestion(text: String, type: QuestionType, options: List<String>, correct: Int?) {
        questions.add(
            Question(
                id = nextQuestionId++,
                text = text.trim(),
                type = type,
                options = if (type == QuestionType.Quiz) options else emptyList(),
                correctOption = if (type == QuestionType.Quiz) correct else null,
            ),
        )
    }

    fun deleteQuestion(id: Int) {
        questions.removeAll { it.id == id }
    }

    // ── Счётчики для статистики ───────────────────────────────────────
    val total: Int get() = applications.size
    val pendingCount: Int get() = applications.count { it.status == AppStatus.Pending }
    val acceptedCount: Int get() = applications.count { it.status == AppStatus.Accepted }
    val rejectedCount: Int get() = applications.count { it.status == AppStatus.Rejected }

    /** Доля принятых среди рассмотренных (принятые + отклонённые). 0..1. */
    val acceptRate: Float
        get() {
            val decided = acceptedCount + rejectedCount
            return if (decided == 0) 0f else acceptedCount.toFloat() / decided
        }

    fun countForRole(roleId: Int): Int = applications.count { it.roleId == roleId }
}

val LocalAnketnica = compositionLocalOf<AnketnicaData> { error("AnketnicaData not provided") }

@Composable
fun rememberAnketnicaData(): AnketnicaData = remember { AnketnicaData() }