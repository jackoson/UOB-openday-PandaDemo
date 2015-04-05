package client.model;

import client.scotlandyard.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * A class to handle all of the game logic.
 */

public class ScotlandYardModel extends ScotlandYard {
    
    private int numberOfPlayers;
    private List<GamePlayer> players;
    private List<Spectator> spectators;
    private List<Boolean> rounds;
    private int roundCounter;
    private Graph<Integer, Route> graph;
    private GamePlayer currentPlayer;
    private int mrXLocation;

    /**
     * Constructs a new ScotlandYardModel object.
     *
     * @param numberOfDetectives the number of detectives.
     * @param rounds the List of booleans denoting when Mr X is visible.
     * @param graphFileName the path to the file containing the graph data.
     * @throws IOException throws when it can't read the graph file.
     */
    public ScotlandYardModel(int numberOfDetectives, List<Boolean> rounds, String graphFileName) throws IOException {
        super(numberOfDetectives, rounds, graphFileName);
        ScotlandYardGraphReader graphReader = new ScotlandYardGraphReader();
        this.graph = graphReader.readGraph(graphFileName);
        this.numberOfPlayers = numberOfDetectives + 1;
        this.players = new ArrayList<GamePlayer>();
        this.spectators = new ArrayList<Spectator>();
        this.mrXLocation = 0;
        this.roundCounter = 0;
        this.rounds = rounds;
    }

    //Create a list of valid moves for a player of colour colour
    @Override
    protected Move getPlayerMove(Colour colour) {
        List<Move> moves = validMoves(colour);
        GamePlayer gamePlayer = getPlayer(colour);
        int playerLocation = gamePlayer.location();
        
        Move chosenMove = gamePlayer.player().notify(playerLocation, moves);
        if (moves.contains(chosenMove)) {
            return chosenMove;
        } else {
            return null;
        }
    }

    //Get the next player in the list of players
    @Override
    protected void nextPlayer() {
        int currentPosition = players.indexOf(currentPlayer);
        currentPlayer = players.get((currentPosition + 1) % players.size());
    }

    //Play a MoveTicket
    @Override
    protected void play(MoveTicket move) {
        GamePlayer gamePlayer = getPlayer(move.colour);
        gamePlayer.setLocation(move.target);
        gamePlayer.removeTicket(move.ticket);
        //Give player ticket to MrX
        MoveTicket newMove = move;
        if (gamePlayer.colour().equals(Colour.Black)) {
            incCounter(gamePlayer);
            newMove = new MoveTicket(move.colour, mrXLocation, move.ticket);
        } else {
            GamePlayer mrX = getPlayer(Colour.Black);
            mrX.addTicket(move.ticket);
        }
        notifySpectators(newMove);
    }

    //Play a MoveDouble
    @Override
    protected void play(MoveDouble move) {
        List<Move> moves = move.moves;
        notifySpectators(move);
        for (Move singleMove : moves) {
            play(singleMove);
        }
        GamePlayer gamePlayer = getPlayer(move.colour);
        gamePlayer.removeTicket(Ticket.DoubleMove);
    }

    //Play a MovePass
    @Override
    protected void play(MovePass move) {
        GamePlayer gamePlayer = getPlayer(move.colour);
        incCounter(gamePlayer);
        notifySpectators(move);
    }

    //Increment roundCounter and update MrX's location
    private void incCounter(GamePlayer player) {
        roundCounter++;
        if (rounds.get(roundCounter)) mrXLocation = player.location();
    }

