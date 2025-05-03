
#### application.yaml
```yaml
ktor:
  application:
    modules:
      - de.coreMobile.server.ApplicationKt.module
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
docker compose build core-mobile-server
docker compose up -d
```
 
### Update core-mobile-server und Push to Docker Hub (coremobileserver- weil das project so heisst)
```bash
docker compose build core-mobile-server && 
docker tag coremobileserver-core-mobile-server:latest chromesd22159/core-mobile-server:latest &&
docker push chromesd22159/core-mobile-server:latest
```` 

```bash
docker tag protheseconnected-connected-server chromesd22159/core-mobile-server:latest &&
docker push chromesd22159/core-mobile-server:latest
```

```bash
docker exec -it coremobileserver-core-mobile-server-1 bash
```

#### Build und Teste Lokal
```bash
docker compose down &&   
docker compose up -d
```