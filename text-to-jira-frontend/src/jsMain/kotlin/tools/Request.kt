package tools

import data.AppConfig
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.js.jso
import web.http.Headers
import web.http.Request
import web.http.RequestInit

// bad, but works
fun makeGetRequest(
    config: AppConfig, relativeUrl: String,
    onError: (Throwable) -> Unit = ::defaultErrorCallback,
    onFinally: () -> Unit = { },
    result: (String) -> Unit
): Job {

    return MainScope().launch {

        val requestInit = jso<RequestInit> {
            method = "GET"
//            headers = Headers().apply {
//                set("Content-Type", "application/json")
//            }
        }

        makeRequestInner(config, relativeUrl, requestInit, onError, onFinally, result)
    }
}

inline fun <reified T> makeRequestWithData(
    config: AppConfig,
    relativeUrl: String,
    queryData: T,
    crossinline onError: (Throwable) -> Unit = ::defaultErrorCallback,
    crossinline onFinally: () -> Unit = { },
    crossinline result: (String) -> Unit): Job {

    return MainScope().launch {

        val requestInit = jso<RequestInit> {
            body = serializeJson(queryData)
            method = "POST"
            headers = Headers().apply {
                set("Content-Type", "application/json")
            }
        }

        makeRequestInner(config, relativeUrl, requestInit, onError, onFinally, result)
    }
}

inline fun makeRequestInner(
    config: AppConfig,
    relativeUrl: String,
    requestInit: RequestInit,
    crossinline onError: (Throwable) -> Unit = ::defaultErrorCallback,
    crossinline onFinally: () -> Unit = { },
    crossinline result: (String) -> Unit
) {

    Logger.debug("request.relativeUrl=${relativeUrl}")
    val request = Request(config.apiUrl + relativeUrl)

    Logger.debug("request=${request.url}, requestInit.body=${requestInit.body}")

    window.fetch(request, requestInit)
        .then {
            it.text()
                .then { fetched -> result.invoke(fetched) }
                .catch { err -> onError.invoke(err) }
                .finally { onFinally.invoke() }
        }

}

fun defaultErrorCallback(it: Throwable) {
    val message = if (it.message == null) "Unknown error" else
        "message=${it.message}, cause=${it.cause}, stackTrace=${it.stackTraceToString()}"
    window.alert(message)
}

