package player;

import scotlandyard.*;
import client.algorithms.*;
import client.model.*;

import java.util.*;

public class GameTree {
    
    private Graph<Integer, Route> graph;
    private PageRank pageRank;
    private Dijkstra routeFinder;
    
    private List<Boolean> rounds;
    private Integer round;
    private GamePlayer currentPlayer;
    public static final int kMaxDepth = 6;
    private int depth;
    
    public GameTree(Graph<Integer, Route> graph, PageRank pageRank, Dijkstra routeFinder, List<GamePlayer> players, List<Boolean> rounds, Integer round, GamePlayer currentPlayer) {
        this.graph = graph;
        this.pageRank = pageRank;
        this.routeFinder = routeFinder;
        this.rounds = rounds;
        this.round = round;
        depth = 0;
        
        //
        TreeNode root = new TreeNode(null, players, null, false);
        addLayer(root, players);
    }
    
    private double addLayer(TreeNode parent, List<GamePlayer> players, Double minBest, Double maxBest) {
        Set<Move> validMoves = ModelHelper.validMoves(currentPlayer, players, graph);
        
        if(currentPlayer.colour().equals(Colour.Black)) round++;
        currentPlayer = ModelHelper.getNextPlayer(players, currentPlayer);
        
        Double currentBestScore = 0.0;
        if (parent.minimum) currentBestScore = Double.POSITIVE_INFINITY;
        TreeNode currentBestNode = null;
        
        for (Move move : validMoves) {
            List<GamePlayer> clonedPlayers = cloneList(players);
            playMove(clonedPlayers, move);
            boolean minimum = currentPlayer.colour().equals(Colour.Black);
            TreeNode node = new TreeNode(parent, clonedPlayers, move, minimum);
            //Do stuff
            if (depth == kMaxDepth) {
                if ((parent.minimiser && node.score > maxBest) || (!parent.minimum && node.score < minBest)){
                    return node;
                }
                
                if ((minimum && node.score > currentBestScore) || (!minimum && node.score < currentBestScore)){
                    currentBestScore = node.score;
                    currentBestNode = node;
                }
                
                return currentBestNode;
            }
            if (depth < kMaxDepth) addLayer(node, clonedPlayers);
        }
        return 1.0;
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
        
        public static final double kMultiplier = 1.0;
        public static final double kMax = 10.0;
        public static final double kMin = -10.0;
        public final TreeNode parent;
        public final boolean minimum;
        public Move move;
        public final double score;
        
        public TreeNode(TreeNode parent, List<GamePlayer> players, Move move, boolean minimum) {
            this.parent = parent;
            this.minimum = minimum;
            this.move = move;
            //this.score = score(players);
        }
        
        public double score(List<GamePlayer> players) {
            Set<Colour> winningPlayers = ModelHelper.getWinningPlayers(players, currentPlayer, graph, rounds, round);
            if (winningPlayers.contains(Colour.Black)) return TreeNode.kMax;
            if (winningPlayers.size() != 0) return TreeNode.kMin;
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
            double score = ((mrXPageRank * avgDetDistance) / avgDetPageRank) * TreeNode.kMultiplier;
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