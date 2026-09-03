# Local development

Docker containers for local development.

* PostgreSQL
* [Cadence Workflow](https://cadenceworkflow.io/)
* [SeaweedFS](https://seaweedfs.com/)

## Run containers

```bash
# Start
./bin/start.sh

# Follow logs
./bin/follow-logs.sh

# Stop
./bin/stop.sh
```

## Workflow

```bash
# Start workflow for given document
./bin/workflow/start.sh <document_d>

# List workflows
./bin/workflow/list

# List open workflows
./bin/workflow/list --open

# Show workflow details
./bin/workflow/list <workflow_id>
```
