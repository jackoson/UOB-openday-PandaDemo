package player;

import scotlandyard.*;
import client.algorithms.*;
import client.application.*;
import client.model.*;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.Timer;

/**
 * A class that implements a game tree using Alpha-Beta pruning.
 */

public class GameTree implements Runnable {
  
    static ForkJoinPool pool = new ForkJoinPool();
  
    public final Graph<Integer, Route> graph;
    public final PageRank pageRank;
    public final Dijkstra dijkstra;
    
    private final Integer round;
    private final Colour initialPlayer;
    private final List<GamePlayer> initialState;
    
    public boolean iterate = true;
    private TreeNode root;
    private int iterationDepth;
    private static boolean prune = false;
    
    private static GameTree.GameTreeHelper helper = null;

    /**
     * Constructs a new GameTree object.
     *
     * @param graph the Graph associated with the game.
     * @param pageRank the PageRank object associated with the game.
     * @param dijkstra the Dijkstra object associated with the game.
     * @param round the current round of the game.
     * @param initialPlayer the current player in the game.
     * @param initialState the List of GamePlayer objects representing the current state of the game.
     */
    public GameTree(Graph<Integer, Route> graph, 
                    PageRank pageRank, Dijkstra dijkstra, int round, Colour initialPlayer,
                    List<GamePlayer> initialState) {
        this.graph = graph;
        this.pageRank = pageRank;
        this.dijkstra = dijkstra;
        this.round = round;
        this.initialPlayer = initialPlayer;
        this.initialState = initialState;
    }
    
    /**
     * Static function to create a GameTree and a GameTreeHelper 
     * and start the relevant threads.
     *
     * @param threadCom the ThreadCommunicator object to put the Moves on after an iteration of the game tree.
     * @param graph the Graph associated with the game.
     * @param pageRank the PageRank object associated with the game.
     * @param dijkstra the Dijkstra object associated with the game.
     * @param round the current round of the game.
     * @param initialPlayer the current player in the game.
     * @param initialState the List of GamePlayer objects representing the current state of the game.
     * @return the new GameTreeHelper object.
     */
    public static GameTreeHelper startTree(ThreadCommunicator threadCom, Graph<Integer, Route> graph,
                    PageRank pageRank, Dijkstra dijkstra, int round, Colour initialPlayer,
                    List<GamePlayer> initialState, ActionListener listener) {
        if (helper != null) helper.stop();
        GameTree gameTree = new GameTree(graph, pageRank, dijkstra,
                                          round, initialPlayer, initialState);
        new Thread(gameTree).start();
        helper = new GameTreeHelper(gameTree, listener);
        return helper;
    }
    
    /**
     * Stops the game tree.
     */
    public void stop() {
        iterate = false;
        GameTree.prune = true;
    }
    
    /**
     * Returns the GameTreeHelper object.
     *
     * @return the GameTreeHelper object.
     */
    public static GameTreeHelper getGameTreeHelper() {
        return helper;
    }
    
    /**
     * Returns the tree root.
     *
     * @return the tree root.
     */
    public TreeNode getRoot() {
        return root;
    }
    
    /**
     * Decrements the iterationDepth variable.
     */
    public void decrementIterationDepth() {
        iterationDepth--;
    }
    
    /**
     * Starts a new game tree.
     */
    public void run() {
        root = new TreeNode(null, initialState, initialPlayer, round, null, this);
        iterationDepth = 1;
        while (true) {
            Double bestScore = pool.invoke(new AlphaBeta(root, iterationDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
            pool = new ForkJoinPool();
            System.err.println("Best score " + bestScore + " depth - " + iterationDepth);
            GameTree.prune = false;
            iterationDepth++;
            if (iterationDepth > (24 * 6)) break;
        }
        System.err.println("Game tree stopped.");
    }
    
    private Double alphaBeta(TreeNode node, int depth, Double alpha, Double beta) {
        if (prune) return null;
        if (depth == 0 || (ModelHelper.getWinningPlayers(node.getState(),
                                                         node.getPlayer(), graph, node.getRound()).size() != 0)) {
            return node.getScore();
        }
        boolean maximising = false;
        if (node.getPlayer().equals(Colour.Black)) maximising = true;
        if (node.getChildren().size() == 0) node = addChildren(node, maximising);
        if (maximising) {
            Double v = Double.NEGATIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                Double newValue = alphaBeta(child, depth - 1, alpha, beta);
                if (newValue == null) return null;
                if (newValue > v) {
                    v = newValue;
                    node.setBestChild(child);
                }
                if (v >= beta) break;
                alpha = Math.max(alpha, v);
            }
            return v;
        } else {
            Double v = Double.POSITIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                Double newValue = alphaBeta(child, depth - 1, alpha, beta);
                if (newValue == null) return null;
                if (newValue < v) {
                    v = newValue;
                    node.setBestChild(child);
                }
                
                if (v <= alpha) break;
                beta = Math.min(beta, v);
            }
            return v;
        }
    }
    
