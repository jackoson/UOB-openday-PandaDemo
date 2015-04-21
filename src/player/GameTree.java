package player;

import scotlandyard.*;
import client.algorithms.*;
import client.model.*;

import java.util.*;

public class GameTree {
    
    private Graph<Integer, Route> graph;
    private PageRank pageRank;
    private Dijkstra routeFinder;
    
    public GameTree(Graph<Integer, Route> graph, PageRank pageRank, Dijkstra routeFinder, List<GamePlayer> players) {
        this.graph = graph;
        this.pageRank = pageRank;
        this.routeFinder = routeFinder;
        
        //
        TreeNode root = new TreeNode(null, players);
        addLayer(root, root.players);
    }
    
    private void addLayer(TreeNode parent, List<GamePlayer> players) {
        GamePlayer mrX = players.get(0);
        Set<Move> validMoves = ModelHelper.validMoves(mrX, players, graph);
        
        for (Move move : validMoves) {
            List<GamePlayer> clonedPlayers = cloneList(players);
            playMove(clonedPlayers, move);
            TreeNode node = new TreeNode(parent, clonedPlayers);
            //Do stuff
        }
    }
    
    private void playMove(List<GamePlayer> players, Move move) {
        if (move instanceof MoveTicket) playMove(players, (MoveTicket) move);
        else if (move instanceof MoveDouble) playMove(players, (MoveDouble) move);
    }
    
    private void playMove(List<GamePlayer> players, MoveTicket move) {
        GamePlayer player = getPlayer(players, move.colour);
        player.setLocation(move.target);
        player.removeTicket(move.ticket);
    }
    
    private void playMove(List<GamePlayer> players, MoveDouble move) {
        playMove(players, move.move1);
        playMove(players, move.move2);
        GamePlayer player = getPlayer(players, move.colour);
        player.removeTicket(Ticket.Double);
    }
    
    private List<GamePlayer> cloneList(List<GamePlayer> players) {
        List<GamePlayer> newPlayers = new ArrayList<GamePlayer>();
        for (GamePlayer player : players) {
            newPlayers.add(new GamePlayer(player));
        }
        return newPlayers;
    }
    
    private GamePlayer getPlayer(List<GamePlayer> players, Colour colour) {
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer.colour().equals(colour)) return gamePlayer;
        }
        return null;
    }
    
    private class TreeNode {
        
        public final List<GamePlayer> players;
        public static final double multiplier = 1.0;
        public final TreeNode parent;
        
        public TreeNode(TreeNode parent, List<GamePlayer> players) {
            this.parent = parent;
            this.players = players;
        }
        
        public double score() {
            int mrXLocation = players.get(0).location();
            double mrXPageRank = pageRank.getPageRank(mrXLocation);
            double sumDetPageRank = 0.0;
            double sumDetDistance = 0.0;
            boolean oneMoveAway = false;
            for (int i = 1; i < players.size(); i++) {
                GamePlayer player = players.get(i);
                int detLocation = player.location();
                sumDetPageRank += pageRank.getPageRank(detLocation);
                int detDistance = routeFinder.getRoute(detLocation, mrXLocation, convertDetTickets(player.tickets())).size();
                if (detDistance == 1) oneMoveAway = true; //?
                sumDetDistance += (double) detDistance;
            }
            double avgDetPageRank = sumDetPageRank / (double) (players.size() - 1);
            double avgDetDistance = sumDetDistance / (double) (players.size() - 1);
            double score = ((mrXPageRank * avgDetDistance) / avgDetPageRank) * TreeNode.multiplier;
            return score;
        }
        
        private Map<Route, Integer> convertDetTickets(Map<Ticket, Integer> tickets) {
            Map<Route, Integer> routeMap = new HashMap<Route, Integer>();
            routeMap.put(Route.Taxi, tickets.get(Ticket.Taxi));
            routeMap.put(Route.Bus, tickets.get(Ticket.Bus));
            routeMap.put(Route.Underground, tickets.get(Ticket.Underground));
            routeMap.put(Route.Boat, tickets.get(Ticket.Secret));
            return routeMap;
        }
        
    }
    
}