package player;

import scotlandyard.*;
import client.model.*;

import java.util.*;

public class TreeNode {
    
    private TreeNode parent;
    private final List<GamePlayer> currentState;
    private final Colour currentPlayer;
    private final int round;
    private final Move move;
    private final GameTree gameTree;
    private List<TreeNode> children;
    private TreeNode bestChild = null;
    private Double score = null;
    
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
    
    public TreeNode getParent() {
        return parent;
    }
    
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }
    
    public List<GamePlayer> getState() {
        return currentState;
    }
    
    public Colour getPlayer() {
        return currentPlayer;
    }
    
    public int getRound() {
        return round;
    }
    
    public void addChild(TreeNode child) {
        children.add(child);
    }
    
    public List<TreeNode> getChildren() {
        return children;
    }
    
    public double getScore() {
        if (score == null) score = score();
        return score;
    }
    
    public Move getMove() {
        return move;
    }
    
    public void setBestChild(TreeNode bestChild) {
        this.bestChild = bestChild;
    }
    
    public TreeNode getBestChild() {
        return bestChild;
    }
    
    private double score() {
        //TODO: Implement the score function.
        Set<Colour> winningPlayers = ModelHelper.getWinningPlayers(currentState, currentPlayer, gameTree.graph, round);
        if (winningPlayers.contains(Colour.Black)) return Double.POSITIVE_INFINITY;
        else if (winningPlayers.size() != 0) return Double.NEGATIVE_INFINITY;
        int mrXLocation = currentState.get(0).location();
        if (mrXLocation == 0) mrXLocation = 1;
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
        return mrXPageRank;
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