    // A class to perform the Alpha-Beta MiniMax algorithm.
    // When it gets to depth 1, it computes the score of the first child
    // node, if this doesn't cause a Beta cut-off, it calculates the scores
    // of all other children in parallel.
    class AlphaBeta extends RecursiveTask<Double> {
        
        private TreeNode node;
        private int depth;
        private Double alpha;
        private Double beta;
        
        public AlphaBeta(TreeNode node, int depth, Double alpha, Double beta) {
            this.node = node;
            this.depth = depth;
            this.alpha = alpha;
            this.beta = beta;
        }
        
        protected Double compute() {
            if (GameTree.prune) return null;
            if (depth == 0 || (ModelHelper.getWinningPlayers(node.getState(), 
                            node.getPlayer(), graph, node.getRound()).size() != 0)) {
                return node.getScore();
            }
            boolean maximising = false;
            if (node.getPlayer().equals(Colour.Black)) maximising = true;
            if (node.getChildren().size() == 0) node = addChildren(node, maximising);
            
            if (depth == 1) {
                if (maximising) {
                    return maxInParallel();
                } else {
                    return minInParallel();
                }
            } else {
                if (maximising) {
                    return max();
                } else {
                    return min();
                }
            }
        }
        
        private Double maxInParallel() {
            Double v = Double.NEGATIVE_INFINITY;
            List<TreeNode> children = node.getChildren();
            Double newValue = new AlphaBeta(children.get(0), depth - 1, alpha, beta).compute();
            if (newValue == null) return null;
            if (newValue > v) {
                v = newValue;
                node.setBestChild(children.get(0));
            }
            if (v >= beta) return v;
            alpha = Math.max(alpha, v);
            
            List<RecursiveTask<Double>> forks = forkTasks(children);
            for (int i = 1; i < children.size() - 1; i++) {
                Double forkValue = forks.get(i - 1).join();
                if (forkValue == null) return null;
                if (forkValue > v) {
                    v = forkValue;
                    node.setBestChild(children.get(i));
                }
                if (v >= beta) break;
                alpha = Math.max(alpha, v);
            }
            return v;
        }
        
        private Double minInParallel() {
            Double v = Double.POSITIVE_INFINITY;
            List<TreeNode> children = node.getChildren();
            Double newValue = new AlphaBeta(children.get(0), depth - 1, alpha, beta).compute();
            if (newValue == null) return null;
            if (newValue < v) {
                v = newValue;
                node.setBestChild(children.get(0));
            }   
            if (v <= alpha) return v;
            beta = Math.min(beta, v);
            
            List<RecursiveTask<Double>> forks = forkTasks(children);
            for (int i = 1; i < children.size() - 1; i++) {
                Double forkValue = forks.get(i - 1).join();
                if (forkValue == null) return null;
                if (forkValue < v) {
                    v = forkValue;
                    node.setBestChild(children.get(i));
                }
                if (v <= alpha) break;
                alpha = Math.min(beta, v);
            }
            return v;
        }
        
        private List<RecursiveTask<Double>> forkTasks(List<TreeNode> children) {
            List<RecursiveTask<Double>> forks = new ArrayList<RecursiveTask<Double>>();
            for (int i = 1; i < children.size() - 1; i++) {
                AlphaBeta alphaBeta = new AlphaBeta(children.get(1), depth - 1, alpha, beta);
                forks.add(alphaBeta);
                alphaBeta.fork();
            }
            return forks;
        }
        
        private Double max() {
            Double v = Double.NEGATIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                Double newValue = new AlphaBeta(child, depth - 1, alpha, beta).compute();
                if (newValue == null) return null;
                if (newValue > v) {
                    v = newValue;
                    node.setBestChild(child);
                }
                if (v >= beta) break;
                alpha = Math.max(alpha, v);
            }
            return v;
        }
        
