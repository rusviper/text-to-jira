package ru.rusviper.logic

import ru.rusviper.data.WorkLogRow
import ru.rusviper.data.WorkLogRowType
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeParseException
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkLogTextParserTest {

    private val exampleString1 = "1.3 - 11615: ответы на комментарии в проектировании"
    private val exampleString2 = "2 - 11615: ответы на комментарии в проектировании IA-12345"
    private val exampleString3 = "0.5 - 11615 : ответы на комментарии в : проектировании"
    private val exampleString4 = "22.05 (планирование)"
    private val exampleString5 = "2.05"
    private val exampleString6 = "на ретро: ура, всё хорошо"
    private val exampleString7 = "1.5 - мм+м"
    private val exampleString8 = "1.5 -  11615: ответы"
    private val exampleString9 = "1.5  -  11615: ответы"
    private val exampleString10 = "1.5   -   11615: ответы"

    @Test
    fun parseDayWorkLogs() {
        val parser = WorkLogTextParser()
        val testString = """
            21.05
            $exampleString1
            $exampleString2
            $exampleString4
            $exampleString3
        """.trimIndent()

        val rows = parser.parseDayWorkLogs(testString)
        // проверим, что считалось три строки записей
        assertEquals(3, rows.size)
        // проверим, что у первых двух строк проставлено 21 число
        assertEquals(21, rows[0].date.dayOfMonth)
        assertEquals(21, rows[1].date.dayOfMonth)
        // проверим, что у третьей строки проставлено 22 число
        assertEquals(22, rows[2].date.dayOfMonth)
    }

    @Test
    fun parseDateString() {
        val parser = WorkLogTextParser()

        try {
            val date1 = parser.parseDateString(exampleString1)
        } catch (e: Throwable) {
            assert(e is DateTimeParseException)
        }

        val date4 = parser.parseDateString(exampleString4)
        assertEquals(22, date4.dayOfMonth)
        assertEquals(Month.MAY, date4.month)

        val date5 = parser.parseDateString(exampleString5)
        assertEquals(2, date5.dayOfMonth)
        assertEquals(Month.MAY, date5.month)


        try {
            val date6 = parser.parseDateString(exampleString6)
        } catch (e: Throwable) {
            assert(e is DateTimeParseException)
        }
    }

    @Test
    fun parseWorkLogString() {
        val parser = WorkLogTextParser()
        val date = LocalDateTime.now()

        val row1 = parser.parseWorkLogString(exampleString1, date)
        checkRow(row1, "IA-11615", "ответы на комментарии в проектировании", 1.3)

        val row2 = parser.parseWorkLogString(exampleString2, date)
        checkRow(row2, "IA-11615", "ответы на комментарии в проектировании IA-12345", 2.0)

        val row3 = parser.parseWorkLogString(exampleString3, date)
        checkRow(row3, "IA-11615", "ответы на комментарии в : проектировании", 0.5)

        try {
            val row4 = parser.parseWorkLogString(exampleString4, date)
        } catch (e: Throwable) {
            assert(e is NoSuchElementException)
        }

        try {
            val row5 = parser.parseWorkLogString(exampleString5, date)
        } catch (e: Throwable) {
            assert(e is NoSuchElementException)
        }

        try {
            val row6 = parser.parseWorkLogString(exampleString6, date)
        } catch (e: Throwable) {
            assert(e is NoSuchElementException)
        }

        val row8 = parser.parseWorkLogString(exampleString8, date)
        checkRow(row8, "IA-11615", "ответы", 1.5)
        val row9 = parser.parseWorkLogString(exampleString9, date)
        checkRow(row9, "IA-11615", "ответы", 1.5)
        val row10 = parser.parseWorkLogString(exampleString10, date)
        checkRow(row10, "IA-11615", "ответы", 1.5)

    }

    private fun checkRow(row: WorkLogRow,
                         expectedIssue: String, expectedComment: String, expectedDuration: Double) {
        assertEquals(expectedIssue, row.issue)
        assertEquals(expectedComment, row.comment)
        assertEquals(expectedDuration, row.durationHours)
    }

    @Test
    fun getRowType() {

        val parser = WorkLogTextParser()

        assertEquals(WorkLogRowType.WORK_NOTE, parser.getRowType(exampleString1))
        assertEquals(WorkLogRowType.WORK_NOTE, parser.getRowType(exampleString2))
        assertEquals(WorkLogRowType.WORK_NOTE, parser.getRowType(exampleString3))
        assertEquals(WorkLogRowType.DATE, parser.getRowType(exampleString4))
        assertEquals(WorkLogRowType.DATE, parser.getRowType(exampleString5))
        assertEquals(WorkLogRowType.OTHER, parser.getRowType(exampleString6))
        assertEquals(WorkLogRowType.OTHER, parser.getRowType(exampleString7))
    }
}