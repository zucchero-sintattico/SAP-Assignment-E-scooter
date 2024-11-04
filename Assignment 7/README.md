# Software Architecture and Platforms - a.y. 2023-2024

## Assignment 7 - Observable Microservices

In questo assignment ho messo in pratica alcuni patttern per la gestione dei microservizi tra cui:
- Health check API: introdotto con un'API /health presente nell'api gateway e nel cooperativePixelArtService;
- Application metrics: implementato utilizzando Prometheus e Grafana per visualizzare meglio le metriche scelte;
- Distributed Logging: creato un nuovo microservizio in grado di ricevere i vari log da tutti gli altri microservizi e di inviarli sotto forma json a chiunque fa richieste GET;
- Distributed Tracing: ho utilizzato Zipkin, con relativa dashboard in grado di raccogliere le informazioni sui tempi di risposta e sui percorsi delle richieste all'interno dei microservizi.

# Deployment
- Eseguire `docker compose up`
- Avviare DistributedLogService e poi tutti gli altri
- Una volta fatto ciò bisogna settare Grafana. Quindi andare su localhost:3000, dopodichè importare i dati e utilizzare Prometheus come sorgente. Immettere l'URL di prometheus: http://host.docker.internal:9090/ e cliccare SAVE & TEST. Dopodichè andare su Explore Now.