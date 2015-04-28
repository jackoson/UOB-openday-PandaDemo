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
    
<<<<<<< HEAD
    public List<Move> createMoves() {
        List<Move> moves = new ArrayList<Move>();
        TreeNode n = root;
        n = n.bestChild;
        while (n != null) {
            moves.add(n.getMove());
            n = n.bestChild;
        }
        return moves;
    }
    
    public void printTree(TreeNode n, int d) {
        if (n != null) {
        System.err.println("N: " + d  + ", " + n.getMove());
        for (TreeNode c : n.children) {
            System.err.println(n.children.size());
            printTree(c, d + 1);
        }
        }
    }
    
    public boolean pruneTree(Move move) {
        if (tree.root == null) return false;
        printTree(tree.root, 0);
        //System.err.println("Not null" + move);
        if (move.colour.equals(Colour.Black)) {
            tree.round++;//?
            
        }
        for (TreeNode n : tree.root.children) {
            //System.err.println("ChildreCn:" + n.getMove() + " Target:" + move);
        }
        for (TreeNode n : tree.root.children) {
            //System.err.println("Children:" + n.move + " Target:" + move);
            if (n.getMove().equals(move)) {
                n.parent = null;
                tree.root = n;
                for (TreeNode m : tree.root.children) {
                    //System.err.println("ChildrenA:" + m.getMove() + " Target:" + move);
                }
                //System.err.println("NEW ROOOOOOOOOT:" + tree.root.getMove());
                //System.err.println("current PLayer:" + tree.currentPlayer + "New Player: " + ModelHelper.getNextPlayer(tree.players, ModelHelper.getPlayerOfColour(tree.players, tree.currentPlayer)).colour());
                tree.currentPlayer = move.colour;
                tree.players = n.players;
                tree.iterationDepth--;
                //System.err.println("I! " + tree.iterationDepth);
                //System.err.println("AAcurrent PLayer:" + tree.currentPlayer + "New Player: " + ModelHelper.getNextPlayer(tree.players, ModelHelper.getPlayerOfColour(tree.players, tree.currentPlayer)).colour());
                printTree(tree.root, 0);
                return true;
            }
        }
        
        for (TreeNode n : tree.root.children) {
            //System.err.println("ChildrenB:" + n.getMove() + " Target:" + move);
        }
=======
    public boolean pruneTree(Move move) {
>>>>>>> 632ef06c2f5a9ceac0966d96f48ec980e1976b7d
        return false;
    }
    
    public boolean connectedToTree(TreeNode node) {
        while (node != null) {
            if (node.equals(root)) return true;
            node = node.getParent();
        }
        return false;
    }
<<<<<<< HEAD

    private double alphaBeta(TreeNode node, int round, Colour currentPlayerColour, List<GamePlayer> currentState, int depth, double alpha, double beta) throws Exception {
        //System.err.println("Currentplayer:" + currentPlayerColour + "Depth: " + depth);
        if (!conectedToTree(node)) {throw new Exception();}
        
        GamePlayer currentPlayer = ModelHelper.getPlayerOfColour(currentState, currentPlayerColour);
        if (depth == 0 || (ModelHelper.getWinningPlayers(currentState, currentPlayer, graph, rounds, round).size() > 0)) {
            double score = node.score(currentPlayer, round);
            return score;
        }
        List<TreeNode> children = node.children;
        boolean maximising = false;
        if (currentPlayer.colour().equals(Colour.Black)) maximising = true;
        if (children == null) {
            children = new ArrayList<TreeNode>();
            //Create new layer
            Set<Move> validMoves = ModelHelper.validMoves(currentPlayer, currentState, graph);
            //System.err.println("Parent: " + currentPlayerColour + "First Move: " + validMoves.iterator().next());
            for (Move move : validMoves) {
                List<GamePlayer> clonedPlayers = cloneList(currentState);
                playMove(clonedPlayers, move);
                TreeNode newNode = new TreeNode(clonedPlayers, move, node);
                children.add(newNode);
            }
            //Set children
            node.setChildren(children);
=======
    
    private double alphaBeta(TreeNode node, int depth, Double alpha, Double beta) {
        /** Check if node is in tree, if not return the worst case for this node. **/
        if (!connectedToTree(node)) {
            if (node.getPlayer().equals(Colour.Black)) return Double.NEGATIVE_INFINITY;
            else return Double.POSITIVE_INFINITY;
        }
        if (depth == 0 || (ModelHelper.getWinningPlayers(node.getState(), 
                            node.getPlayer(), graph, node.getRound()).size() != 0)) {
            return node.getScore();
>>>>>>> 632ef06c2f5a9ceac0966d96f48ec980e1976b7d
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
    
<<<<<<< HEAD
    private GamePlayer getPlayer(List<GamePlayer> players, Colour colour) {
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer.colour().equals(colour)) return gamePlayer;
        }
        return null;
    }
    
    private class TreeNode {
        
        public final List<GamePlayer> players;
        public static final double kMultiplier = 1.0;
        public static final double kMax = 10.0;
        public static final double kMin = -10.0;
        public List<TreeNode> children;
        public TreeNode parent;
        public Double score;
        private boolean best = false;
        public TreeNode bestChild = null;
        private Move move;
        
        public TreeNode(List<GamePlayer> players, Move move, TreeNode parent) {
            this.players = players;
            this.move = move;
            this.children = null;
            this.parent = parent;
        }
        
        public void setChildren(List<TreeNode> children) {
            this.children = children;
        }
        
        public double getScore(GamePlayer currentPlayer, int round) {
            if (score == null) score = score(currentPlayer, round);
            return score;
        }
        
        public Move getMove() {
            return move;
        }
        
        public void setBest(boolean best) {
            this.best = best;
        }
        
        public boolean getBest() {
            return best;
        }
        
        private double score(GamePlayer currentPlayer, int round) {
            Set<Colour> winningPlayers = ModelHelper.getWinningPlayers(players, currentPlayer, graph, rounds, round);
            if (winningPlayers.contains(Colour.Black)) return TreeNode.kMax;
            if (winningPlayers.size() != 0) return TreeNode.kMin;
            int mrXLocation = players.get(0).location();
            if (mrXLocation == 0) mrXLocation = 1; //?NEED TO FIND A BETTER SOLUTION TO THIS
            //?System.err.println("Loc" + mrXLocation);
            double mrXPageRank = pageRank.getPageRank(mrXLocation);//? NOT WORKING
            //?System.err.println("Rank:" + mrXPageRank);
            double sumDetPageRank = 0.0;
            double sumDetDistance = 0.0;
            
            for (int i = 1; i < players.size(); i++) {
                GamePlayer player = players.get(i);
                int detLocation = player.location();
                sumDetPageRank += pageRank.getPageRank(detLocation);
                int detDistance = routeFinder.getRoute(detLocation, mrXLocation, convertDetTickets(player.tickets())).size();
                if (detDistance == 1) return TreeNode.kMin;
                sumDetDistance += (double) detDistance;
            }
            double avgDetDistance = sumDetDistance / (double) (players.size() - 1);
            double avgDetPageRank = sumDetPageRank / (double) (players.size() - 1);
            avgDetPageRank = avgDetPageRank * (Math.pow(avgDetDistance, 2) / 50);
            mrXPageRank = mrXPageRank * (Math.pow(avgDetDistance, 2) / 100);
            double score = avgDetDistance;//?add in pagerank
            //System.err.println("avgDist" + avgDetDistance + "num" + (players.size()));
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
    
=======
>>>>>>> 632ef06c2f5a9ceac0966d96f48ec980e1976b7d
}