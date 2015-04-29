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
    private static final double kTicketInfluence = 0.5;
    private static final double kPageRankInfluence = 0.1;
    
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
    
    /**
     * Returns the parent.
     *
     * @return the parent.
     */
    public TreeNode getParent() {
        return parent;
    }
    
    /**
     * Sets the new parent of this node.
     *
     * @param parent the new parent of this node.
     */
    public void setParent(TreeNode parent) {
        this.parent = parent;
    }
    
    /**
     * Returns the curent state.
     *
     * @return the curent state.
     */
    public List<GamePlayer> getState() {
        return currentState;
    }
    
    /**
     * Returns the current player.
     *
     * @return the current player.
     */
    public Colour getPlayer() {
        return currentPlayer;
    }
    
    /**
     * Returns the round.
     *
     * @return the round.
     */
    public int getRound() {
        return round;
    }
    
    /**
     * Adds a child to be added to children.
     *
     * @param child the child to be added to children.
     */
    public void addChild(TreeNode child) {
        children.add(child);
    }
    
    /**
     * Returns the children.
     *
     * @return the children.
     */
    public List<TreeNode> getChildren() {
        return children;
    }
    
    /**
     * Returns the score.
     *
     * @return the score.
     */
    public double getScore() {
        if (score == null) score = score();
        return score;
    }
    
    /**
     * Returns the move.
     *
     * @return the move.
     */
    public Move getMove() {
        return move;
    }
    
    /**
     * Sets the best child for this node.
     *
     * @param bestChild the best child for this node.
     */
    public void setBestChild(TreeNode bestChild) {
        this.bestChild = bestChild;
    }
    
    /**
     * Returns the best child.
     *
     * @return the best child.
     */
    public TreeNode getBestChild() {
        return bestChild;
    }
    
    //Calculates the value of the given ticket.
    private Double ticketValue(Ticket t, Integer count) {
        if (t.equals(Ticket.Taxi)) return 1.0 * count;
        if (t.equals(Ticket.Bus)) return 2.0 * count;
        if (t.equals(Ticket.Underground)) return 4.0 * count;
        if (t.equals(Ticket.Secret)) return 7.0 * count;
        if (t.equals(Ticket.Double)) return 10.0 * count;
        return 0.0;
        
    }
    
    // Calculates the value of a players tickets.
    private Double playerTicketValue(Map<Ticket, Integer> tickets) {
        Double value = 0.0;
        for (Map.Entry<Ticket, Integer> entry : tickets.entrySet()) {
            value += ticketValue(entry.getKey(), entry.getValue());
        }
        return value;
    }

    // Calculates the score for this node.
    private double score() {
        //TODO: Implement the score function.
        Set<Colour> winningPlayers = ModelHelper.getWinningPlayers(currentState, currentPlayer, gameTree.graph, round);
        if (winningPlayers.contains(Colour.Black)) {System.err.println("Mr X has won in this state - " + winningPlayers); return Double.POSITIVE_INFINITY;}
        else if (winningPlayers.size() != 0) {System.err.println("Detectives have won in this state - " + winningPlayers); return Double.NEGATIVE_INFINITY;}
        GamePlayer mrX = currentState.get(0);
        int mrXLocation = mrX.location();
        if (mrXLocation <= 0) mrXLocation = 1;
        double mrXPageRank = gameTree.pageRank.getPageRank(mrXLocation);
        double mrXTicketValue = playerTicketValue(mrX.tickets());
        int closestDistance = Integer.MAX_VALUE;
        double detPageRank = 0.0;
        double detDistance = 0.0;
        double detTicketValue = 0.0;
        
        for (int i = 1; i < currentState.size(); i++) {
            GamePlayer player = currentState.get(i);
            Map<Ticket, Integer> detTickets = player.tickets();
            int detectiveLocation = player.location();
            detPageRank += gameTree.pageRank.getPageRank(detectiveLocation);
            int detectiveDistance = gameTree.dijkstra.getRoute(detectiveLocation, mrXLocation, convertDetTickets(detTickets)).size();
            if (detectiveDistance < closestDistance) closestDistance = detectiveDistance;
            detDistance += (double) detectiveDistance;
            detTicketValue += playerTicketValue(detTickets);
        }
        detPageRank /= (double) (currentState.size() - 1);
        detDistance /= (double) (currentState.size() - 1);
        
        double ticketRatio = mrXTicketValue / detTicketValue;
        double pageRankRatio = mrXPageRank / detPageRank;
        
        double score = (10 * Math.pow(detDistance, 0.5));
        score += ((ticketRatio - 1) * kTicketInfluence) * score;
        score += ((pageRankRatio - 1) * kPageRankInfluence) * score;
        
        if ((closestDistance < 3) && (move instanceof MoveTicket) && (((MoveTicket)move).ticket.equals(Ticket.Secret))) score += 2;//Need to adjust if we change things
        return score;
    } 
    
    //Converts the tickets for a player into a form useful to Dijkstra.
    private Map<Route, Integer> convertDetTickets(Map<Ticket, Integer> tickets) {
        Map<Route, Integer> routeMap = new HashMap<Route, Integer>();
        routeMap.put(Route.Taxi, tickets.get(Ticket.Taxi));
        routeMap.put(Route.Bus, tickets.get(Ticket.Bus));
        routeMap.put(Route.Underground, tickets.get(Ticket.Underground));
        routeMap.put(Route.Boat, tickets.get(Ticket.Secret));
        return routeMap;
    }
    
}