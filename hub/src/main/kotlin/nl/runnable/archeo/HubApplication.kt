package nl.runnable.archeo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class HubApplication

fun main(args: Array<String>) {
    runApplication<HubApplication>(*args)
}
