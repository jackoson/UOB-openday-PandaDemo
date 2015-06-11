package client.model;

import scotlandyard.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * A class to perform certain operations used by both the game model and the AI.
 */

public class ModelHelper {

    /**
     * Returns the List of Booleans determining when Mr X is visible.
     * Only the advanced version of the game is supported at this time.
     *
     * @return the List of Booleans determining when Mr X is visible.
     */
    public static List<Boolean> getRounds() {
        return Arrays.asList(false, false, false, true, false,
                             false, false, false, true, false,
                             false, false, false, true, false,
                             false, false, false, true, false,
                             false, false, false, false, true);
    }

    /**
     * Returns the GamePlayer object of the specified player from a List.
     *
     * @param players the List of GamePlayer objects.
     * @param colour the player whose GamePlayer object is to be returned.
     * @return the GamePlayer object of the specified player from a List.
     */
    public static GamePlayer getPlayerOfColour(List<GamePlayer> players, Colour colour) {
        for (GamePlayer gamePlayer : players) {
            if (gamePlayer.colour().equals(colour)) return gamePlayer;
        }
        return null;
    }

    /**
     * Returns the GamePlayer object of the player whose turn it is next.
     *
     * @param players the List of GamePlayer objects for all players in the game.
     * @param currentPlayer the GamePlayer object of the current player in the game.
     * @return the GamePlayer object of the player whose turn it is next.
     */
    public static GamePlayer getNextPlayer(List<GamePlayer> players, GamePlayer currentPlayer) {
        int currentPosition = players.indexOf(currentPlayer);
        GamePlayer nextPlayer = players.get((currentPosition + 1) % players.size());
        return nextPlayer;
    }

    /**
     * Returns the Set of players who have won the game.
     *
     * @param players the List of players in the game.
     * @param currentPlayers the current player in the game.
     * @param graph the Graph associated with the game.
     * @param rounds the List of Booleans that decide when Mr X is visible.
     * @param round the current round of the game.
     * @return the Set of players who have won the game.
     */
    public static Set<Colour> getWinningPlayers(List<GamePlayer> players, Colour currentPlayer, Graph<Integer, Route> graph, Integer round) {
        Set<Colour> winners = new HashSet<Colour>();
        if (detectivesNoValidMoves(players, graph) || players.size() == 1
            || (round >= (getRounds().size() - 1)
                && currentPlayer.equals(Colour.Black))) {
                winners.add(Colour.Black);
            } else if (onMrX(players) || validMoves(players.get(0), players, graph).size() == 0) {
                for (GamePlayer player : players) {
                    if (!player.colour().equals(Colour.Black)) winners.add(player.colour());
                }
            }
        return winners;
    }

    /**
     * Returns true if all detectives have no valid Moves.
     *
     * @param players the List of players in the game.
     * @param graph the Graph associated with the game.
     * @return true if all detectives have no valid Moves.
     */
    public static boolean detectivesNoValidMoves(List<GamePlayer> players, Graph<Integer, Route> graph) {
        boolean noMoves = true;
        for (GamePlayer player : players) {
            if (!player.colour().equals(Colour.Black)) {
                Set<Move> moves = validMoves(player, players, graph);
                if (!(moves.size() == 1 && moves.iterator().next() instanceof MovePass)) {
                    noMoves = false;
                }
            }
        }
        return noMoves;
    }

    /**
     * Returns true if a detective shares the same Node as Mr X.
     *
     * @param players the List of players in the game.
     * @return true if a detective shares the same Node as Mr X.
     */
    public static boolean onMrX(List<GamePlayer> players) {
        GamePlayer mrX = players.get(0);
        for (GamePlayer player : players) {
            if (!player.colour().equals(Colour.Black) && player.location().equals(mrX.location())) return true;
        }
        return false;
    }

