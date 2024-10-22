package components

import csstype.*
import emotion.react.css
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.p


external interface InputFormProps : Props {
    var name: String
    var defaultValue: Double
}

// форма для ввода параметров и отправки запроса
val InputParametersForm = FC<InputFormProps>("InputParametersForm") { props ->
    a {
        +"Введите желаемое значение параметра ${props.name}: "
    }
    input {
        type = InputType.text
        value = props.defaultValue
    }
    button {
        onClick = { TODO("Отправлять запрос на расчёт") }
        +"Ок"
    }
}




