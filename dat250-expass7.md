During the containerization process, several issues occurred that had to be resolved. Initially, Docker was not running, which caused build commands to fail with a daemon connection error. Starting Docker Desktop and verifying the setup with docker info solved this.

The next issue was a build error caused by a missing build stage in the Dockerfile (failed to resolve source metadata for docker.io/library/build:latest). This was fixed by defining a proper multi-stage build using FROM gradle:8-jdk21 AS build.

After building the image, the application produced Kafka connection errors because it tried to reach localhost:9092 inside the container. The solution was to add a Docker Compose setup including a Kafka service and to configure SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092 so the containers could communicate correctly.

Another problem was that Docker Compose initially failed to find the image locally and attempted to pull it from Docker Hub. Adding a build: section to the Compose file ensured the image was built locally.

Once these issues were fixed, the containerized application ran successfully and returned the expected JSON responses, confirming that the setup was working correctly.