package nl.runnable.archeo.elasticsearch

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration

@Configuration
class ClientConfig : ElasticsearchConfiguration() {
    @Value($$"${elasticsearch.endpoint}")
    lateinit var endpoint: String

    override fun clientConfiguration(): ClientConfiguration =
        ClientConfiguration
            .builder()
            .connectedTo(endpoint)
            .build()
}
