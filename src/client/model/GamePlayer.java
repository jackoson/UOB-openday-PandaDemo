package client.model;

import scotlandyard.*;

import java.util.*;
import java.io.Serializable;

/**
 * A class that contains all the information about a particular player.
 */

public class GamePlayer {

    private Player player;
    private Colour colour;
    private Integer location;
    private Map<Ticket, Integer> tickets;
    
    /**
     * Constructs a new GamePlayer object.
     *
     * @param player the Player object associated with the player.
     * @param colour the colour of the player.
     * @param location the location of the player.
     * @param tickets the tickets associated with the player.
     */
    public GamePlayer(Player player, Colour colour, Integer location, Map<Ticket, Integer> tickets) {
        this.player = player;
        this.colour = colour;
        this.location = location;
        this.tickets = tickets;
    }
    
    public GamePlayer(GamePlayer player) {
        this.player = player.player();
        this.colour = player.colour();
        this.location = new Integer(player.location());
        Map<Ticket, Integer> ticketMap = new HashMap<Ticket, Integer>();
        for (Map.Entry<Ticket, Integer> entry : player.tickets().entrySet()) {
            ticketMap.put(entry.getKey(), new Integer(entry.getValue()));
        }
        this.tickets = ticketMap;
    }
    
    /**
     * Returns the Player object associated with the player.
     *
     * @return the Player object associated with the player.
     */
    public Player player() {
        return player;
    }
    
    /**
     * Returns the Colour object associated with the player.
     *
     * @return the Colour object associated with the player.
     */
    public Colour colour() {
        return colour;
    }
    
    /**
     * Sets the player's current location.
     *
     * @param location the player's current location.
     */
    public void setLocation(Integer location) {
        this.location = location;
    }
    
    /**
     * Returns the player's current location.
     *
     * @return the player's current location.
     */
    public Integer location() {
        return location;
    }
    
    /**
     * Returns the player's current tickets.
     *
     * @return the player's current tickets.
     */
    public Map<Ticket, Integer> tickets() {
        return tickets;
    }
    
    /**
     * Sets the player's current tickets.
     *
     * @param tickets the player's current tickets.
     */
    public void setTickets(Map<Ticket, Integer> tickets) {
        this.tickets = tickets;
    }
    
    /**
     * Adds a ticket to the player's current tickets.
     *
     * @param ticket the ticket to be added.
     */
    public void addTicket(Ticket ticket) {
        incTicket(true, ticket);
    }
    
    /**
     * Removes a ticket to the player's current tickets.
     *
     * @param ticket the ticket to be removed.
     */
    public void removeTicket(Ticket ticket) {
        incTicket(false, ticket);
    }
    
    // Increments the ticket count for a particular Ticket.
    // @param inc the boolean to decide whether to increment (true) or decrement (false).
    // @param ticket the Ticket whose number is to be changed.
    private void incTicket(boolean inc, Ticket ticket) {
        Integer ticketCount = tickets.get(ticket);
        if (inc) ticketCount++;
        else ticketCount--;
        tickets.remove(ticket);
        tickets.put(ticket, ticketCount);
    }
    
}