    //Create a list of valid moves
    @Override
    protected List<Move> validMoves(Colour player) {
        GamePlayer gamePlayer = getPlayer(player);
        int secretMoveCount = getPlayerTickets(player, Ticket.SecretMove);
        int doubleMoveCount = getPlayerTickets(player, Ticket.DoubleMove);
        Node<Integer> currentPosition = getNode(gamePlayer.location());
        List<Move> allMoves = new ArrayList<Move>();
        List<MoveTicket> singleMoves = createSingleMoves(player, currentPosition.data());
        allMoves.addAll(singleMoves);
        
        if (player.equals(Colour.Black)) {
            List<MoveTicket> secretMoves = new ArrayList<MoveTicket>();
            List<MoveDouble> doubleMoves = new ArrayList<MoveDouble>();
            if (secretMoveCount >= 1) secretMoves = createSingleSecretMoves(singleMoves);
            if (doubleMoveCount >= 1) doubleMoves = createDoubleMoves(singleMoves, secretMoveCount);
            
            allMoves.addAll(secretMoves);
            allMoves.addAll(doubleMoves);
        } else if (allMoves.size() == 0) {
            allMoves.add(new MovePass(player));
        }
        return allMoves;
    }
    
    //Create a list of valid single moves
    private List<MoveTicket> createSingleMoves(Colour player, Integer location) {
        List<MoveTicket> moves = new ArrayList<MoveTicket>();
        List<Edge<Integer, Route>> edges = graph.getEdges(location);
        
        for (Edge<Integer, Route> edge : edges) {
            Node<Integer> node = getNode(edge.source());
            if (node.data().equals(location)) node = getNode(edge.target());
            
            Ticket ticket = Ticket.fromRoute(edge.data());
            MoveTicket newMove = new MoveTicket(player, (int) node.data(), ticket);
            if (!nodeOccupied(node) && hasTickets(newMove, null)) {
                moves.add(newMove);
            }
        }
        return moves;
    }
    
    //Create a list of valid double moves including secret moves
    private List<MoveDouble> createDoubleMoves(List<MoveTicket> moves, int secretMoveCount) {
        List<MoveDouble> doubleMoves = new ArrayList<MoveDouble>();
        for (MoveTicket move : moves) {
            List<MoveTicket> secondMoves = createSingleMoves (move.colour, move.target);
            
            for (MoveTicket secondMove : secondMoves) {
                
                if (hasTickets(move, secondMove)) doubleMoves.add(new MoveDouble(move.colour, move, secondMove));
                if (secretMoveCount >= 1) {
                    if (hasTickets(secondMove,null)) doubleMoves.add(new MoveDouble(move.colour, makeSecret(move), secondMove));
                    if (hasTickets(move,null)) doubleMoves.add(new MoveDouble(move.colour, move, makeSecret(secondMove)));
                    
                    if (secretMoveCount >= 2) doubleMoves.add(new MoveDouble(move.colour, makeSecret(move), makeSecret(secondMove)));
                }
            }
        }

        return doubleMoves;
    }
    
    //Create a list of secret single moves
    private List<MoveTicket> createSingleSecretMoves(List<MoveTicket> moves) {
        List<MoveTicket> secretMoves = new ArrayList<MoveTicket>();
        for (MoveTicket move : moves) {
            secretMoves.add(makeSecret(move));
        }
        return secretMoves;
    }
    private MoveTicket makeSecret(MoveTicket move) {
        return new MoveTicket(move.colour, move.target, Ticket.SecretMove);
    }
    
    //Check to see if player has required moves
    private boolean hasTickets(MoveTicket move1, MoveTicket move2) {
        Ticket ticket1;
        if (move1 != null){
            Colour player = move1.colour;
            ticket1 = move1.ticket;
            if (getPlayerTickets(player, ticket1) < 1) return false;
        } else {
            return false;
        }
        if (move2 != null){
            Colour player = move2.colour;
            Ticket ticket = move2.ticket;
            int num = 1;
            if (ticket.equals(ticket1)) num = 2;
            if (getPlayerTickets(player, ticket) < num) return false;
        }
        return true;
    }
    
    //Checks whether a detective occupies a given node
    private boolean nodeOccupied(Node<Integer> node) {
        for (GamePlayer player : players) {
            if (node.data().equals(player.location())) {
                if (!player.colour().equals(Colour.Black)) return true;
            }
        }
        return false;
    }

    //Add a spectator to the game
    @Override
    public void spectate(Spectator spectator) {
        spectators.add(spectator);
    }
    
    //Notify all spectators that a move has occurred
    private void notifySpectators(Move move) {
        for (Spectator spectator : spectators) {
            spectator.notify(move);
        }
    }

