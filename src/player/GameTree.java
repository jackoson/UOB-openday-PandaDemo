package player;

import scotlandyard.*;
import client.algorithms.*;
import client.application.*;
import client.model.*;
import client.view.Formatter;
import client.view.GraphNodeRep;

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

    private final Integer round;
    private final Colour initialPlayer;
    private final List<GamePlayer> initialState;
    private boolean pause = false;

    private Move mrXMove;
    private Move detMove;
    private GraphNodeRep topRep;

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
                    List<GamePlayer> initialState, ThreadCommunicator threadCom) {
        this.graph = graph;
        this.pageRank = pageRank;
        this.dijkstra = dijkstra;
        this.round = round;
        this.initialPlayer = initialPlayer;
        this.initialState = initialState;
        this.threadCom = threadCom;
        this.mrXMove = MovePass.instance(Colour.Black);
        this.detMove = MovePass.instance(Colour.Blue);
    }

    /**
     * Starts a new game tree.
     */
    public void run() {
        root = new TreeNode(null, initialState, initialPlayer, round, null, this);
        topRep = new GraphNodeRep(Formatter.colorForPlayer(initialPlayer), root.getTrueLocation());
        threadCom.putUpdate("link_tree", this);
        threadCom.putUpdate("ai_set_rep", root);
        for (int i = 0; i < 5; i++) {
            Double result = alphaBeta(root, i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, topRep);
            System.out.println("Depth: " + i + " Score: " + result);
            getMoves(root);
        }
        threadCom.putUpdate("ai_end", true);
    }

    private void getMoves(TreeNode node) {
        if (node.getBestChild() != null && node.getBestChild().getMove() != null) {
            synchronized (mrXMove) {
                mrXMove = node.getBestChild().getMove();
            }
            if (node.getBestChild().getBestChild() != null && node.getBestChild().getBestChild().getMove() != null) {
                synchronized (detMove) {
                    detMove = node.getBestChild().getBestChild().getMove();
                }
            }
        }
    }

    public void pause() {
        pause = true;
    }

    public void resume() {
        pause = false;
    }

    private Double alphaBeta(TreeNode node, int depth, Double alpha, Double beta, GraphNodeRep graphNode) {
        checkPause();
        if (depth == 0) return node.getScore();
        boolean maximising = false;
        if (node.getPlayer().equals(Colour.Black)) maximising = true;
        node = addChildren(node, maximising);
        if (maximising) {
            Double v = Double.NEGATIVE_INFINITY;
            for (TreeNode child : node.getChildren()) {
                GraphNodeRep newGraphNode = new GraphNodeRep(Formatter.colorForPlayer(node.getPlayer()), node.getTrueLocation());
                synchronized (graphNode) {
                    graphNode.addChild(newGraphNode);
                }
                Double result = alphaBeta(child, depth - 1, alpha, beta, newGraphNode);
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
                GraphNodeRep newGraphNode = new GraphNodeRep(Formatter.colorForPlayer(node.getPlayer()), node.getTrueLocation());
                synchronized (graphNode) {
                    graphNode.addChild(newGraphNode);
                }
                Double result = alphaBeta(child, depth - 1, alpha, beta, newGraphNode);
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

    private void checkPause() {
        while (pause) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.err.println("GameTree pause interrupted.");
                e.printStackTrace();
            }
        }
    }

    public Integer randomNode() {
        if (root == null) return -1;
        TreeNode n = root;
        Random r = new Random();
        while (r.nextInt(3) < 1 && n.getChildren().size() > 0) {
            List<TreeNode> children = n.getChildren();
            int c = children.size();
            n = children.get(r.nextInt(c));
        }
        return n.getTrueLocation();
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
