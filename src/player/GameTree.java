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
  
    public final Graph<Integer, Route> graph;
    public final PageRank pageRank;
    public final Dijkstra dijkstra;
    
    private final Integer round;
    private final Colour initialPlayer;
    private final List<GamePlayer> initialState;
    
    public boolean iterate = true;
    private TreeNode oldRoot;
    private TreeNode root;
    private int iterationDepth;
    private boolean prune = false;
    
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
                    List<GamePlayer> initialState) {
        if (helper != null) helper.stop();
        GameTree gameTree = new GameTree(graph, pageRank, dijkstra,
                                          round, initialPlayer, initialState);
        new Thread(gameTree).start();
        helper = new GameTreeHelper(threadCom, gameTree);
        return helper;
    }
    
    /**
     * Sets the prune boolean which tells the alpha-beta function to exit.
     *
     * @param prune the boolean which tells the alpha-beta function to exit.
     */
    public void setPrune(boolean prune) {
        this.prune = prune;
    }
    
    /**
     * Stops the game tree.
     */
    public void stop() {
        iterate = false;
        prune = true;
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
     * Sets the tree root.
     *
     * @param the tree root.
     */
    public void setRoot(TreeNode root) {
        this.root = root;
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
     * Resets the iterationDepth variable.
     */
    public void resetIterationDepth() {
        iterationDepth = 0;
    }
    
    
    /**
     * Starts a new game tree.
     */
    public void run() {
        root = new TreeNode(null, initialState, initialPlayer, round, null, this);
        iterationDepth = 1;
        while (iterate) {
            oldRoot = root;
            Double bestScore = alphaBeta(root, iterationDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (bestScore != null) helper.setMoves(generateMoves());
            prune = false;
            iterationDepth++;
            if (root.getRound() > 24) break;
        }
        System.err.println("Game tree stopped.");
    }

    // Performs the alpha-beta algorithm to identify the node with the best score
    // @param node the current node the algorithm is searching.
    // @param depth the current depth the algorithm is searching.
    // @param alpha the current alpha value.
    // @param beta the current beta value.
    // @return the best score from the game tree.
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
            if (node.equals(root)) System.err.println("Best score " + bestScore + " root player - " + node.getPlayer());
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
            if (node.equals(root)) System.err.println("Best score " + bestScore + " root player - " + node.getPlayer());
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
    private List<Move> generateMoves() {
        List<Move> moves = new ArrayList<Move>();
        TreeNode n = root.getBestChild();
        while (n != null) {
            moves.add(n.getMove());
            n = n.getBestChild();
        }
        return moves;
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
    public static class GameTreeHelper implements ActionListener, Runnable {
        
        private final Timer timer;
        private final ThreadCommunicator threadCom;
        private final GameTree gameTree;
        public List<Move> moves;
        private Move move = null;
        private ActionListener listener;
        
        private final int kTurnTime = 10000;
        
        /**
         * Constructs a new GameTreeHelper object.
         *
         * @param threadCom the ThreadCommunicator object to put the generated Moves onto.
         * @param gameTree the GameTree to whom this GameTreeHelper helps.
         */
        public GameTreeHelper(ThreadCommunicator threadCom, GameTree gameTree) {
            this.threadCom = threadCom;
            this.gameTree = gameTree;
            this.timer = new Timer(kTurnTime, this);
        }
        
        /**
         * Stops the associated GameTree.
         */
        public void stop() {
            gameTree.stop();
        }
        
        /**
         * Starts the Timer for kTurnTime.
         *
         * @param listener the ActionListener to notify if the GameTree crashes.
         */
        public void startTimer(ActionListener listener) {
            this.listener = listener;
            timer.restart();
        }
        
        /**
         * Called when the Timer runs out.
         *
         * @param e the ActionEvent containing the information about the 
         * object which called this listener.
         */
        public void actionPerformed(ActionEvent e) {
            timer.stop();
            threadCom.putEvent("calculated_moves", moves);
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
         * Sets the List of Moves generated by the GameTree.
         *
         * @param moves the List of Moves generated by the GameTree.
         */
        public void setMoves(List<Move> moves) {
            this.moves = moves;
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
            if (gameTree.getRoot() == null) return false;
            for (TreeNode node : gameTree.root.getChildren()) {
                if (node.getMove().equals(move)) {
                    node.setParent(null);
                    gameTree.setRoot(node);
                    gameTree.resetIterationDepth();
                    gameTree.setPrune(true);
                    return true;
                }
            }
            //Restart game tree
            gameTree.stop();
            if (listener != null) listener.actionPerformed(new ActionEvent(this, 0, "game_tree_crashed"));
            System.err.println("Failed to prune, stopping game tree.");
            return false;
        }
        
    }
    
}