package ru.rusviper.logic

import com.atlassian.jira.rest.client.api.JiraRestClient
import com.atlassian.jira.rest.client.api.domain.Comment
import com.atlassian.jira.rest.client.api.domain.Issue
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory
import java.net.URI


class JiraClient(val username: String, val password: String, val jiraUrl: String) {

    private val restClient: JiraRestClient

    init {
        restClient = AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(getJiraUri(), username, password)
    }

    private fun getJiraUri() = URI.create(jiraUrl)

    fun getIssue(issueId: String): Issue =
        restClient.issueClient.getIssue(issueId).get()

    fun addComment(issue: Issue, commentBody: String?) {
        restClient.issueClient
            .addComment(issue.commentsUri, Comment.valueOf(commentBody))
    }
}