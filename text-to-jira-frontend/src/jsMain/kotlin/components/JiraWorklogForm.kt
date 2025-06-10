package components

import api.OutputError
import tools.makeRequestWithData
import data.AppConfig
import csstype.*
import emotion.react.css
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import mui.material.CircularProgress
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.h1
import react.useEffect
import react.useState
import tools.Logger
import tools.readJson
import kotlin.js.Date


// region data

// server models
@Serializable
data class InputParameters(val expectedCount: Int, val worklogText: String)
@Serializable
data class OutputParameters(val rowsCount: Int)



// endregion


// region components

external interface FormParametersProps : Props {
    var defaultParameters: InputParameters?
    var appConfig: AppConfig
}


val JiraEnterWorklogForm = FC<FormParametersProps>("InputParametersForm") { rootProps ->
    var inputParameters by useState(rootProps.defaultParameters?: InputParameters(0, ""))
    var outputParameters by useState(null as OutputParameters?)
    var errorParameters by useState(null as OutputError?)
    var queryActive by useState(false)
    var inputParamsUpdated by useState(false)
    var lastUpdateTime by useState(null as Date?)

    // отслеживаем нужное изменение параметра, которое будет говорить о необходимости перезапроса
    useEffect(inputParamsUpdated) {
        // если обновлено - это вводятся значения
        // todo нужно ли проверять, выполняется ли запрос в данный момент?
        if (inputParamsUpdated)
            return@useEffect

        // если "не обновлено" - значит сброшено значение флага и нужно обновить
        queryActive = true
        Logger.debug("useEffect: inputParameters=${inputParameters}")

        val job = makeRequestWithData(rootProps.appConfig, "/text/check", inputParameters,
            onError = {
                errorParameters = OutputError(it.message ?: "Unknown error", it)
            },
            onFinally = {
                queryActive = false
            }) {
            outputParameters = readJson(it)
            errorParameters = null
            lastUpdateTime = Date()
        }

        cleanup {
            job.cancel()
        }
    }

    ParametersInputs {
        defaultParameters = inputParameters
        onChangeExpectedCount = {
            inputParameters = inputParameters.copy(expectedCount = it)
            inputParamsUpdated = true
            Logger.debug("onChangeExpectedCount: inputParameters=${inputParameters}, expectedCount=${it}")
        }
        onChangeWorklogText = {
            inputParameters = inputParameters.copy(worklogText = it)    // выполняет setState, но не присвоение?
            inputParamsUpdated = true
            Logger.debug("onChangeWorklogText: inputParameters=${inputParameters}, text=${it}")
        }
    }

    QueryButtons {
        parameters = inputParameters
        appConfig = rootProps.appConfig
        onCheck = {
            inputParamsUpdated = false
        }
        onSubmit = {
            inputParamsUpdated = false
        }
    }

   ResultFields {
       this.queryActive = queryActive
       this.outputParameters = outputParameters
       this.errorParameters = errorParameters
       this.lastUpdateTime = lastUpdateTime
   }

}

external interface InputParametersProps : Props {
    var defaultParameters: InputParameters?
    var onChangeExpectedCount: (Int) -> Unit
    var onChangeWorklogText: (String) -> Unit
}

val ParametersInputs = FC<InputParametersProps> { props ->
    div {
        css {
            // на всю ширину экрана
            width = 100.pct
            // для размещения двух полей ввода рядом друг с другом
            display = Display.flex
            justifyContent = JustifyContent.spaceBetween
        }

        div {
            css {
                margin = 20.px
                flexBasis = 30.pct
            }

            // справа ввод параметра
            InputField {
                name = "Введите ожидаемое количество записей"
                placeholder = "Например, 10"
                defaultValue = props.defaultParameters?.expectedCount?.toString()
                onChange = {
                    props.onChangeExpectedCount.invoke(it.toInt())
                }
            }
        }
        div {
            css {
                margin = 20.px
                flexBasis = 65.pct
            }
            // слева ввод ворклога
            MultiLineInputField {
                name = "Введите текст ворклога"
                placeholder = "Например, \n1 - 10123: писал тесты"
                defaultValue = props.defaultParameters?.worklogText
                onChange = {
                    props.onChangeWorklogText.invoke(it)
                }
            }
        }
    }
}

external interface QueryParametersProps : Props {
    var parameters: InputParameters
    var appConfig: AppConfig
    var onCheck: () -> Unit
    var onSubmit: () -> Unit
}

val QueryButtons = FC<QueryParametersProps> { props ->

    div {
        css {
            // на всю ширину экрана
            margin = 20.px
            display = Display.flex
        }

        div {
            css {
                marginLeft = 50.px
            }
            // справа кнопка "проверить"
            myButton {
                text = "Проверить"
                onClick = {
                    props.onCheck()
                }

            }
        }
        div {
            css {
                marginLeft = 50.px
            }
            // слева кнопка "отправить"
            myButton {
                text = "Отправить"
                onClick = {
                    props.onSubmit()
                }
            }
        }
    }
}

external interface ResultFieldsProps : Props {
    var errorParameters: OutputError?
    var outputParameters: OutputParameters?
    var queryActive: Boolean
    var lastUpdateTime: Date?
}

val ResultFields = FC<ResultFieldsProps> { props ->

    div {
        css {
            // на всю ширину экрана
        }
        h1 {
            + "Результаты расчета:"
        }
        if (props.errorParameters != null) {
            div {
                css {
                    color = NamedColor.red
                }
                p {
                    + props.errorParameters!!.message
                }
                p {
                    + props.errorParameters!!.error!!.stackTraceToString()
                }
            }
        }
        div {
            // список параметров с результатами
            p {
                val resultsString = if (props.outputParameters == null)
                    "Данные ещё не были получены"
                else
                    props.outputParameters!!.toString()
                + resultsString
            }
        }
        if (props.queryActive) {
            div {
                // невидимый спиннер
                CircularProgress {
                    //variant = CircularProgressVariant.determinate
                    //value = 75
                }
            }
        } else {
            p {
                + "Последняя дата обновления: ${props.lastUpdateTime?:"-"}"
            }
        }
    }
}

// endregion