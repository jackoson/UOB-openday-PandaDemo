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
    private GameTree gameTree;
    private List<Move> moveList;
    private ThreadCommunicator threadCom;
    
    public GeneHunt(ScotlandYardView view, String graphFilename) {
        //TODO: A better AI makes use of `view` and `graphFilename`.
        try {
            this.threadCom = new ThreadCommunicator();
            this.view = view;
            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            this.graph = graphReader.readGraph(graphFilename);
            this.dijkstra = new Dijkstra(graphFilename);
            this.pageRank = new PageRank(graph);
            this.gameTree = new GameTree();
        } catch (Exception e) {
            System.err.println("Error creating a new AI player :" + e);
            e.printStackTrace();
            System.exit(1);
        }
        
    }

    @Override
    public Move notify(int location, Set<Move> moves) {
        //TODO: Some clever AI here ...
        Colour currentPlayer = getCurrentPlayer(moves);
        List<GamePlayer> players = getPlayers(location, currentPlayer);
        gameTree.calculateTree(threadCom, graph, pageRank, dijkstra, players, view.getRounds(), view.getRound(), getCurrentGamePlayer(currentPlayer, players));
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
        return moveList.get(0);
    }
    
    private Colour getCurrentPlayer(Set<Move> moves) {
        Move move = moves.iterator().next();
        return move.colour;
    }
    
    private GamePlayer getCurrentGamePlayer(Colour currentPlayer, List<GamePlayer> players) {
        for (GamePlayer player : players) {
            if (player.colour().equals(currentPlayer)) return player;
        }
        return null;
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