    //Add a player to the game
    @Override
    public boolean join(Player player, Colour colour, int location, Map<Ticket, Integer> tickets) {
        if (players.size() >= numberOfPlayers || getPlayer(colour) != null) {
            return false;
        }
        GamePlayer gamePlayer = new GamePlayer(player, colour, location, tickets);
        players.add(gamePlayer);
        if (colour.equals(Colour.Black)) {
            currentPlayer = gamePlayer;
            if (rounds.get(0)) mrXLocation = gamePlayer.location();
        }
        return true;
    }
    
    //Get the GamePlayer for a colour
    private GamePlayer getPlayer(Colour colour) {
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer.colour().equals(colour)) return gamePlayer;
        }
        return null;
    }

    //Get all player colors
    @Override
    public List<Colour> getPlayers() {
        List<Colour> colours = new ArrayList<Colour>();
        for (GamePlayer player : players) {
            colours.add(player.colour());
        }
        return colours;
    }

    //Check for winners
    @Override
    public Set<Colour> getWinningPlayers() {
        Set<Colour> winners = new HashSet<Colour>();
        if (detectivesNoValidMoves() || getPlayers().size() == 1 || roundCounter >= rounds.size()) {
            winners.add(Colour.Black);
        } else if (onMrX() || validMoves(Colour.Black).size() == 0) {
            for (GamePlayer player : players) {
                if (!player.colour().equals(Colour.Black)) winners.add(player.colour());
            }
        }
        return winners;
    }

    @Override
    public int getPlayerLocation(Colour colour) {
        GamePlayer player = getPlayer(colour);
        if (player.colour().equals(Colour.Black)) return mrXLocation;
        else return player.location();
    }
    
    /**
     * Returns the players true location regardless of 
     * if it is Mr X.
     *
     * @param colour the player colour.
     * @return the players true location regardless of
     * if it is Mr X.
     */
    public int getTruePlayerLocation(Colour colour) {
        GamePlayer player = getPlayer(colour);
        return player.location();
    }

    @Override
    public int getPlayerTickets(Colour colour, Ticket ticket) {
        GamePlayer player = getPlayer(colour);
        Map<Ticket, Integer> tickets = player.tickets();
        return tickets.get(ticket);
    }

    @Override
    public boolean isGameOver() {
        if (isReady() && ((roundCounter >= (rounds.size() - 1) && currentPlayer.colour().equals(Colour.Black)) || validMoves(Colour.Black).size() == 0
              || getPlayers().size() == 1 || onMrX() || detectivesNoValidMoves())) return true;
        return false;
    }
    
    private boolean detectivesNoValidMoves() {
        boolean noMoves = true;
        for (GamePlayer player : players) {
            if (!player.colour().equals(Colour.Black)) {
                List<Move> moves = validMoves(player.colour());
                if (!(moves.size() == 1 && moves.get(0) instanceof MovePass)) {
                    noMoves = false;
                }
            }
        }
        return noMoves;
    }
    
    private boolean onMrX() {
        GamePlayer mrX = getPlayer(Colour.Black);
        for (GamePlayer player : players) {
            if (!player.colour().equals(Colour.Black) && player.location().equals(mrX.location())) return true;
        }
        return false;
    }

    @Override
    public boolean isReady() {
        if (players.size() == numberOfPlayers && getPlayer(Colour.Black) != null) return true;
        return false;
    }

    @Override
    public Colour getCurrentPlayer() {
        return currentPlayer.colour();
    }

    @Override
    public int getRound() {
        return roundCounter;
    }
    
    public void setRound(int r) {
        roundCounter = r;
    }

    @Override
    public List<Boolean> getRounds() {
        return rounds;
    }
    
    // Returns the node in the graph with the given location.
    // Returns null if there is no node in the graph.
    // Needed as supplied code is incorrect.
    // @param id the location of the node to be found.
    // @return the node in the graph with the given location.
    private Node<Integer> getNode(Integer id) {
        for (Node<Integer> node : graph.getNodes()) {
            if (node.data().equals(id)) {
                return node;
            }
        }
        return null;
    }
    
}