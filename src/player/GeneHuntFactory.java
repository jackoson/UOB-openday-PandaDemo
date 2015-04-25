package player;

import client.application.*;
import net.PlayerFactory;
import scotlandyard.Colour;
import scotlandyard.Player;
import scotlandyard.ScotlandYardView;
import scotlandyard.Spectator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The GeneHuntFactory is a PlayerFactory that
 * creates a series of Gene Hunt players. By default it assigns
 * Gene Hunt to be Mr X and all other players are controlled by the
 * GUI.
 */
public class GeneHuntFactory implements PlayerFactory {
    protected Map<Colour, PlayerType> typeMap;

    public enum PlayerType {AI, GUI}

    private String graphFilename;
    private ScotlandYardGame gui;
    private ScotlandYardApplication application;
    private ThreadCommunicator threadCom;
    private GameTree gameTree;
    
    private final int kTimerTime = 15;

    public GeneHuntFactory(ScotlandYardApplication application, ThreadCommunicator threadCom) {
        this.application = application;
        this.threadCom = threadCom;
        this.gameTree = new GameTree();
        
        typeMap = new HashMap<Colour, PlayerType>();
        typeMap.put(Colour.Black, GeneHuntFactory.PlayerType.AI);
        typeMap.put(Colour.Blue, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.Green, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.Red, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.White, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.Yellow, GeneHuntFactory.PlayerType.GUI);
        
        graphFilename = "resources/graph.txt";
    }

    public GeneHuntFactory(ScotlandYardApplication application, ThreadCommunicator threadCom, Map<Colour, PlayerType> typeMap, String graphFilename) {
        this.application = application;
        this.threadCom = threadCom;
        this.typeMap = typeMap;
        this.graphFilename = graphFilename;
    }

    @Override
    public Player player(Colour colour, ScotlandYardView view, String graphFilename) {        
        switch (typeMap.get(colour)) {
            case AI:
                return new GeneHunt(view, graphFilename, gameTree);
            case GUI:
                return gui(view);
            default:
                return new GeneHunt(view, graphFilename, gameTree);
        }
    }

    @Override
    public void ready() {
        if (gui != null && application != null) {
            application.beginGame(kTimerTime);
            application.newAIGame(gui);
        }
    }

    @Override
    public List<Spectator> getSpectators(ScotlandYardView view) {
        List<Spectator> specs = new ArrayList<Spectator>();
        specs.add(gui(view));
        specs.add(gameTree);
        return specs;
    }

    @Override
    public void finish() {
        if (gui != null && application != null) {
            application.endGame();
        }
    }


    private ScotlandYardGame gui(ScotlandYardView view) {
        if (gui == null) {
            gui = new ScotlandYardGame(view, graphFilename, threadCom);
        }
        return gui;
    }
}
