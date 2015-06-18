package player;

import scotlandyard.*;
import client.algorithms.*;
import client.application.*;
import client.model.*;
import client.view.Formatter;
import client.view.*;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.Timer;
import java.awt.Color;

/**
 * A class that implements a game tree using Alpha-Beta pruning.
 */

public class GameTree implements Runnable {

    public final Graph<Integer, Route> graph;
    public final PageRank pageRank;
    public final Dijkstra dijkstra;
    public final ThreadCommunicator threadCom;
    private TreeNode root;
    private ScotlandYardGame game;

    private final Integer round;
    private final Colour initialPlayer;
    private final List<GamePlayer> initialState;
    private boolean paused = false;

    private Move mrXMove;
    private Move detMove;

    private boolean canFinish = false;

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
                    List<GamePlayer> initialState, ThreadCommunicator threadCom, ScotlandYardGame game) {
        this.graph = graph;
        this.pageRank = pageRank;
        this.dijkstra = dijkstra;
        this.round = round;
        this.initialPlayer = initialPlayer;
        this.initialState = initialState;
        this.threadCom = threadCom;
        this.mrXMove = MovePass.instance(Colour.Black);
        this.detMove = MovePass.instance(Colour.Blue);
        this.game = game;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    /**
     * Starts a new game tree.
     */
    public void run() {
        paused = false;
        root = new TreeNode(null, initialState, initialPlayer, round, null, this);
        threadCom.putUpdate("link_tree", this);
        threadCom.putUpdate("ai_set_rep", root);
        for (int i = 0; i < 6; i++) {
            Double result = alphaBeta(root, i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            game.setAiMove(getDetMove());
        }
        //Run detective game tree
        GamePlayer mrX = ModelHelper.getPlayerOfColour(initialState, Colour.Black);
        initialState.remove(mrX);
        Integer newLoc = game.mrXLocatation();
        if (newLoc != 0) mrX.setLocation(newLoc);

        initialState.add(mrX);
        TreeNode detRoot = new TreeNode(null, initialState, initialPlayer, round, null, this);
        Double result = alphaBeta(root, 2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        getMoves(root, detRoot);

        while (!canFinish) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }

        threadCom.putUpdate("show_route", new ArrayList<RouteHint>());
        threadCom.putUpdate("ai_end", true);
    }

    public void setCanFinish(boolean canFinish) {
        this.canFinish = canFinish;
    }

    private void getMoves(TreeNode node, TreeNode detNode) {
        if (node.getBestChild() != null && node.getBestChild().getMove() != null) {
            synchronized (mrXMove) {
                mrXMove = node.getBestChild().getMove();
            }
            if (detNode.getBestChild() != null && detNode.getBestChild().getMove() != null && detNode.getBestChild().getBestChild() != null && detNode.getBestChild().getBestChild().getMove() != null) {
                synchronized (detMove) {
                    detMove = detNode.getBestChild().getBestChild().getMove();
                }
            }
        }
    }

    private Double alphaBeta(TreeNode node, int depth, Double alpha, Double beta) {
        while(paused){
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
        if (ModelHelper.isGameOver(node.getState(), node.getPlayer(), graph, node.getRound())) {
            if (ModelHelper.getWinningPlayers(node.getState(), node.getPlayer(), graph, node.getRound()).contains(Colour.Black)) return Double.POSITIVE_INFINITY;
            else return Double.NEGATIVE_INFINITY;
        }
        if (depth == 0) return node.getScore();
        boolean maximising = false;
        if (node.getPlayer().equals(Colour.Black)) maximising = true;
        node = addChildren(node, maximising);
        if (maximising) {
            Double v = Double.NEGATIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                Double result = alphaBeta(child, depth - 1, alpha, beta);
                if (result > v) {
                    v = result;
                    node.setBestChild(child);
                }
                if (v >= beta) {
                    break;
                }
                alpha = Math.max(alpha, v);
            }
            return v;
        } else {
            Double v = Double.POSITIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                Double result = alphaBeta(child, depth - 1, alpha, beta);
                if (result < v) {
                    v = result;
                    node.setBestChild(child);
                }
                if (v <= alpha) {
                    break;
                }
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
        if (parent.getChildren().size() > 0) return parent;
        int nextRound = parent.getRound();
        if (maximising) nextRound++;
        Colour nextPlayer = ModelHelper.getNextPlayer(parent.getState(), ModelHelper.getPlayerOfColour(parent.getState(), parent.getPlayer())).colour();
        Set<Move> validMoves = ModelHelper.validMoves(ModelHelper.getPlayerOfColour(parent.getState(),
                                                      parent.getPlayer()), parent.getState(), graph, false);
        for (Move move : validMoves) {
            List<GamePlayer> clonedState = cloneList(parent.getState());
            playMove(clonedState, move);
            parent.addChild(new TreeNode(parent, clonedState, nextPlayer, nextRound, move, this));
        }
        return parent;
    }

    public Move getMrXMove() {
        return mrXMove;
    }

    public Move getDetMove() {
        return detMove;
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
}
