package com.houkunlin.sms.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.annotation.PostConstruct

@Configuration
class WebConfig(val objectMapper: ObjectMapper) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    @PostConstruct
    fun post() {
        // json序列化驼峰，json反序列化下划线
        objectMapper.propertyNamingStrategy = object : PropertyNamingStrategy.SnakeCaseStrategy() {
            override fun nameForGetterMethod(
                config: MapperConfig<*>?,
                method: AnnotatedMethod?,
                defaultName: String?
            ): String? {
                return defaultName
            }
        }

    }
}