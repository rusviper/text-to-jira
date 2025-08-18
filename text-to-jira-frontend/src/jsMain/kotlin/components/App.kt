package components

import csstype.*
import emotion.react.css
import data.loadConfig
import mui.material.*
import mui.material.styles.TypographyVariant
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
    var isSidebarOpen by useState(false)

    val toggleSidebar = { newOpen: Boolean ->
        isSidebarOpen = newOpen
    }
    Box {

        css {
            padding = 20.px
        }

        AppBar {
            position = AppBarPosition.fixed
            Toolbar {
                IconButton {
                    //ariaLabel = "open drawer"
                    edge = IconButtonEdge.start
                    onClick = { toggleSidebar(!isSidebarOpen) }
                    //+ "Меню"
                    Icon {
                        + "menu"
                    }
                }

                Typography {
                    variant = TypographyVariant.h6

                    + if (activeMode == AppMode.ModeJiraWorkLog) "Jira Worklog" else "Test Algorithm"
                }
            }
        }

        Sidebar {
            onModeChange = { newMode ->
                activeMode = newMode
            }
            selectedAppMode = activeMode
            isOpen = isSidebarOpen
        }

        Box {
            when (activeMode) {
                AppMode.ModeTestAlgorithm -> showModeTestAlgorithm()
                AppMode.ModeJiraWorkLog -> showModeJiraWorklog()
            }
        }
    }
}


fun ChildrenBuilder.showModeJiraWorklog() {
    val config = loadConfig()
    // проверяем доступность жиры с заданными параметрами пользователя
    Box {
        css {
            padding = 20.px
            display = Display.flex
            flexDirection = FlexDirection.column
            alignItems = AlignItems.center
        }

        // todo добавить параметры пользователя для подключения (заполнять из конфигурации)

        Typography {
            variant = TypographyVariant.h2
            +"Статус соединения с Jira"
        }

        CheckJiraStatus {
            appConfig = config
        }
        // форма ввода Jira Worklog
        JiraEnterWorklogForm {
            defaultParameters = null    // отображаются плейсхолдеры
            appConfig = config
        }
    }
}

// по документации не желательно использовать ChildrenBuilder таким образом
fun ChildrenBuilder.showModeTestAlgorithm() {
    Box {
        css {
            padding = 20.px
            display = Display.flex
            flexDirection = FlexDirection.column
            alignItems = AlignItems.center
        }

        WelcomeComponent()

        InputParametersForm {
            defaultValue = 6.0
            name = "d"
        }

        Box {
            css {
                marginTop = 20.px
            }
            counter()
        }
    }

}

val counter = FC<Props> {
    var count by useState(0)

    Button {
        variant = ButtonVariant.contained
        color = ButtonColor.primary
        onClick = { count += 1 }
        +count.toString()
    }
}