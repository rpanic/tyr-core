package handlers

interface Handler {
    fun routes(handler: MainHandler) : MainHandler
}