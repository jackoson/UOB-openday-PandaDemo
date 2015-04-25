package client.application;

import client.application.*;
import player.*;

import net.*;

import java.util.*;
import java.util.concurrent.*;

public class ScotlandYardAIGame implements Runnable {
  
    private ScotlandYardApplication app;
    private ThreadCommunicator threadCom;
    private String hostname;
    private int port;
    private List<String> studentIds;
    
    public ScotlandYardAIGame(ScotlandYardApplication app, ThreadCommunicator threadCom, String hostname, int port, List<String> studentIds) {
        this.app = app;
        this.threadCom = threadCom;
        this.hostname = hostname;
        this.port = port;
        this.studentIds = studentIds;
    }
    
    public void run() {
        try {
            GeneHuntFactory factory = new GeneHuntFactory(app, threadCom);
            PlayerClient client = new PlayerClient(hostname, port, studentIds, factory);
            client.run();
        } catch (Exception e) {
            System.err.println("Error");
            System.exit(1);
        }
    }
    
}