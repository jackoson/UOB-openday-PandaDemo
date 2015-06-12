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

public class GeneHunt implements Player {

    private ScotlandYardView view;
    private Graph<Integer, Route> graph;
    private Dijkstra dijkstra;
    private PageRank pageRank;
    private List<Move> moveList;
    private ThreadCommunicator threadCom;

    /**
     * Constructs a new GeneHunt AI object.
     *
     * @param view the ScotlandYardView that contains information about the game.
     * @param graphFilename the path to the file that contains the Graph.
     * @param guiThreadCom the ThreadCommunicator object to communicate with the Event handling thread (GUI thread).
     */
    public GeneHunt(ScotlandYardView view, String graphFilename, ThreadCommunicator threadCom) {
        try {
            this.view = view;
            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            this.graph = graphReader.readGraph(graphFilename);
            this.dijkstra = new Dijkstra(graphFilename);
            this.pageRank = new PageRank(graph);
            this.pageRank.iterate(100);
            this.threadCom = threadCom;
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
        threadCom.putUpdate("valid_moves", new HashSet<Move>());
        Colour player = view.getCurrentPlayer();
        if (threadCom != null) updateUI(player);
        GameTree gameTree = new GameTree(graph, pageRank, dijkstra, view.getRound(), player, getPlayers(location, player), threadCom);
        Thread gameTreeThread = new Thread(gameTree);
        gameTreeThread.start();
        joinThread(gameTreeThread);
        Move move = gameTree.getMove();
        if (move == null || !moves.contains(move)) move = moves.iterator().next();
        return move;
    }

    private void joinThread(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            System.err.println("Gene Hunt was interrupted.");
            e.printStackTrace();
        }
    }

    // Updates the UI at the start of an AI move.
    // @param player the player whose turn it is.
    private void updateUI(Colour player) {
        threadCom.putUpdate("stop_timer", true);
        threadCom.putUpdate("ai_reset_prune", true);
        threadCom.putUpdate("zoom_out", true);
        threadCom.putUpdate("send_notification", "Gene is thinking about " + getPlayerMessage(player) + "'s Move");
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
        threadCom.putUpdate("update_tickets", newTickets);
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
