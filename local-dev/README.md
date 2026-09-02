# Local development

Docker containers for local development.

* PostgreSQL
* [Cadence Workflow](https://cadenceworkflow.io/)
* [SeaweedFS](https://seaweedfs.com/)

```bash
# Start
./bin/start.sh

# Follow logs
./bin/follow-logs.sh

# Stop
./bin/stop.sh
```

## S3 usage

Seaweed S3 is configured with the following credentials:  
[`config/seaweedfs_s3/s3-users.json`](config/seaweedfs_s3/s3-users.json)

```bash
# Connect using AWS CLI
export AWS_ACCESS_KEY_ID=admin
export AWS_SECRET_ACCESS_KEY=admin
aws --endpoint-url http://localhost:8333 s3 ls

```
