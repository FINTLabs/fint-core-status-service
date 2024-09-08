# FINT Core Status Service

## Overview
The **fint-core-status-service** is a backend service designed to fetch and present metadata and events produced by the [provider-gateway](link to project). The service updates and displays metadata in a presentable way, while also updating the health status of each **AdapterContract** by monitoring their heartbeats.

### Key Features:
- **Heartbeat Monitoring**: Listens to and tracks heartbeat events from each adapter to update their health status.
- **Sync Events**: Fetches and monitors full-sync, delta-sync, and delete-sync events from each adapter. These events represent data sets that include full data transmissions, updates, and deletions.
- **Entity Tracking**: Fetches the total number of entities being sent by each adapter to track how much data is delivered by each one.

## Running the Project Locally

### Prerequisites:
- Java 21
- Docker
- Docker Compose

### Steps to Run Locally:
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd fint-core-status-service
   ```
2. Start the service with the local profile
    ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```
3. Run the docker-compose file
    ```bash
   docker-compose up -d
   ```
4. Boot up the application in you preferred IDE