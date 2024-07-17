package ru.rusviper.logic

import ru.rusviper.data.WorkLogRow
import ru.rusviper.data.WorkLogRowType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

/**
 * Парсит строку лога в виде:
 * "1.7 - 11616: обсуждение по РК, план по передаче кода"
 * и преобразует в модель записи в лог
 */
class WorkLogTextParser(val projectPrefix: String = "IA-") {

    // строки могут быть следующих типов:
    // 1) дата (день) + комментарий к дню
    // 2) запись о работе
    // 3) другое (всё остальное - на ретро, итоги ===, комментарии и пр.)

    /**  Формат строки записи в лог **/
    private val matcherRowFormat = "(\\d\\.?\\d?)\\s+-\\s+(\\d+)\\s*: (.+)".toRegex()

    /**  Формат строки записи даты **/
    private val matcherDateFormat = "^(\\d{1,2}\\.\\d{1,2})([.[^:-]]+)\$".toRegex()

    /**  Выражение получения часов **/
    private val matcherHours = "(^\\d\\.?\\d?)".toRegex()

    /**  Выражение получения даты **/
    private val matcherDate = "(^\\d{1,2}\\.\\d{1,2})".toRegex()

    /**  Выражение получения задачи **/
    private val matcherIssue = "(?<=-\\s+)\\d+(?!\\d)".toRegex()

    /**  Выражение получения комментария **/
    private val matcherComment = "(?<=: ).+$".toRegex()



    /**
     * Считывает последовательность строк записей с учётом строк с датой
     */
    fun parseDayWorkLogs(textRow: String): List<WorkLogRow> {
        var actualRowDate = LocalDateTime.now()
        val rows = ArrayList<WorkLogRow>()
        for (line in textRow.lines()) {
            val rowType = getRowType(line)
            when (rowType) {
                WorkLogRowType.DATE -> actualRowDate = parseDateString(line)
                WorkLogRowType.WORK_NOTE -> rows.add(parseWorkLogString(line, actualRowDate))
                WorkLogRowType.OTHER -> continue
            }
        }
        return rows
    }

    /**
     * Считывает значение записи в лог
     */
    fun parseDateString(textRow: String): LocalDateTime {
        val stringValue = matcherDate.findAll(textRow).first().value
        val formatter = DateTimeFormatterBuilder()
            .appendPattern("d.MM")
            .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
            .toFormatter()

        val date = LocalDate.parse(stringValue, formatter)
        return LocalDateTime.of(date, LocalTime.now())
    }

    /**
     * "1.7 - 11616: обсуждение по РК, план по передаче кода"
     */
    fun parseWorkLogString(textRow: String, date: LocalDateTime): WorkLogRow {
        return WorkLogRow(
            projectPrefix + getWorkLogIssue(textRow),
            getWorkLogHours(textRow),
            getWorkLogComment(textRow),
            date)
    }

    fun getRowType(textRow: String): WorkLogRowType {
        return if (checkIsWorkLogRow(textRow))
            WorkLogRowType.WORK_NOTE
        else if (checkIsDateRow(textRow))
            WorkLogRowType.DATE
        else
            WorkLogRowType.OTHER
    }

    // region tools

    private fun checkIsWorkLogRow(textRow: String): Boolean {
        return matcherRowFormat.findAll(textRow).count() == 1
    }

    private fun checkIsDateRow(textRow: String): Boolean {
        return matcherDateFormat.findAll(textRow).count() == 1
    }

    private fun getWorkLogHours(textRow: String): Double {
        val strValue = matcherHours.findAll(textRow).first().value
        return strValue.toDouble()
    }

    private fun getWorkLogIssue(textRow: String): String {
        return matcherIssue.findAll(textRow).first().value
    }

    private fun getWorkLogComment(textRow: String): String {
        return matcherComment.findAll(textRow).first().value
    }


    // endregion tools

}