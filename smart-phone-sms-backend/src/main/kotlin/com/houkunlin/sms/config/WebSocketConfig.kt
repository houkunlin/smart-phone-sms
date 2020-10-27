package com.houkunlin.sms.config

import com.houkunlin.sms.entity.User
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration
import org.springframework.web.socket.handler.WebSocketHandlerDecorator
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession
import java.security.Principal
import java.util.concurrent.atomic.AtomicInteger


/**
 * @EnableWebSocketMessageBroker注解用于开启使用STOMP协议来传输基于代理（MessageBroker）的消息，这时候控制器（controller）开始支持@MessageMapping,就像是使用@requestMapping一样。
 * @author HouKunLin
 * @date 2020/10/21 0021 11:57
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {
    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)!!
    final val IS_WEB_SOCKET = "IS_WEB_SOCKET"
    final val IS_STOMP = "IS_STOMP"
    val number = AtomicInteger(0)
    val websocket = AtomicInteger(0)

    @Bean
    fun serverEndpointExporter(): ServerEndpointExporter {
        return ServerEndpointExporter()
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        val handler = object : DefaultHandshakeHandler() {
            override fun determineUser(
                request: ServerHttpRequest,
                wsHandler: WebSocketHandler,
                attributes: MutableMap<String, Any>
            ): Principal? {
                return User("WebSocket 用户-${websocket.incrementAndGet()}")
            }
        }
        val interceptor = object : HandshakeInterceptor {
            override fun beforeHandshake(
                request: ServerHttpRequest,
                response: ServerHttpResponse,
                wsHandler: WebSocketHandler,
                attributes: MutableMap<String, Any>
            ): Boolean {
                return true
            }

            override fun afterHandshake(
                request: ServerHttpRequest,
                response: ServerHttpResponse,
                wsHandler: WebSocketHandler,
                exception: Exception?
            ) {
            }
        }
        // 注册一个 Stomp 的节点(endpoint),并指定使用 SockJS 协议。
        registry
            .addEndpoint("/ws/stomp")
            .setAllowedOrigins("*")
            .setHandshakeHandler(handler)
            .addInterceptors(interceptor)
            .withSockJS()
        registry
            .addEndpoint("/ws/stomp")
            .setAllowedOrigins("*")
            .setHandshakeHandler(handler)
            .addInterceptors(interceptor)
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
                val accessor =
                    MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java) ?: return message
                val sessionAttributes =
                    SimpMessageHeaderAccessor.getSessionAttributes(message.headers) ?: mutableMapOf()

                if (StompCommand.CONNECT == accessor.command) {
                    val login = accessor.login
                    val passcode = accessor.passcode
                    val host = accessor.host
                    val token = accessor.getFirstNativeHeader("token")
                    logger.info("Stomp 登录: {}:{}@{} , Token = {}", login, passcode, host, token)
                    val index = number.incrementAndGet()
                    val user = if (!login.isNullOrBlank()) {
                        // 设置当前访问器的认证用户
                        User(login)
                    } else {
                        User("STOMP 用户-${index}")
                    }
                    accessor.user = user
                    sessionAttributes[IS_STOMP] = true
                    logger.info("用户 {} 上线", user.name)
                }
                if (StompCommand.DISCONNECT == accessor.command) {
                    // 为了保证幂等性加 !accessor.isMutable 条件，因为 StompCommand.DISCONNECT == accessor.command 被调用了两次
                    // 添加 sessionAttributes[IS_STOMP] != null 条件是因为单纯通过 WebSocket 连接断开连接时同样会触发 StompCommand.DISCONNECT == accessor.command 条件
                    if (!accessor.isMutable && sessionAttributes[IS_STOMP] != null) {
                        // 执行到这里能够有效保证该连接一定是 Stomp 连接，同时也一定只会触发一次
                        number.decrementAndGet()
                        logger.info("用户 {} 下线", accessor.user?.name ?: "未知用户")
                    }
                }
                return message
            }
        })
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 广播式配置名为 /nasus 消息代理 , 这个消息代理必须和 controller 中的 @SendTo 配置的地址前缀一样或者全匹配
        registry.enableSimpleBroker("/ws/subscribe")
        // val enableStompBrokerRelay = registry.enableStompBrokerRelay("/ws/subscribe")
        // enableStompBrokerRelay.setUserRegistryBroadcast("hou")
        // 设置发送消息前缀，发送消息时，所有的端点都要带上该前缀
        // registry.setApplicationDestinationPrefixes("/app")
    }

    override fun configureWebSocketTransport(registry: WebSocketTransportRegistration) {
        registry.addDecoratorFactory {
            object : WebSocketHandlerDecorator(it) {
                override fun afterConnectionEstablished(session: WebSocketSession) {
                    val principal = session.principal
                    if (session !is WebSocketServerSockJsSession && session !is StandardWebSocketSession) {
                        return
                    }
                    session.attributes[IS_WEB_SOCKET] = true
                    if (principal != null) {
                        val username = principal.name
                        logger.info("用户 {} 上线", username)
                    }
                    super.afterConnectionEstablished(session)
                }

                override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
                    val principal = session.principal
                    if (session !is WebSocketServerSockJsSession && session !is StandardWebSocketSession) {
                        return
                    }
                    if (principal != null) {
                        websocket.decrementAndGet()
                        val username = principal.name
                        logger.info("用户 {} 下线", username)
                    }
                    super.afterConnectionClosed(session, closeStatus)
                }
            }
        }
    }
}