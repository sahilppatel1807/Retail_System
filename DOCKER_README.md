# Docker Setup for Retail Microservices

This project now supports Docker deployment using Docker Compose.

## Prerequisites

- Docker Desktop installed
- Docker Compose installed (included with Docker Desktop)

## Running with Docker

### Start All Services

```bash
# Build and start all containers
docker-compose up --build

# Or run in detached mode (background)
docker-compose up --build -d
```

### Stop All Services

```bash
# Stop all containers
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f warehouse
docker-compose logs -f retailer
docker-compose logs -f customer
docker-compose logs -f customer-ui
```

## Services

| Service | Port | URL |
|---------|------|-----|
| PostgreSQL | 5432 | - |
| Warehouse | 8081 | http://localhost:8081/api/warehouse |
| Retailer | 8082 | http://localhost:8082/api/retailer |
| Customer | 8083 | http://localhost:8083/api/customer |
| Customer UI | 3000 | http://localhost:3000 |

## Architecture

```
customer-ui (React) → customer (8083) → retailer (8082) → warehouse (8081) → postgres (5432)
```

## Environment Variables

The services use environment variables for configuration:

- **Retailer Service**: `WAREHOUSE_HOST` (default: localhost)
- **Customer Service**: `RETAILER_HOST` (default: localhost)
- **React UI**: `REACT_APP_API_URL` (default: http://localhost:8083)
- **All Spring Boot**: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`

## Development

To run locally without Docker, the services will use the default `localhost` values.

## Troubleshooting

**Services not starting**: Check logs with `docker-compose logs -f <service-name>`

**Database connection issues**: Wait for PostgreSQL health check to pass

**Port conflicts**: Ensure ports 3000, 5432, 8081-8083 are available
