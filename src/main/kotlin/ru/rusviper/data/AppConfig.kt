package ru.rusviper.data

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(val app: AppRootConfig)

@Serializable
data class AppRootConfig(val jira: JiraConfig, val job: JobConfig)

@Serializable
data class JobConfig(val inputFile: String)

@Serializable
data class JiraConfig(val login: String, val password: String, val jiraUrl: String)