package components

import api.OutputError
import csstype.px
import data.AppConfig
import emotion.react.css
import kotlinx.js.jso
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mui.material.*
import react.FC
import react.Props
import react.dom.html.ReactHTML.p
import react.useEffect
import react.useState
import tools.Logger
import tools.makeGetRequest
import tools.makeRequestWithData
import tools.readJson
import kotlin.js.Date


/** Кнопочка "проверить соединение с Jira" **/

external interface CheckJiraStatusProps : Props {
    var appConfig: AppConfig
}

@Serializable
data class GetJiraStatusResult(val status: String,
                               val ping: Long, val jiraLink: String,
                               val serverInfo: String, val serverTitle: String)

val CheckJiraStatus = FC<CheckJiraStatusProps>("CheckJiraStatus") { props ->
    var needRecheck by useState(true)
    var errorParameters by useState(null as OutputError?)
    var requestInProgress by useState(false)
    var jiraStatusResult by useState(null as GetJiraStatusResult?)
    var lastCheckTime by useState(null as Date?)

    useEffect(needRecheck) {
        if (!needRecheck || requestInProgress) {
            Logger.debug("useEffect: CheckJiraStatus SKIP request")
            return@useEffect
        }

        requestInProgress = true
        Logger.debug("useEffect: CheckJiraStatus request")

        val job = makeGetRequest(props.appConfig, "/jira/check",
            onError = {
                errorParameters = OutputError(it.message ?: "Unknown error", it)
            },
            onFinally = {
                needRecheck = false
                requestInProgress = false
            }) {
            jiraStatusResult = readJson<GetJiraStatusResult>(it)
            errorParameters = null
            lastCheckTime = Date()
        }

        cleanup {
            job.cancel()
        }
    }

    Button {
        onClick = {
            needRecheck = true
        }

        +"Проверить соединение с Jira"
    }

    if(requestInProgress) {
        CircularProgress {
            css {
                marginLeft = 10.px
            }
        }
    }

    if (jiraStatusResult != null) {
        p {
            + "Ссылка: ${jiraStatusResult!!.jiraLink}"
        }

        p {
            + "Сервер: ${jiraStatusResult!!.serverTitle} (${jiraStatusResult!!.serverInfo})"
        }

        p {
            + "Пинг: ${jiraStatusResult!!.ping}ms"
        }
    }


    if (jiraStatusResult == null) {
        Alert {
            css {
                marginTop = 10.px
            }
            severity = AlertColor.error
            + "Проблема соединения с сервером приложения (не Jira)"
        }
    } else {
        if (jiraStatusResult!!.status == "OK") {
            Alert {
                severity = AlertColor.success
                + "Соединение с Jira успешно установлено"
            }
        } else {
            Alert {
                severity = AlertColor.warning
                + "Соединение с Jira не установлено"
            }
        }
    }
}