        private Double min() {
            Double v = Double.POSITIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                Double newValue = new AlphaBeta(child, depth - 1, alpha, beta).compute();
                if (newValue == null) return null;
                if (newValue < v) {
                    v = newValue;
                    node.setBestChild(child);
                }
                
                if (v <= alpha) break;
                beta = Math.min(beta, v);
            }
            return v;
        }
        
    }
    
    // Adds all children to a specified node.
    // @param parent the node to add children to.
    // @param maximising the boolean which decides whether the new nodes are maximising.
    // @return the node with children added.
    private TreeNode addChildren(TreeNode parent, boolean maximising) {
        int nextRound = parent.getRound();
        if (maximising) nextRound++;
        Colour nextPlayer = ModelHelper.getNextPlayer(parent.getState(), ModelHelper.getPlayerOfColour(parent.getState(), parent.getPlayer())).colour();
        Set<Move> validMoves = ModelHelper.validMoves(ModelHelper.getPlayerOfColour(parent.getState(),
                                                      parent.getPlayer()), parent.getState(), graph);
        for (Move move : validMoves) {
            List<GamePlayer> clonedState = cloneList(parent.getState());
            playMove(clonedState, move);
            parent.addChild(new TreeNode(parent, clonedState, nextPlayer, nextRound, move, this));
        }
        return parent;
    }
    
    // Returns the List of Moves that the game tree has calculated.
    // @return the List of Moves that the game tree has calculated.
    private Move generateMove(int round, Colour colour) {
        TreeNode n = root.getBestChild();
        while (n != null) {
            if (n.getRound() == round && n.getPlayer().equals(colour)) return n.getMove();
            n = n.getBestChild();
        }
        return null;
    }
    
    // Plays the specified Move in the specified game state.
    // @param players the specified game state.
    // @param move the specified Move.
    private void playMove(List<GamePlayer> players, Move move) {
        if (move instanceof MoveTicket) playMove(players, (MoveTicket) move);
        else if (move instanceof MoveDouble) playMove(players, (MoveDouble) move);
    }
    
    // Plays the specified Move in the specified game state.
    // @param players the specified game state.
    // @param move the specified Move.
    private void playMove(List<GamePlayer> players, MoveTicket move) {
        GamePlayer player = ModelHelper.getPlayerOfColour(players, move.colour);
        player.setLocation(move.target);
        player.removeTicket(move.ticket);
    }
    
    // Plays the specified Move in the specified game state.
    // @param players the specified game state.
    // @param move the specified Move.
    private void playMove(List<GamePlayer> players, MoveDouble move) {
        playMove(players, move.move1);
        playMove(players, move.move2);
        GamePlayer player = ModelHelper.getPlayerOfColour(players, move.colour);
        player.removeTicket(Ticket.Double);
    }
    
    // Returns a List of GamePlayers that is an identical copy of the List passed in.
    // @param players the List of GamePlayers to clone.
    // @return a List of GamePlayers that is an identical copy of the List passed in.
    private List<GamePlayer> cloneList(List<GamePlayer> players) {
        List<GamePlayer> newPlayers = new ArrayList<GamePlayer>();
        for (GamePlayer player : players) {
            newPlayers.add(new GamePlayer(player));
        }
        return newPlayers;
    }
    
    // A class to help prune the GameTree when a Move is played.
    public static class GameTreeHelper implements Runnable {
        
        private final GameTree gameTree;
        private Move move = null;
        private ActionListener listener;
        
        /**
         * Constructs a new GameTreeHelper object.
         *
         * @param threadCom the ThreadCommunicator object to put the generated Moves onto.
         * @param gameTree the GameTree to whom this GameTreeHelper helps.
         */
        public GameTreeHelper(GameTree gameTree, ActionListener listener) {
            this.gameTree = gameTree;
            this.listener = listener;
        }
        
        /**
         * Stops the associated GameTree.
         */
        public void stop() {
            gameTree.stop();
        }
        
        /**
         * Sets the Move to be pruned.
         *
         * @param move the Move to be pruned.
         */
        public void setMove(Move move) {
            this.move = move;
        }

        /**
         * Returns the suggested Move selected by the game tree.
         *
         * @return the suggested Move selected by the game tree.
         */
        public Move getSuggestedMove(int round, Colour player) {
            return gameTree.generateMove(round, player);
        }
        
        /**
         * Prunes the GameTree associated with this helper.
         */
        public void run() {
            if (move != null) pruneTree(move);
            move = null;
        }
        
        // Returns true if pruning the GameTree is successful.
        // @param move the Move to be pruned.
        // @return true if pruning the GameTree is successful.
        private boolean pruneTree(Move move) {
            TreeNode root = gameTree.getRoot();
            if (root == null) return false;
            while (root != null) {
                for (TreeNode node : root.getChildren()) {
                    if (node.getMove().equals(move)) {
                        root.removeChildren();
                        root.addChild(node);
                        root.setBestChild(node);
                        gameTree.decrementIterationDepth();
                        GameTree.prune = true;
                        System.out.println("Pruned tree with - " + move);
                        return true;
                    }
                }
                root = root.getBestChild();
            }
            //Restart game tree
            gameTree.stop();
            if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "game_tree_crashed"));
            System.err.println("Failed to prune, stopping game tree.");
            return false;
        }
        
    }
    
}