package client.application;

import net.*;

import java.util.concurrent.*;

public class ScotlandYardAIGame implements Runnable {
    
    private PlayerClient client;
    
    public ScotlandYardAIGame(PlayerClient client) {
        this.client = client;
    }
    
    public void run() {
        client.run();
    }
    
}