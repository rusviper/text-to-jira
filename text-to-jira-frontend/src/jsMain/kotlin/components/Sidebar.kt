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
    var isOpen: Boolean
}

val Sidebar = FC<SidebarProps>("Sidebar") { props ->

    // todo нужно научиться управлять состоянием снаружи через пропс (из App)
    val (isSidebarOpen, setOpen) = useState(false)

    val toggleDrawer = { newOpen: Boolean ->
        setOpen(newOpen)
    }

    Box {
        css {
            backgroundColor = NamedColor.lightblue
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

            Box {
//                AppBar {
//                    position = AppBarPosition.fixed
//                    css {
//                        width = 250.px
//                        height = 64.px
//                        display = Display.flex
//                        alignItems = AlignItems.center
//                        justifyContent = JustifyContent.center
//                        backgroundColor = NamedColor.blue
//                    }
//                    Toolbar {
//                        Typography {
//                            variant = TypographyVariant.h6
//                            + "Боковое меню"
//                            css {
//                                color = NamedColor.white
//                            }
//                        }
//                    }
//                }
            }
            Divider

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