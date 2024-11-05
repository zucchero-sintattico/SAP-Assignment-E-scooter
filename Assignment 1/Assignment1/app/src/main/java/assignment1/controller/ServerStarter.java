package assignment1.controller;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.server.WebSocketHandler;

public class ServerStarter {

    public static void startServer(WebSocketHandler wsHandler) throws Exception {
        Server server = new Server(8080);
        server.setHandler(wsHandler);
        server.start();

        System.out.println("Server started on http://localhost:8080/");
    }
    
}
