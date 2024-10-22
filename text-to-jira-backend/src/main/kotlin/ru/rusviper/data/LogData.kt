package ru.rusviper.data

import java.time.LocalDateTime

data class WorkLogRow(
    /** Ид задачи (IA-11616) **/
    val issue: String,

    /** Длительность работы в часах **/
    val durationHours: Double,

    /** Описание работ **/
    val comment: String,

    /** Когда работа была сделана **/
    val date: LocalDateTime)

enum class WorkLogRowType {
    DATE, WORK_NOTE, OTHER
}


data class WorkDateRow(
    /** Ид задачи (IA-11616) **/
    val issue: String,

    /** Длительность работы в часах **/
    val durationHours: Double,
)

data class JiraLogRecord(val data: WorkLogRow)

