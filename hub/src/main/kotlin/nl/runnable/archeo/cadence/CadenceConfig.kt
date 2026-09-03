package nl.runnable.archeo.cadence

import com.uber.cadence.DomainAlreadyExistsError
import com.uber.cadence.RegisterDomainRequest
import com.uber.cadence.client.WorkflowClient
import com.uber.cadence.client.WorkflowClientOptions
import com.uber.cadence.serviceclient.ClientOptions
import com.uber.cadence.serviceclient.IWorkflowService
import com.uber.cadence.serviceclient.WorkflowServiceTChannel
import com.uber.cadence.worker.WorkerFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import nl.runnable.archeo.document.workflow.DocumentActivity
import nl.runnable.archeo.document.workflow.DocumentWorkflowImpl
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.getBean
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

private val logger = KotlinLogging.logger {}

@Configuration
class CadenceConfig {
    @Value($$"${cadence.host}")
    lateinit var host: String

    @Value($$"${cadence.port:7933}")
    var port: Int = 0

    @Value($$"${cadence.domain.name}")
    lateinit var domain: String

    @Value($$"${cadence.task-list}")
    lateinit var taskList: String

    @Value($$"${cadence.domain.retention}")
    var retention: Int = 0

    @Bean(destroyMethod = "close")
    fun workflowService(): IWorkflowService =
        WorkflowServiceTChannel(
            ClientOptions
                .newBuilder()
                .setHost(host)
                .setPort(port)
                .build(),
        )

    @Bean
    fun workflowClient(workflowService: IWorkflowService): WorkflowClient =
        WorkflowClient.newInstance(
            workflowService,
            WorkflowClientOptions.newBuilder().setDomain(domain).build(),
        )

    @EventListener(ApplicationStartedEvent::class)
    fun registerDomain(event: ApplicationStartedEvent) {
        val workflowService = event.applicationContext.getBean<IWorkflowService>()
        logger.info { "Registering Cadence domain '$domain'" }
        val request = RegisterDomainRequest()
        request.setName(domain)
        request.setEmitMetric(false)
        request.setWorkflowExecutionRetentionPeriodInDays(retention)

        try {
            workflowService.RegisterDomain(request)
        } catch (_: DomainAlreadyExistsError) {
        } catch (e: Exception) {
            logger.error { "Error registering Cadence domain '$domain': $e.message" }
        }
    }

    @EventListener(ApplicationStartedEvent::class)
    private fun registerWorkflows(event: ApplicationStartedEvent) {
        val applicationContext = event.applicationContext
        val workflowClient = applicationContext.getBean<WorkflowClient>()
        val factory = WorkerFactory.newInstance(workflowClient)
        val worker = factory.newWorker(taskList)
        worker.registerWorkflowImplementationTypes(
            DocumentWorkflowImpl::class.java,
        )
        worker.registerActivitiesImplementations(applicationContext.getBean<DocumentActivity>())
        factory.start()
    }
}