    /**
     * Returns the Set of valid Moves a player can make.
     *
     * @param gamePlayer the player for whom to generate the Set of valid Moves.
     * @param players the List of players in the game.
     * @param graph the Graph associated with the game.
     * @return the Set of valid Moves a player can make.
     */
    public static Set<Move> validMoves(GamePlayer gamePlayer, List<GamePlayer> players, Graph<Integer, Route> graph) {
        Colour player = gamePlayer.colour();
        int secretMoveCount = gamePlayer.tickets().get(Ticket.Secret);
        int doubleMoveCount = gamePlayer.tickets().get(Ticket.Double);
        Node<Integer> currentPosition = graph.getNode(gamePlayer.location());
        if (currentPosition == null) currentPosition = graph.getNode(1); //NEED TO FIND A BETTER SOLUTION TO THIS
        Set<Move> allMoves = new HashSet<Move>();
        Set<MoveTicket> singleMoves = createSingleMoves(gamePlayer, players, graph, currentPosition.data());
        allMoves.addAll(singleMoves);

        if (player.equals(Colour.Black)) {
            Set<MoveTicket> secretMoves = new HashSet<MoveTicket>();
            Set<MoveDouble> doubleMoves = new HashSet<MoveDouble>();
            if (secretMoveCount >= 1) secretMoves = createSingleSecretMoves(singleMoves);
            if (doubleMoveCount >= 1) doubleMoves = createDoubleMoves(gamePlayer, players, graph, singleMoves, secretMoveCount);

            allMoves.addAll(secretMoves);
            allMoves.addAll(doubleMoves);
        } else if (allMoves.size() == 0) {
            allMoves.add(MovePass.instance(player));
        }
        return allMoves;
    }

    // Returns a Set of valid MoveTickets for a specified player.
    // @param gamePlayer the player for whom to generate the Set of valid MoveTickets.
    // @param players the List of players in the game.
    // @param graph the Graph associated with the game.
    // @param location the location of the player.
    // @return a Set of valid single Moves for a specified player.
    private static Set<MoveTicket> createSingleMoves(GamePlayer gamePlayer, List<GamePlayer> players, Graph<Integer, Route> graph, Integer location) {
        Set<MoveTicket> moves = new HashSet<MoveTicket>();
        Set<Edge<Integer, Route>> edges = graph.getEdges(location);

        for (Edge<Integer, Route> edge : edges) {
            Node<Integer> node = graph.getNode(edge.source());
            if (node.data().equals(location)) node = graph.getNode(edge.target());

            Ticket ticket = Ticket.fromRoute(edge.data());
            MoveTicket newMove = MoveTicket.instance(gamePlayer.colour(), ticket, (int) node.data());
            if (!nodeOccupied(players ,node) && hasTickets(gamePlayer, newMove, null)) {
                moves.add(newMove);
            }
        }
        return moves;
    }

    // Returns a Set of valid MoveDoubles for a specified player.
    // @param gamePlayer the player for whom to generate the Set of valid MoveDoubles.
    // @param players the List of players in the game.
    // @param graph the Graph associated with the game.
    // @param moves the Set of valid MoveTickets for the player.
    // @param secondMoveCount the number of secret Tickets the player has.
    // @return a Set of valid MoveDoubles for a specified player.
    private static Set<MoveDouble> createDoubleMoves(GamePlayer gamePlayer, List<GamePlayer> players, Graph<Integer, Route> graph, Set<MoveTicket> moves, int secretMoveCount) {
        Set<MoveDouble> doubleMoves = new HashSet<MoveDouble>();
        for (MoveTicket move : moves) {
            Set<MoveTicket> secondMoves = createSingleMoves(gamePlayer, players,  graph, move.target);

            for (MoveTicket secondMove : secondMoves) {

                if (hasTickets(gamePlayer, move, secondMove)) doubleMoves.add(MoveDouble.instance(move.colour, move, secondMove));
                if (secretMoveCount >= 1) {
                    if (hasTickets(gamePlayer, secondMove, null)) doubleMoves.add(MoveDouble.instance(move.colour, makeSecret(move), secondMove));
                    if (hasTickets(gamePlayer, move, null)) doubleMoves.add(MoveDouble.instance(move.colour, move, makeSecret(secondMove)));

                    if (secretMoveCount >= 2) doubleMoves.add(MoveDouble.instance(move.colour, makeSecret(move), makeSecret(secondMove)));
                }
            }
        }

        return doubleMoves;
    }

