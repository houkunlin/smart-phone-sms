package com.houkunlin.sms.web

import com.houkunlin.sms.entity.Client2ServerMessage
import com.houkunlin.sms.entity.Server2ClientMessage
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.WebSocketMessageBrokerStats
import java.security.Principal

@RestController
class WebSocketController(
    val messagingTemplate: SimpMessagingTemplate,
    val userRegistry: SimpUserRegistry,
    val webSocketHandler: WebSocketHandler,
    val webSocketMessageBrokerStats: WebSocketMessageBrokerStats,
//    val sessionManager: DefaultWebSessionManager,
//    val serverEndpointExporter: ServerEndpointExporter
) {
    @MessageMapping("/hello") // @MessageMapping 和 @RequestMapping 功能类似，浏览器向服务器发起消息，映射到该地址。
    @SendTo("/ws/subscribe/default") // 如果服务器接受到了消息，就会对订阅了 @SendTo 括号中的地址的浏览器发送消息。
    fun say(message: Client2ServerMessage): Server2ClientMessage {
        return Server2ClientMessage("Hello," + message.name.toString() + "!")
    }

    @MessageMapping("/sendTo")
    fun send(principal: Principal, @Payload body: Map<String, Any?>, @Headers headers: MessageHeaders) {
        val to = body["to"] ?: return
        if (to.toString().isBlank()) {
            return
        }
        val map = body.toMutableMap()
        map.remove("to")
        messagingTemplate.convertAndSend("$to", map)
    }

    @Scheduled(fixedRate = 500)
    fun sendUsers() {
        val users = userRegistry.users.map { user ->
            val sessions = user.sessions.map { session ->
                val subscriptions = session.subscriptions.map { subscription ->
                    mapOf(
                        "id" to subscription.id,
                        "destination" to subscription.destination
                    )
                }
                mapOf(
                    "id" to session.id,
                    "subscriptions" to subscriptions
                )
            }
            mapOf("name" to user.name, "sessions" to sessions)
        }

        val map = mapOf(Pair("users", users), Pair("userCount", userRegistry.userCount))
        messagingTemplate.convertAndSend("/ws/subscribe/users", map)
    }
}