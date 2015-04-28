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

public class GameTree implements Runnable, ActionListener {
  
    private final ThreadCommunicator threadCom;
    public final Graph<Integer, Route> graph;
    public final PageRank pageRank;
    public final Dijkstra dijkstra;
    private final Timer timer;
    
    private final Integer round;
    private final Colour initialPlayer;
    private final List<GamePlayer> initialState;
    
    private List<Move> generatedMoves;
    private TreeNode root;
    
    private final int kTurnTime = 13000;
    
    public GameTree(ThreadCommunicator threadCom, Graph<Integer, Route> graph, 
                    PageRank pageRank, Dijkstra dijkstra, int round, Colour initialPlayer,
                    List<GamePlayer> initialState) {
        this.threadCom = threadCom;
        this.graph = graph;
        this.pageRank = pageRank;
        this.dijkstra = dijkstra;
        this.round = round;
        this.initialPlayer = initialPlayer;
        this.initialState = initialState;
        this.timer = new Timer(kTurnTime, this);
        this.generatedMoves = new ArrayList<Move>();
    }
    
    public static GameTree startTree(ThreadCommunicator threadCom, Graph<Integer, Route> graph, 
                    PageRank pageRank, Dijkstra dijkstra, int round, Colour initialPlayer,
                    List<GamePlayer> initialState) {
        GameTree gameTree = new GameTree(threadCom, graph, pageRank, dijkstra,
                                          round, initialPlayer, initialState);
        new Thread(gameTree).start();
        return gameTree;
    }
    
    public void run() {
        root = new TreeNode(null, initialState, initialPlayer, round, null, this);
        int iterationDepth = 1;
        while (true) {
            double bestScore = alphaBeta(root, iterationDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            generatedMoves = generateMoves();
            iterationDepth++;
        }
    }
    
    public boolean pruneTree(Move move) {
        return false;
    }
    
    public boolean connectedToTree(TreeNode node) {
        while (node != null) {
            if (node.equals(root)) return true;
            node = node.getParent();
        }
        return false;
    }
    
    private double alphaBeta(TreeNode node, int depth, Double alpha, Double beta) {
        /** Check if node is in tree, if not return the worst case for this node. **/
        if (!connectedToTree(node)) {
            if (node.getPlayer().equals(Colour.Black)) return Double.NEGATIVE_INFINITY;
            else return Double.POSITIVE_INFINITY;
        }
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
                double newValue = alphaBeta(child, depth - 1, alpha, beta);
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
                double newValue = alphaBeta(child, depth - 1, alpha, beta);
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
    
    private TreeNode addChildren(TreeNode root, boolean maximising) {
        int nextRound = root.getRound();
        if (maximising) nextRound++;
        Colour nextPlayer = ModelHelper.getNextPlayer(root.getState(), ModelHelper.getPlayerOfColour(root.getState(), root.getPlayer())).colour();
        Set<Move> validMoves = ModelHelper.validMoves(ModelHelper.getPlayerOfColour(root.getState(), 
                                                      root.getPlayer()), root.getState(), graph);
        for (Move move : validMoves) {
            List<GamePlayer> clonedState = cloneList(root.getState());
            playMove(clonedState, move);
            root.addChild(new TreeNode(root, clonedState, nextPlayer, nextRound, move, this));
        }
        return root;
    }
    
    private List<Move> generateMoves() {
        List<Move> moves = new ArrayList<Move>();
        TreeNode n = root.getBestChild();
        while (n != null) {
            moves.add(n.getMove());
            n = n.getBestChild();
        }
        return moves;
    }
    
    public void startTimer() {
        timer.restart();
    }
    
    public void actionPerformed(ActionEvent e) {
        timer.stop();
        threadCom.putEvent("calculated_moves", generatedMoves);
    }
    
    private void playMove(List<GamePlayer> players, Move move) {
        if (move instanceof MoveTicket) playMove(players, (MoveTicket) move);
        else if (move instanceof MoveDouble) playMove(players, (MoveDouble) move);
    }
    
    private void playMove(List<GamePlayer> players, MoveTicket move) {
        GamePlayer player = ModelHelper.getPlayerOfColour(players, move.colour);
        player.setLocation(move.target);
        player.removeTicket(move.ticket);
    }
    
    private void playMove(List<GamePlayer> players, MoveDouble move) {
        playMove(players, move.move1);
        playMove(players, move.move2);
        GamePlayer player = ModelHelper.getPlayerOfColour(players, move.colour);
        player.removeTicket(Ticket.Double);
    }
    
    private List<GamePlayer> cloneList(List<GamePlayer> players) {
        List<GamePlayer> newPlayers = new ArrayList<GamePlayer>();
        for (GamePlayer player : players) {
            newPlayers.add(new GamePlayer(player));
        }
        return newPlayers;
    }
    
}