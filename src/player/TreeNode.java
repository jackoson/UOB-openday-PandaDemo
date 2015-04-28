package player;

import scotlandyard.*;
import client.model.*;

import java.util.*;

public class TreeNode {
    
    private volatile TreeNode parent;
    private final List<GamePlayer> currentState;
    private final Colour currentPlayer;
    private final int round;
    private final Move move;
    private final GameTree gameTree;
    private volatile List<TreeNode> children;
    private volatile TreeNode bestChild = null;
    private volatile Double score = null;
    
    public static final double kMultiplier = 1.0;
    public static final double kMax = 10.0;
    public static final double kMin = -10.0;
    
    public TreeNode(TreeNode parent, List<GamePlayer> currentState, Colour currentPlayer,
                    int round, Move move, GameTree gameTree) {
        this.parent = parent;
        this.currentState = currentState;
        this.currentPlayer = currentPlayer;
        this.round = round;
        this.move = move;
        this.gameTree = gameTree;
        this.children = new ArrayList<TreeNode>();
    }
    
    public synchronized TreeNode getParent() {
        return parent;
    }
    
    public synchronized void setParent(TreeNode parent) {
        this.parent = parent;
    }
    
    public synchronized List<GamePlayer> getState() {
        return currentState;
    }
    
    public synchronized Colour getPlayer() {
        return currentPlayer;
    }
    
    public synchronized int getRound() {
        return round;
    }
    
    public synchronized void addChild(TreeNode child) {
        children.add(child);
    }
    
    public synchronized List<TreeNode> getChildren() {
        return children;
    }
    
    public synchronized double getScore() {
        if (score == null) score = score();
        return score;
    }
    
    public synchronized Move getMove() {
        return move;
    }
    
    public synchronized void setBestChild(TreeNode bestChild) {
        this.bestChild = bestChild;
    }
    
    public synchronized TreeNode getBestChild() {
        return bestChild;
    }
    
    private synchronized double score() {
        //TODO: Implement the score function.
        Set<Colour> winningPlayers = ModelHelper.getWinningPlayers(currentState, currentPlayer, gameTree.graph, round);
        if (winningPlayers.contains(Colour.Black)) {System.err.println("SCORE-N"); return Double.POSITIVE_INFINITY;}
        else if (winningPlayers.size() != 0) {System.err.println("SCORE"); return Double.NEGATIVE_INFINITY;}
        int mrXLocation = currentState.get(0).location();
        if (mrXLocation <= 0) mrXLocation = 1;
        double mrXPageRank = gameTree.pageRank.getPageRank(mrXLocation);
        int closestDistance = Integer.MAX_VALUE;
        double detPageRank = 0.0;
        double detDistance = 0.0;
        
        for (int i = 1; i < currentState.size(); i++) {
            GamePlayer player = currentState.get(i);
            int detectiveLocation = player.location();
            detPageRank += gameTree.pageRank.getPageRank(detectiveLocation);
            int detectiveDistance = gameTree.dijkstra.getRoute(detectiveLocation, mrXLocation, convertDetTickets(player.tickets())).size();
            if (detectiveDistance < closestDistance) closestDistance = detectiveDistance;
            detDistance += (double) detectiveDistance;
        }
        
        detPageRank /= (double) (currentState.size() - 1);
        detDistance /= (double) (currentState.size() - 1);
        detPageRank *= (Math.pow(detDistance, 2) / 50);
        
        // Need to finish.
        return detDistance;
    } 
    
    private synchronized Map<Route, Integer> convertDetTickets(Map<Ticket, Integer> tickets) {
        Map<Route, Integer> routeMap = new HashMap<Route, Integer>();
        routeMap.put(Route.Taxi, tickets.get(Ticket.Taxi));
        routeMap.put(Route.Bus, tickets.get(Ticket.Bus));
        routeMap.put(Route.Underground, tickets.get(Ticket.Underground));
        routeMap.put(Route.Boat, tickets.get(Ticket.Secret));
        return routeMap;
    }
    
}