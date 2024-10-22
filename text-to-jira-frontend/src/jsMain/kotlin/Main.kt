import components.App
import components.AppMode
import kotlinx.browser.document
import react.*
import react.dom.client.createRoot

fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(container).render(App.create())
}

