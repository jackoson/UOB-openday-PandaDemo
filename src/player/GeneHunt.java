package player;

import scotlandyard.*;
import client.algorithms.*;
import client.application.*;
import client.model.*;

import java.util.*;

/**
 * The RandomPlayer class is an example of a very simple AI that
 * makes a random move from the given set of moves. Since the
 * RandomPlayer implements Player, the only required method is
 * notify(), which takes the location of the player and the
 * list of valid moves. The return value is the desired move,
 * which must be one from the list.
 */
public class GeneHunt implements Player {
    
    private ScotlandYardView view;
    private Graph<Integer, Route> graph;
    private Dijkstra dijkstra;
    private PageRank pageRank;
    private GameTree gameTree = null;
    private List<Move> moveList;
    private ThreadCommunicator threadCom;
    private ThreadCommunicator guiThreadCom;
    
    public GeneHunt(ScotlandYardView view, String graphFilename, ThreadCommunicator guiThreadCom, GameTree gameTree) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
        try {
            this.threadCom = new ThreadCommunicator();
            this.view = view;
            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            this.graph = graphReader.readGraph(graphFilename);
            this.dijkstra = new Dijkstra(graphFilename);
            this.pageRank = new PageRank(graph);
            this.guiThreadCom = guiThreadCom;
        } catch (Exception e) {
            System.err.println("Error creating a new AI player :" + e);
            e.printStackTrace();
            System.exit(1);
        }
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...
        Colour player = view.getCurrentPlayer();
        updateUI(player);
        if (gameTree == null) {
            List<GamePlayer> players = getPlayers(location, player);
            gameTree = GameTree.startTree(threadCom, graph, pageRank, dijkstra, view.getRound(), view.getCurrentPlayer(), players);
        }
        gameTree.startTimer();
        
        while (true) {
            try {
                String id = (String)threadCom.takeEvent();
                Object object = threadCom.takeEvent();
                if (id.equals("calculated_moves")) {
                    moveList = (List<Move>) object;
                    for (Move m : moveList);
                    break;
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        Move move = moveList.get(0);
        gameTree.pruneTree(move);
        return move;
    }
    
    private void updateUI(Colour player) {
        guiThreadCom.putUpdate("reset_timer", true);
        guiThreadCom.putUpdate("send_notification", "Gene is thinking about " + getPlayerMessage(player) + "'s Move");
        updateTickets(player);
    }
    
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
