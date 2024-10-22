package components

import mui.material.Button
import react.*

external interface ButtonProps : Props {
    var text: String
    var onClick: () -> Unit
}

val myButton = FC<ButtonProps> { props ->
    Button {
        onClick = { props.onClick() }
        + props.text
    }
}

