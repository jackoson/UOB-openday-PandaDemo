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
    
    private ThreadCommunicator threadCom;
    private Graph<Integer, Route> graph;
    private PageRank pageRank;
    private Dijkstra routeFinder;
    
    private List<Boolean> rounds;
    private Integer round;
    private GamePlayer currentPlayer;
    private List<GamePlayer> players;
    public static final int kMaxDepth = 6;
    private int depth;
    public List<Move> moves;
    public boolean iterate;
    private GameTree tree;
    private Timer timer;
    private TreeNode root;
    /*
    public static void main(String[] args) {
        List<GamePlayer> players = new ArrayList<GamePlayer>();
        Colour[] colours = Colour.values();
        
        Map<Ticket, Integer> mrXTickets = new HashMap<Ticket, Integer>();
        mrXTickets.put(Ticket.Taxi, 10);
        mrXTickets.put(Ticket.Bus, 10);
        mrXTickets.put(Ticket.Underground, 10);
        mrXTickets.put(Ticket.Double, 2);
        mrXTickets.put(Ticket.Secret, 5);
        players.add(new GamePlayer(null, colours[0], 115, mrXTickets));
        
        Map<Ticket, Integer> blueDetTickets = new HashMap<Ticket, Integer>();
        blueDetTickets.put(Ticket.Taxi, 11);
        blueDetTickets.put(Ticket.Bus, 8);
        blueDetTickets.put(Ticket.Underground, 4);
        blueDetTickets.put(Ticket.Double, 0);
        blueDetTickets.put(Ticket.Secret, 0);
        players.add(new GamePlayer(null, colours[1], 102, blueDetTickets));
        
        Map<Ticket, Integer> greenDetTickets = new HashMap<Ticket, Integer>();
        greenDetTickets.put(Ticket.Taxi, 11);
        greenDetTickets.put(Ticket.Bus, 8);
        greenDetTickets.put(Ticket.Underground, 4);
        greenDetTickets.put(Ticket.Double, 0);
        greenDetTickets.put(Ticket.Secret, 0);
        players.add(new GamePlayer(null, colours[2], 114, greenDetTickets));
        
        Map<Ticket, Integer> redDetTickets = new HashMap<Ticket, Integer>();
        redDetTickets.put(Ticket.Taxi, 11);
        redDetTickets.put(Ticket.Bus, 8);
        redDetTickets.put(Ticket.Underground, 4);
        redDetTickets.put(Ticket.Double, 0);
        redDetTickets.put(Ticket.Secret, 0);
        players.add(new GamePlayer(null, colours[3], 126, redDetTickets));
        
        Map<Ticket, Integer> yellowDetTickets = new HashMap<Ticket, Integer>();
        yellowDetTickets.put(Ticket.Taxi, 11);
        yellowDetTickets.put(Ticket.Bus, 8);
        yellowDetTickets.put(Ticket.Underground, 4);
        yellowDetTickets.put(Ticket.Double, 0);
        yellowDetTickets.put(Ticket.Secret, 0);
        players.add(new GamePlayer(null, colours[4], 133, yellowDetTickets));
        
        List<Boolean> rounds = Arrays.asList(false, false, false, true, false,
                                             false, false, false, true, false,
                                             false, false, false, true, false,
                                             false, false, false, true, false,
                                             false, false, false, false, true);
        
        try {
            ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
            Graph<Integer, Route> testGraph = graphReader.readGraph("graph.txt");
            Dijkstra dijkstra = new Dijkstra("graph.txt");
            PageRank testPageRank = new PageRank(testGraph);
            testPageRank.iterate(100);
            GameTree gameTree = new GameTree(testGraph, testPageRank, dijkstra, players, rounds, 0, players.get(0));
            //gameTree.calculateTree();
        } catch (Exception e) {
            System.err.println("Error running alpha-beta pruning test :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
    */
    public GameTree(ThreadCommunicator threadCom, Graph<Integer, Route> graph, PageRank pageRank, Dijkstra routeFinder, List<GamePlayer> players, List<Boolean> rounds, Integer round, GamePlayer currentPlayer) {
        this.threadCom = threadCom;
        this.graph = graph;
        this.pageRank = pageRank;
        this.routeFinder = routeFinder;
        this.players = players;
        this.currentPlayer = currentPlayer;
        this.rounds = rounds;
        this.round = round;
    }
    
    public GameTree() {
        //To run a game tree
        
    }
    
    public void calculateTree(ThreadCommunicator threadCom, Graph<Integer, Route> graph, PageRank pageRank, Dijkstra routeFinder, List<GamePlayer> players, List<Boolean> rounds, Integer round, GamePlayer currentPlayer) {
        this.threadCom = threadCom;
        tree = new GameTree(threadCom, graph, pageRank, routeFinder, players, rounds, round, currentPlayer);
        new Thread(tree).start();
        System.err.println("Timer started");
        timer = new Timer(13000, this);
        timer.start();
    }
    
    public void actionPerformed(ActionEvent e) {
        timer.stop();
        moves = tree.moves;
        tree.iterate = false;
        threadCom.putEvent("calculated_moves", moves);
    }
    
    public void run() {
        root = new TreeNode(players, null);
        iterate = true;//need to stop alphabeta when iterate cahnges
        moves = new ArrayList<Move>();
        
        int i = 1;
        while (iterate) {
            double bestScore = alphaBeta(root, round, currentPlayer.colour(), players, i, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            moves = createMoves();
            i++;
        }
    }
    
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

    private double alphaBeta(TreeNode node, int round, Colour currentPlayerColour, List<GamePlayer> currentState, int depth, double alpha, double beta) {
        GamePlayer currentPlayer = ModelHelper.getPlayerOfColour(currentState, currentPlayerColour);
        if (depth == 0 || (ModelHelper.getWinningPlayers(currentState, currentPlayer, graph, rounds, round).size() > 0)) {
            double score = node.score(currentPlayer, round);
            return score;
        }
        List<TreeNode> children = new ArrayList<TreeNode>();
        
        boolean maximising = false;
        if (currentPlayer.colour().equals(Colour.Black)) maximising = true;
        if (depth == 1) {
            //Create new layer
            Set<Move> validMoves = ModelHelper.validMoves(currentPlayer, currentState, graph);           
            for (Move move : validMoves) {
                List<GamePlayer> clonedPlayers = cloneList(currentState);
                playMove(clonedPlayers, move);
                TreeNode newNode = new TreeNode(clonedPlayers, move);
                node.addChild(newNode);
            }
        }
        // Advance the game.  
        if (maximising) round++;
        currentPlayer = ModelHelper.getNextPlayer(currentState, currentPlayer);
        //Get children
        children = node.children;
        if (maximising) {
            // We are on a maximising node.
            Double v = Double.NEGATIVE_INFINITY;
            for (TreeNode child : children) {
                double newValue = alphaBeta(child, round, currentPlayer.colour(), child.players, depth - 1, alpha, beta);
                if (newValue > v) {
                    v = newValue;
                    node.bestChild = child;
                }
                //System.out.println("max: Depth = " + depth + " v = " + v + " alpha = " + alpha + " beta = " + beta);
                if (v >= beta) break;
                alpha = Math.max(alpha, v);
            }
            return v;
        } else {
            // We are on a minimising node.
            Double v = Double.POSITIVE_INFINITY;
            for (TreeNode child : children) {
                double newValue = alphaBeta(child, round, currentPlayer.colour(), child.players, depth - 1, alpha, beta);
                if (newValue < v) {
                    v = newValue;
                    node.bestChild = child;
                }
                //System.out.println("min: Depth = " + depth + " v = " + v + " alpha = " + alpha + " beta = " + beta);
                if (v <= alpha) break;
                beta = Math.min(beta, v);
            }
            return v;
        }
    }
    
    public List<Move> findRoute(TreeNode root) {
        if (root.children.size() == 0) {
            List<Move> move = new ArrayList<Move>();
            move.add(root.getMove());
            return move;
        }
        for (TreeNode child : root.children) {
            if (child.getBest()) {
                List<Move> move = findRoute(child);
                List<Move> moves = new ArrayList<Move>();
                moves.add(child.getMove());
                moves.addAll(move);
                return moves;
            }
        }
        return new ArrayList<Move>();
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
        
        public final List<GamePlayer> players;
        public static final double kMultiplier = 1.0;
        public static final double kMax = 10.0;
        public static final double kMin = -10.0;
        public List<TreeNode> children;
        public Double score;
        private boolean best = false;
        public TreeNode bestChild = null;
        private Move move;
        
        public TreeNode(List<GamePlayer> players, Move move) {
            this.players = players;
            this.move = move;
            this.children = new ArrayList<TreeNode>();
        }
        
        public void addChild(TreeNode child) {
            children.add(child);
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
            if (mrXLocation == 0) mrXLocation = 1; //NEED TO FIND A BETTER SOLUTION TO THIS
            double mrXPageRank = pageRank.getPageRank(mrXLocation);
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