package tools

object Logger {

    private val debugLogging = true

    fun debug(message: String) {
        if (debugLogging)
            console.log(message)
    }


    fun log(message: String) {
        console.log(message)
    }
}