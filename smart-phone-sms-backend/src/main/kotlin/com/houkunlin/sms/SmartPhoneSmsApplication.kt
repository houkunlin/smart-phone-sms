package com.houkunlin.sms

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class SmartPhoneSmsApplication

fun main(args: Array<String>) {
    runApplication<SmartPhoneSmsApplication>(*args)
}
