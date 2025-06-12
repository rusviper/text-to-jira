package components

import csstype.AlignContent
import csstype.NamedColor
import csstype.pct
import csstype.px
import emotion.react.css
import mui.material.Box
import mui.material.Typography
import mui.material.styles.TypographyVariant
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.h1
import react.useEffect
import react.useEffectOnce
import kotlin.js.Date

val WelcomeHeader = FC<Props>("WelcomeHeader") {

    Box {
        css {
            width = 20.px
            height = 20.px
            borderRadius = 50.pct
            alignContent = AlignContent.center
        }
    }
    Typography {
        variant = TypographyVariant.h1
        + getWelcomeString(Date())
    }
}

/**
 * Функция определяет строку приветствия в зависимости от текущего часа. Варианты:
 * - доброе утро (3..12)
 * - добрый день (12..17)
 * - добрый вечер (17..22)
 * - доброй ночи (22..3)
 **/
fun getWelcomeString(date: Date): String {
    val hour = Date().getHours()
    if (hour >= 3 && hour < 12) {
        return "Доброе утро!"
    } else if (hour >= 12 && hour < 17) {
        return "Добрый день!"
    } else if (hour >= 17 && hour < 22) {
        return "Добрый вечер!"
    } else {
       return "Доброй ночи!"
    }
}

val WelcomeComponent = FC<Props>("WelcomeComponent") {
    div {
        css {
            // на всю ширину экрана
        }
        div {
            css {
                backgroundColor = NamedColor.blue
                width = 20.px
                height = 20.px
                borderRadius = 50.pct
            }
        }

        p {
            css {
                alignContent = AlignContent.center
            }
            val welcomeString = "Попробуем посчитать наиболее оптимальные параметры выражения a*x^2+b*x+c=d для " +
                    "заданного значения параметра d методом генетического алгоритма."
            + welcomeString
        }
    }
}