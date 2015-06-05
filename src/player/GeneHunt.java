package player;

import scotlandyard.*;
import client.algorithms.*;
import client.application.*;
import client.model.*;

import java.util.*;
import java.awt.event.*;

/**
 * A class that uses a GameTree to make it a useful AI, it channels it's inner Gene Hunt to make the best Moves.
 */
 
public class GeneHunt implements Player, ActionListener {
    
    private ScotlandYardView view;
    private Graph<Integer, Route> graph;
    private Dijkstra dijkstra;
    private PageRank pageRank;
    private GameTree.GameTreeHelper gameTreeHelper = null;
    private List<Move> moveList;
    private ThreadCommunicator guiThreadCom;
    
    private final int kTurnTime = 10000;
    
    /**
     * Constructs a new GeneHunt AI object.
     *
     * @param view the ScotlandYardView that contains information about the game.
     * @param graphFilename the path to the file that contains the Graph.
     * @param guiThreadCom the ThreadCommunicator object to communicate with the Event handling thread (GUI thread).
     */
    public GeneHunt(ScotlandYardView view, String graphFilename, ThreadCommunicator guiThreadCom) {
        try {
            this.view = view;
            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            this.graph = graphReader.readGraph(graphFilename);
            this.dijkstra = new Dijkstra(graphFilename);
            this.pageRank = new PageRank(graph);
            this.pageRank.iterate(100);
            this.guiThreadCom = guiThreadCom;
        } catch (Exception e) {
            System.err.println("Error creating a new AI player :" + e);
            e.printStackTrace();
            System.exit(1);
        }
        
    }

    /**
     * Returns the Move chosen by the game tree.
     *
     * @param location the location of the player.
     * @param moves the Set of valid Moves for the player.
     * @return the Move chosen by the game tree.
     */
    @Override
    public Move notify(int location, Set<Move> moves) {
        Colour player = view.getCurrentPlayer();
        if (guiThreadCom != null) updateUI(player);
        Move move = null;
        if (gameTreeHelper == null) {
            List<GamePlayer> players = getPlayers(location, player);
            gameTreeHelper = GameTree.startTree(graph, pageRank, dijkstra, view.getRound(), view.getCurrentPlayer(), players, this, guiThreadCom);
        }
        wait(kTurnTime);
        move = gameTreeHelper.getSuggestedMove(view.getRound(), view.getCurrentPlayer());
        System.out.println(move);
        if (move == null) move = moves.iterator().next();
        //gameTreeHelper.setMove(move);
        //new Thread(gameTreeHelper).start();
        gameTreeHelper.stop();
        gameTreeHelper = null;
        guiThreadCom.putUpdate("clear_routes", true);
        return move;
    }
    
    private void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting in GeneHunt.");
        }
    }
    
    /**
     * Called when the game tree crashes and needs to be restarted.
     *
     * @param e the ActionEvent containing information about what object created it.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("game_tree_crashed")) {
            //Colour player = view.getCurrentPlayer();
            //List<GamePlayer> players = getPlayers(view.getPlayerLocation(player), player);
            //gameTreeHelper = GameTree.startTree(graph, pageRank, dijkstra, view.getRound(), view.getCurrentPlayer(), players, this);
        }
    }
    
    // Updates the UI at the start of an AI move.
    // @param player the player whose turn it is.
    private void updateUI(Colour player) {
        guiThreadCom.putUpdate("reset_timer", true);
        guiThreadCom.putUpdate("send_notification", "Gene is thinking about " + getPlayerMessage(player) + "'s Move");
        updateTickets(player);
    }
    
    // Returns the correct message for the specified player.
    // @param player the player whose message should be returned.
    // @return the correct message for the specified player.
    private String getPlayerMessage(Colour player) {
        if (player.equals(Colour.Black)) return "Mr X";
        else return "the " + player.toString() + " Detective";
    }
    
    // Updates the PlayerTicketView with the current players Tickets.
    // @param player the player for whom the PlayerTicketView should update.
    private void updateTickets(Colour player) {
        List<Object> newTickets = new ArrayList<Object>();
        newTickets.add(player);
        Map<Ticket, Integer> tickets = ModelHelper.getTickets(player, view);
        newTickets.add(tickets);
        guiThreadCom.putUpdate("update_tickets", newTickets);
    }
    
    // Returns the List of GamePlayer objects for the current game state.
    // @param location the location of the current player.
    // @param currentPlayer the Colour of the current player.
    // @return the List of GamePlayer objects for the current game state.
    private List<GamePlayer> getPlayers(int location, Colour currentPlayer) {
        List<Colour> players = view.getPlayers();
        List<GamePlayer> gamePlayers = new ArrayList<GamePlayer>();
        for (Colour player : players) {
            int loc = view.getPlayerLocation(player);
            if (player.equals(currentPlayer)) loc = location;
            gamePlayers.add(new GamePlayer(null, player, loc, ModelHelper.getTickets(player, view)));
        }
        return gamePlayers;
    }
    
}
