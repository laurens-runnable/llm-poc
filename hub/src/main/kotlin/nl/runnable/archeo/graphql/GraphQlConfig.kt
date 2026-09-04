package nl.runnable.archeo.graphql

import graphql.scalars.ExtendedScalars
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer

@Configuration
class GraphQlConfig {
    @Bean
    fun runtimeWiringConfigurer() = RuntimeWiringConfigurer { it.scalar(ExtendedScalars.DateTime) }
}
