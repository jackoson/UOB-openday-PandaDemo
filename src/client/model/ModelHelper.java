package client.model;

import scotlandyard.*;

import java.util.*;


public class MoveHandler {
    
    //Create a list of valid moves
    static public Set<Move> validMoves(GamePlayer gamePlayer, List<GamePlayer> players, Graph<Integer, Route> graph) {
        Colour player = gamePlayer.colour();
        int secretMoveCount = gamePlayer.tickets().get(Ticket.Secret);
        int doubleMoveCount = gamePlayer.tickets().get(Ticket.Double);
        Node<Integer> currentPosition = graph.getNode(gamePlayer.location());
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
    
    //Create a list of valid single moves
    static private Set<MoveTicket> createSingleMoves(GamePlayer gamePlayer, List<GamePlayer> players, Graph<Integer, Route> graph, Integer location) {
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
    
    //Create a list of valid double moves including secret moves
    static private Set<MoveDouble> createDoubleMoves(GamePlayer gamePlayer, List<GamePlayer> players, Graph<Integer, Route> graph, Set<MoveTicket> moves, int secretMoveCount) {
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
    
    //Create a list of secret single moves
    static private Set<MoveTicket> createSingleSecretMoves(Set<MoveTicket> moves) {
        Set<MoveTicket> secretMoves = new HashSet<MoveTicket>();
        for (MoveTicket move : moves) {
            secretMoves.add(makeSecret(move));
        }
        return secretMoves;
    }
    
    static private MoveTicket makeSecret(MoveTicket move) {
        return MoveTicket.instance(move.colour, Ticket.Secret, move.target);
    }
    
    //Check to see if player has required moves
    static private boolean hasTickets(GamePlayer gamePlayer, MoveTicket move1, MoveTicket move2) {
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
    
    //Checks whether a detective occupies a given node
    static private boolean nodeOccupied(List<GamePlayer> players, Node<Integer> node) {
        for (GamePlayer player : players) {
            if (node.data().equals(player.location())) {
                if (!player.colour().equals(Colour.Black)) return true;
            }
        }
        return false;
    }

}