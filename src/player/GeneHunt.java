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
    private GameTree.GameTreeHelper gameTreeHelper = null;
    private List<Move> moveList;
    private ThreadCommunicator threadCom;
    private ThreadCommunicator guiThreadCom;
    
    public GeneHunt(ScotlandYardView view, String graphFilename, ThreadCommunicator guiThreadCom) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
        try {
            this.threadCom = new ThreadCommunicator();
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

    @SuppressWarnings("unchecked")
    @Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...
        long startTime = System.nanoTime();
        Colour player = view.getCurrentPlayer();
        updateUI(player);
        if (gameTreeHelper == null) {
            List<GamePlayer> players = getPlayers(location, player);
            gameTreeHelper = GameTree.startTree(threadCom, graph, pageRank, dijkstra, view.getRound(), view.getCurrentPlayer(), players);
        }
        System.err.println("Time: " + (System.nanoTime() - startTime));
        startTime = System.nanoTime();
        gameTreeHelper.startTimer();
        
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
        System.err.println("TimeB: " + (System.nanoTime() - startTime));
        startTime = System.nanoTime();
        Move move = null;
        for (Move m : moveList) {
            if (m.colour.equals(view.getCurrentPlayer())) move = m;
        }
        if (move == null) {
            move = moves.iterator().next();
            System.err.println("Bad stuffs has happened.");
        }
        gameTreeHelper.setMove(move);
        //new Thread(gameTreeHelper).start();
        gameTreeHelper.stop();
        gameTreeHelper = null;
        System.err.println("TimeC: " + (System.nanoTime() - startTime));
        startTime = System.nanoTime();
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
