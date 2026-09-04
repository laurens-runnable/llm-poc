package nl.runnable.archeo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing
class HubApplication

fun main(args: Array<String>) {
    runApplication<HubApplication>(*args)
}
