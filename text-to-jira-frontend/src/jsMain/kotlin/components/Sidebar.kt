package components

import csstype.*
import emotion.react.css
import kotlinx.js.jso
import react.*
import mui.material.*
import mui.material.styles.TypographyVariant
import react.dom.aria.ariaLabel

external interface SidebarProps : Props {
    var onModeChange: (AppMode) -> Unit
    var selectedAppMode: AppMode
}

val Sidebar = FC<SidebarProps>("Sidebar") { props ->
    val (isSidebarOpen, setOpen) = useState(false)

    val toggleDrawer = { newOpen: Boolean ->
        setOpen(newOpen)
    }

    Box {
        css {
            backgroundColor = NamedColor.lightblue
        }

        Button {
            onClick = { toggleDrawer(true) }
            + "Open Drawer"
        }

        AppBar {
            position = AppBarPosition.fixed
            Toolbar {
                IconButton {
                    //ariaLabel = "open drawer"
                    edge = IconButtonEdge.start
                    onClick = { toggleDrawer(!isSidebarOpen) }
                    + "Меню"
                }

                Typography {
                    variant = TypographyVariant.h6

                    + if (props.selectedAppMode == AppMode.ModeJiraWorkLog) "Jira Worklog" else "Test Algorithm"
                }
            }
        }
        
        Drawer {
            variant = DrawerVariant.temporary
            open = isSidebarOpen
            onClose = { _,_ -> toggleDrawer(false) }
            PaperProps = jso {
                style = jso {
                    width = 250.px
                }
            }
            List {
                ListItem {
                    Button {
                        +"Jira Worklog"
                        onClick = {
                            props.onModeChange(AppMode.ModeJiraWorkLog)
                            toggleDrawer(false)
                        }
                    }
                }
                ListItem {
                    Button {
                        +"Тестовый режим"
                        onClick = {
                            props.onModeChange(AppMode.ModeTestAlgorithm)
                            toggleDrawer(false)
                        }
                    }
                }
            }
        }
    }
}