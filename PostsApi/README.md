
#### application.yaml
```yaml
ktor:
  application:
    modules:
      - de.frederikkohler.postsapi.ApplicationKt.module
  deployment:
    host: 0.0.0.0
    port: 8080
storage:
  driverClassName: "org.postgresql.Driver"
  jdbcURL: "jdbc:postgresql://db:5432/<DB_NAME>"
  user: "postgres"
  password: "password"
jwt:
  secret: "de.frederikkohler"
  issuer: "de.frederikkohler"
  audience: "your-audience"
  accessTokenExpiration: 864000
  refreshTokenExpiration: 3600
``` 
 
#### Build IMAGE und Teste Lokal
```bash
docker compose down &&  
docker compose build posts-api && 
docker compose up -d
```

#### Build und Teste Lokal
```bash
docker compose down &&   
docker compose up -d
```