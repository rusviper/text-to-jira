package components

import data.loadConfig
import react.*
import react.Props
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.button
import react.useState

/**
 * Корневой компонент приложения
 */

enum class AppMode {
    ModeTestAlgorithm,

    ModeJiraWorkLog
}

external interface AppProps : Props {
    var activeMode: AppMode
}

val App = FC<AppProps> { appProps ->
    var activeMode by useState(AppMode.ModeJiraWorkLog)

    WelcomeHeader

    div {
        myButton {
            text = "Добавить записи Jira Worklog"
            onClick = {
                activeMode = AppMode.ModeJiraWorkLog
            }
        }
        myButton {
            text = "Тестовый режим"
            onClick = {
                activeMode = AppMode.ModeTestAlgorithm
            }
        }

    }
    div {
        when(activeMode) {
            AppMode.ModeTestAlgorithm -> showModeTestAlgorithm()
            AppMode.ModeJiraWorkLog -> showModeJiraWorklog()
        }
    }
}


fun ChildrenBuilder.showModeJiraWorklog() {
    val config = loadConfig()
    // проверяем доступность жиры с заданными параметрами пользователя
    div {
        // todo добавить параметры пользователя для подключения (заполнять из конфигурации)

        h2 {
            + "Статус соединения с Jira"
        }
        CheckJiraStatus {
            appConfig = config
        }
    }
    // форма ввода Jira Worklog
    JiraEnterWorklogForm {
        defaultParameters = null    // отображаются плейсхолдеры
        appConfig = config
    }

}

// по документации не желательно использовать ChildrenBuilder таким образом
fun ChildrenBuilder.showModeTestAlgorithm() {
    div {
        WelcomeComponent()
    }
    div {
        InputParametersForm {
            defaultValue = 6.0
            name = "d"
        }
        div {
            counter()
        }
    }

}

val counter = FC<Props> {
    var count by useState(0)
    button {
        onClick = { count += 1 }
        +count.toString()
    }
}