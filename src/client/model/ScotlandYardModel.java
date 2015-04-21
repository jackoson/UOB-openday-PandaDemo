package client.model;

import scotlandyard.*;

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
        Set<Move> moves = validMoves(colour);
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
        currentPlayer = ModelHelper.getNextPlayer(players, currentPlayer);
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
            newMove = MoveTicket.instance(move.colour, move.ticket, mrXLocation);
        } else {
            GamePlayer mrX = getPlayer(Colour.Black);
            mrX.addTicket(move.ticket);
        }
        notifySpectators(newMove);
    }

    //Play a MoveDouble
    @Override
    protected void play(MoveDouble move) {
        notifySpectators(move);
        play(move.move1);
        play(move.move2);
        GamePlayer gamePlayer = getPlayer(move.colour);
        gamePlayer.removeTicket(Ticket.Double);
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
    protected Set<Move> validMoves(Colour player) {
        GamePlayer gamePlayer = getPlayer(player);
        Set<Move> validMoves = ModelHelper.validMoves(gamePlayer, players, graph);
        return validMoves;
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
        return ModelHelper.getWinningPlayers(players, currentPlayer, graph, rounds, roundCounter);
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
              || getPlayers().size() == 1 || ModelHelper.onMrX(players) || ModelHelper.detectivesNoValidMoves(players, graph))) return true;
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
    
}