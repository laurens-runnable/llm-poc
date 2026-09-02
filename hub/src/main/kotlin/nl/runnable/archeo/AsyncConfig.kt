package nl.runnable.archeo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Configuration
class AsyncConfig {
    @Bean(AsyncExecutors.SINGLE)
    fun synchronizedExecutor(): Executor = Executors.newSingleThreadExecutor()
}

object AsyncExecutors {
    const val SINGLE = "single"
}
