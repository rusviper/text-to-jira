package components

import dom.html.HTMLInputElement
import dom.html.HTMLTextAreaElement
import mui.material.Input
import react.FC
import react.Props
import react.dom.html.ReactHTML.p

external interface InputFieldProps : Props {
    var name: String
    var value: String
    var defaultValue: String?
    var placeholder: String?
    var onChange: (String) -> Unit
}

val InputField = FC<InputFieldProps> { props ->
    p {
        + "${props.name}: "
    }
    Input {
        placeholder = props.placeholder
        defaultValue = props.defaultValue
        onChange = {
            val newValue = (it.target as HTMLInputElement).value
            if (props.onChange != null)
                props.onChange(newValue)
        }
    }
}

val MultiLineInputField = FC<InputFieldProps> { props ->
    p {
        + "${props.name}: "
    }
    Input {
        multiline = true
        minRows = 3
        placeholder = props.placeholder
        defaultValue = props.defaultValue

        onChange = {
            value = (it.target as HTMLTextAreaElement).value
            if (props.onChange != null)
                props.onChange(value)
        }
    }
}