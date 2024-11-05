package assignment1.view;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import assignment1.controller.ServerStarter;
import assignment1.model.ModelObserver;
import assignment1.model.ModelObserverSource;

public class WebView implements ModelObserver{

    //Tengo traccia delle connessioni Web
    private static List<Session> sessions = new CopyOnWriteArrayList<>();
    public ModelObserverSource model;
    
    //crea un server Jetty WebSocket sulla porta 8080.
    //L'oggetto WekSocketHandler gestirà le richieste WebSocket. All'interno di esso, 'webSocket.class' viene registrato come gestore
    //WebSocket.
    public WebView(ModelObserverSource model) throws Exception{  
        this.model = model;
        this.model.addObserver(this);
        
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(WebSocketOperations.class);
            }
        };

        ServerStarter.startServer(wsHandler);
        System.out.println("Open the html page, located in the resources folder, in your browser !");
    }

    @Override
    public void notifyModelUpdated() { //Quando il modello viene aggiornato, questo metodo viene chiamato. 
        //Viene ottenuto il valore del contatore dal modello e inviato a tutti i client WebSocket connessi.
        int counterValue = model.getState();
        String message = "{\"counter\": " + counterValue + "}";
        sendToAll(message);

    }

    private void log(String msg) {
		System.out.println("[Web Server] " + msg);
	}

    public void sendToAll(String message) { //Questo metodo invia un messaggio a tutte le sessioni WebSocket attualmente connesse.
        for (Session session : sessions) {
            try {
                log("model updated => updating the web view");
                session.getRemote().sendString(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @WebSocket
    public static class WebSocketOperations{
        @OnWebSocketConnect
        public void onConnect(Session session) {
            sessions.add(session);
            System.out.println("Nuova connessione WebSocket");
        }

        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {
            sessions.remove(session);
            System.out.println("Connessione WebSocket chiusa");
        }
    }

}
