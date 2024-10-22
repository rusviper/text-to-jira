package api

//class ApiServer(val appConfig: AppConfig) {
//
//    fun get(relativeApiUrl: String, timeout: Int): String {
//        val client = HttpClient()
//        val response = client.get(appConfig.apiUrl + relativeApiUrl, timeout)
//        return response
//    }
//}

data class OutputError(val message: String, val error: Throwable?)