package com.letify.app.anketnica

/** Небольшие форматтеры времени/длительности для списков и деталей. */
object AnketnicaFormat {

    /** Относительное время подачи заявки: «сейчас», «5 мин», «2 ч», «вчера», «3 дн». */
    fun relativeTime(submittedAt: Long, now: Long = System.currentTimeMillis()): String {
        val diff = (now - submittedAt).coerceAtLeast(0L)
        val min = diff / 60_000L
        val hours = diff / 3_600_000L
        val days = diff / 86_400_000L
        return when {
            min < 1 -> "сейчас"
            min < 60 -> "$min мин"
            hours < 24 -> "$hours ч"
            days.toInt() == 1 -> "вчера"
            days < 7 -> "$days дн"
            else -> "${days / 7} нед"
        }
    }

    /** Длина голосового ответа mm:ss. */
    fun voiceLength(seconds: Int): String =
        "%d:%02d".format(seconds / 60, seconds % 60)
}