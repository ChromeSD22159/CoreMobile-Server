
#### application.yaml
```yaml
ktor:
  application:
    modules:
      - de.frederikkohler.authapi.ApplicationKt.module
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

#### Build und Teste Lokal
```bash
docker compose down &&  
docker compose build connected-server && 
docker compose up -d
```

#### Build und Teste Lokal
```bash
docker compose down &&   
docker compose up -d
```

### Update connected-server und Push to Docker Hub (coremobileserver- weil das project so heisst)
```bash
docker compose build connected-server &&
docker tag coremobile-server-connected-server:latest chromesd22159/connected-server:latest &&
docker push chromesd22159/connected-server:latest
````