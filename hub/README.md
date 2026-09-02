# Hub

## Run

```bash
./mvnw spring-boot:run
```

## Workflow

```bash
# Start workflow
docker run --network=host --rm ubercadence/cli:master --do archeo-domain workflow start --tasklist ArcheoTaskList --workflow_type DocumentWorkflow::editMetadata --execution_timeout 3600 --input \"<uuid>\"

# List open workflows
docker run --network=host --rm ubercadence/cli:master --do archeo-domain workflow list --open

# List completed workflows
docker run --network=host --rm ubercadence/cli:master --do archeo-domain workflow list

# Show workflow history
docker run --network=host --rm ubercadence/cli:master --do archeo-domain workflow showid <id>
```
