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
 * A class to create both AI and GUI players. By default it makes only Mr X an AI player and everyone else a GUI player.
 */

public class GeneHuntFactory implements PlayerFactory {
    protected Map<Colour, PlayerType> typeMap;

    public enum PlayerType {AI, GUI}

    private String graphFilename;
    private ScotlandYardGame gui;
    private GeneHunt ai;
    private ScotlandYardApplication application;
    private ThreadCommunicator threadCom;

    private final int kTimerTime = 10;

    /**
     * Constructs a new GeneHuntFactory object.
     *
     * @param application the ScotlandYardApplication that has started the game.
     * @param threadCom the ThreadCommunicator object to communicate with the Event handling thread (GUI Thread).
     */
    public GeneHuntFactory(ScotlandYardApplication application, ThreadCommunicator threadCom) {
        this.application = application;
        this.threadCom = threadCom;

        typeMap = new HashMap<Colour, PlayerType>();
        typeMap.put(Colour.Black, GeneHuntFactory.PlayerType.AI);
        typeMap.put(Colour.Blue, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.Green, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.Red, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.White, GeneHuntFactory.PlayerType.GUI);
        typeMap.put(Colour.Yellow, GeneHuntFactory.PlayerType.GUI);

        graphFilename = "resources/graph.txt";
    }

    /**
     * Constructs a new GeneHuntFactory object.
     *
     * @param application the ScotlandYardApplication that has started the game.
     * @param threadCom the ThreadCommunicator object to communicate with the Event handling thread (GUI Thread).
     * @param typeMap the Map that decides who should be AI players and who should be GUI players.
     * @param graphFilename the path to the file containing the Graph.
     */
    public GeneHuntFactory(ScotlandYardApplication application, ThreadCommunicator threadCom, Map<Colour, PlayerType> typeMap, String graphFilename) {
        this.application = application;
        this.threadCom = threadCom;
        this.typeMap = typeMap;
        this.graphFilename = graphFilename;
    }

    /**
     * Returns the correct Player for the specified player in the game.
     *
     * @param colour the Colour of the player in the game.
     * @param view the ScotlandYardView containing information about the game.
     * @param graphFilename the path to the file containing the Graph.
     * @return the correct Player for the specified player in the game.
     */
    @Override
    public Player player(Colour colour, ScotlandYardView view, String graphFilename) {
        switch (typeMap.get(colour)) {
            case AI:
                return ai(view, graphFilename, threadCom);
            case GUI:
                return gui(view);
            default:
                return new GeneHunt(view, graphFilename, threadCom);
        }
    }

    /**
     * Called when the game is ready.
     * Updates the UI to start the game.
     */
    @Override
    public void ready() {
        if (gui != null && application != null) {
            application.beginGame(kTimerTime);
            application.newAIGame(gui);
        }
    }

    /**
     * Returns a List of Spectators of the game.
     * The only spectator is the GUI.
     *
     * @return a List of Spectators of the game.
     */
    @Override
    public List<Spectator> getSpectators(ScotlandYardView view) {
        List<Spectator> specs = new ArrayList<Spectator>();
        specs.add(gui(view));
        return specs;
    }

    /**
     * Called when the game ends.
     * Updates the UI to end the game.
     */
    @Override
    public void finish() {
        if (gui != null && application != null) {
            application.endGame();
        }
    }

    private GeneHunt ai(ScotlandYardView view, String graphFilename, ThreadCommunicator threadCom) {
        if (ai == null) {
            ai = new GeneHunt(view, graphFilename, threadCom);
        }
        return ai;
    }

    /**
     * Returns the GUI associated with the game.
     *
     * @param view the ScotlandYardView that contains the information about the game.
     * @return the GUI associated with the game.
     */
    private ScotlandYardGame gui(ScotlandYardView view) {
        if (gui == null) {
            gui = new ScotlandYardGame(view, graphFilename, threadCom);
        }
        return gui;
    }
}