    // Returns a Set of valid MoveTickets using secret Tickets for a specified player.
    // @param moves the Set of valid MoveTickets for the specified player.
    // @return a Set of valid MoveTickets using secret Tickets for a specified player.
    private static Set<MoveTicket> createSingleSecretMoves(Set<MoveTicket> moves) {
        Set<MoveTicket> secretMoves = new HashSet<MoveTicket>();
        for (MoveTicket move : moves) {
            secretMoves.add(makeSecret(move));
        }
        return secretMoves;
    }

    // Returns the MoveTicket after the Ticket has been replaced with a secret Ticket.
    // @param move the MoveTicket to be converted.
    // @return the MoveTicket after the Ticket has been replaced with a secret Ticket.
    private static MoveTicket makeSecret(MoveTicket move) {
        return MoveTicket.instance(move.colour, Ticket.Secret, move.target);
    }

    // Returns true if the specified player has enough Tickets for a MoveDouble.
    // @param gamePlayer the player whose Tickets are to be checked.
    // @param move1 the first Move in the MoveDouble.
    // @param move2 the second Move in the MoveDouble.
    // @return true if the specified player has enough Tickets for a MoveDouble.
    private static boolean hasTickets(GamePlayer gamePlayer, MoveTicket move1, MoveTicket move2) {
        Ticket ticket1;
        if (move1 != null){
            ticket1 = move1.ticket;
            if (gamePlayer.tickets().get(ticket1) < 1) return false;
        } else {
            return false;
        }
        if (move2 != null){
            Ticket ticket = move2.ticket;
            int num = 1;
            if (ticket.equals(ticket1)) num = 2;
            if (gamePlayer.tickets().get(ticket) < num) return false;
        }
        return true;
    }

    // Returns true if a detective lies on the specified Node.
    // @param players the List of players in the game.
    // @param node the Node to be checked.
    // @return true if a detective lies on the specified Node.
    private static boolean nodeOccupied(List<GamePlayer> players, Node<Integer> node) {
        for (GamePlayer player : players) {
            if (node.data().equals(player.location())) {
                if (!player.colour().equals(Colour.Black)) return true;
            }
        }
        return false;
    }

    /**
     * Returns the Map of Tickets for a specified player.
     *
     * @param player the player whose Tickets are to be returned.
     * @param model the ScotlandYardView which contains the players information.
     * @return the Map of Tickets for a specified player.
     */
    public static Map<Ticket, Integer> getTickets(Colour player, ScotlandYardView model) {
        Map<Ticket, Integer> tickets = new ConcurrentHashMap<Ticket, Integer>();
        tickets.put(Ticket.Taxi, model.getPlayerTickets(player, Ticket.Taxi));
        tickets.put(Ticket.Bus, model.getPlayerTickets(player, Ticket.Bus));
        tickets.put(Ticket.Underground, model.getPlayerTickets(player, Ticket.Underground));
        tickets.put(Ticket.Secret, model.getPlayerTickets(player, Ticket.Secret));
        tickets.put(Ticket.Double, model.getPlayerTickets(player, Ticket.Double));
        return tickets;
    }

    public static Integer getLocation(Move move) {
        if (move instanceof MovePass) return null;
        else if (move instanceof MoveTicket) {
            MoveTicket moveTicket = (MoveTicket) move;
            return moveTicket.target;
        } else {
            MoveDouble moveDouble = (MoveDouble) move;
            return getLocation(moveDouble.move2);
        }
    }

}
