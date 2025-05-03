
## Installation 

#### local.properties 
```gradle
jwt.secret=de.frederikkohler
jwt.issuer=de.frederikkohler
jwt.audience=your-audience
jwt.accessTokenExpiration=1728000
jwt.refreshTokenExpiration=7200
```

#### docker-compose.yaml
```yaml
version: '0.1'
services:
  auth-api:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
  db:
    image: postgres
    volumes:
      - ./tmp/db:/var/lib/postgresql/data
    environment:
      POSTGRES_DB: "ktor_tutorial_db"
      POSTGRES_HOST_AUTH_METHOD: trust
    ports:
      - "5432:5432"
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U postgres" ]
      interval: 1s
```


#### Build und Teste Lokal
```bash
docker compose -f docker-compose.local.yml down &&  
docker compose -f docker-compose.local.yml build auth-api &&
docker compose -f docker-compose.local.yml build posts-api && 
docker compose -f docker-compose.local.yml up --watch
```

#### Vorhandenes Image Lokal starten
```bash
docker compose -f docker-compose.local.yml down &&  
docker compose -f docker-compose.local.yml up -d 
```  

#### all images
```bash
docker compose -f docker-compose.local.yml images 
```

#### stop images
```bash 
docker compose -f docker-compose.local.yml stop auth-api
```

### delete local db
```bash 
docker compose down
rm -rf ./tmp/db
```

#### Delete images
```bash
docker compose -f docker-compose.local.yml stop auth-api
docker compose -f docker-compose.local.yml rm auth-api
``` 

### Update AuthApi und Push to Docker Hub
```bash
docker compose build auth-api &&
docker tag protheseconnected-auth-api prothese-connected-auth-api:latest &&
docker tag prothese-connected-auth-api chromesd22159/auth-api:latest &&
docker push chromesd22159/auth-api:latest && 
docker compose down && 
##docker compose -f docker-compose.prod.yml up -d 
````

### Update PostsApi und Push to Docker Hub
```bash  
docker compose build posts-api && 
docker tag protheseconnected-posts-api prothese-connected-posts-api:latest &&
docker tag prothese-connected-posts-api chromesd22159/posts-api:latest &&
docker push chromesd22159/posts-api:latest && 
docker compose down && 
##docker compose -f docker-compose.prod.yml up -d 
````

### Lösche all caches
```bash
docker rmi $(docker images -aq)
docker builder prune --all
```

```bash
### Stelle deine Anwendungs-Services bereit
kubectl apply -f auth-api-deployment.yaml
kubectl apply -f auth-api-service.yaml
kubectl apply -f posts-api-deployment.yaml
kubectl apply -f posts-api-service.yaml
kubectl apply -f db-deployment.yaml
kubectl apply -f db-service.yaml

kubectl scale deployment/auth-api-deployment --replicas=0 ### pod entfernen

kubectl get pods -n ingress-nginx ### prüfe ingres
kubectl apply -f ingress.yaml ### ingres anwenden
kubectl get service -n ingress-nginx ### kubectl get service -n ingress-nginx

kubectl rollout restart deployment/posts-api-deployment
kubectl rollout status deployment/posts-api-deployment
```

### [open swagger SwaggerDoc](http://0.0.0.0:8080/swagger/index.html#/)
### [Ktor SwaggerDoc](https://smiley4.github.io/ktor-openapi-tools/latest/examples/request-response/)