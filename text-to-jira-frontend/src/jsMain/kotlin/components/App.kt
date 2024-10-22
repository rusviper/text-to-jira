package components

import data.loadConfig
import react.*
import react.Props
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.button
import react.useState

/**
 * Корневой компонент приложения
 */

enum class AppMode {
    Mode1, Mode2
}

external interface AppProps : Props {
    var activeMode: AppMode
}

val App = FC<AppProps> { appProps ->
    var activeMode by useState(AppMode.Mode2)

    WelcomeHeader

    div {
        myButton {
            text = "Режим 1"
            onClick = {
                activeMode = AppMode.Mode1
            }
        }
        myButton {
            text = "Режим 2"
            onClick = {
                activeMode = AppMode.Mode2
            }
        }
    }
    div {
        when(activeMode) {
            AppMode.Mode1 -> showMode1()
            AppMode.Mode2 -> showMode2()
        }
    }
}


fun ChildrenBuilder.showMode2() {
    val config = loadConfig()
    JiraEnterWorklogForm {
        defaultParameters = null    // отображаются плейсхолдеры
        appConfig = config
    }
    div {
        h2 {
            + "Статус соединения с Jira"
        }
        CheckJiraStatus {
            appConfig = config
        }
    }
}

// по документации не желательно использовать ChildrenBuilder таким образом
fun ChildrenBuilder.showMode1() {
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