# Installation

Diese Anwendung kann optional mit Firebase-Integration betrieben werden. Um die Firebase-Admin-SDK zu nutzen, führe folgende Schritte aus:

1.  **Firebase Admin SDK herunterladen (optional):**
    * Gehe zu deinem Firebase-Projekt in der Firebase-Konsole.
    * Navigiere zu "Projektübersicht" > "Projekteinstellungen" (Zahnrad-Symbol) > "Dienstkonten".
    * Generiere einen neuen privaten Schlüssel.
    * Lade die JSON-Datei mit den Anmeldeinformationen herunter.

2.  **Datei umbenennen und speichern (optional):**
    * Benenne die heruntergeladene JSON-Datei in `serviceAccountKey.json` um.
    * **Speichere diese Datei im selben Ordner, in dem sich deine `docker-compose.yaml`-Datei befindet.**

3.  **Docker Compose starten:**
    * Stelle sicher, dass Docker und Docker Compose auf deinem System installiert sind.
    * Öffne ein Terminal oder eine Befehlszeile.
    * Navigiere zu dem Ordner, der deine `docker-compose.yaml`-Datei enthält (in deinem Fall der `rootFolder`).
    * Führe den folgenden Befehl aus, um die Anwendung zu starten:
        ```bash
        docker compose up -d
        ```

**Hinweis zur optionalen Firebase-Konfiguration:**

* Wenn du die `serviceAccountKey.json`-Datei nicht herunterlädst und im selben Ordner wie die `docker-compose.yaml` speicherst, wird die Anwendung ohne Firebase-Admin-SDK-Funktionalität gestartet. Bestimmte Features der Anwendung könnten in diesem Fall eingeschränkt sein.
* Die Anwendung versucht, die Anmeldeinformationen unter `/app/CoreMobileServer/serviceAccountKey.json` im Container zu finden. Durch den `volumes`-Eintrag in der `docker-compose.yaml` wird die lokale `serviceAccountKey.json`-Datei (falls vorhanden) in diesen Pfad im Container gemountet.
```yaml
# ... deine docker-compose.yaml ...
volumes:
  - ./serviceAccountKey.json:/app/CoreMobileServer/serviceAccountKey.json:ro
# ...
```

## Ordnerstruktur
- docker-compose.yaml
- nginx.conf
- serviceAccountKey.json

## Docker Compose File
```yaml
services:
  core-mobile-server:
    image: chromesd22159/core-mobile-server:latest
    ports:
      - "8080:8080"
    volumes:
      - ./serviceAccountKey.json:/app/CoreMobileServer/serviceAccountKey.json:ro
    depends_on:
      db:
        condition: service_healthy
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - core-mobile-server
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

## nginx.conf erstellen

```nginx configuration
events {}

http {
    server {
        listen 80;
        server_name localhost;

         location / {
             proxy_pass http://core-mobile-server:8080/;
         }
    }
}
```