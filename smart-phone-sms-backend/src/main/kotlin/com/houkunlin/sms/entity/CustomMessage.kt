package com.houkunlin.sms.entity

data class Client2ServerMessage(var name: String? = null) {

}

data class Server2ClientMessage(var responseMessage: String? = null)
