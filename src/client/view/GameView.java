package client.view;

import client.scotlandyard.*;
import client.application.*;
import client.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * A view to display the board, players information and Mr X's previous moves.
 */

public class GameView extends JPanel implements ComponentListener, ActionListener {
    
    private static final long serialVersionUID = -8523153159692178708L;

    private BoardView board;
    private NotifyView notify;
    private TimerView timer;
    private PlayerTicketView ticket;
    private ChatView chat;
    private FileAccess fileAccess;
    private ThreadCommunicator threadCom;
    
    /**
     * Constructs a new GameView object.
     *
     * @param fileAccess the FileAccess object needed to get the 
     * images for the views.
     * @param threadCom the ThreadCommunicator object to communicate
     * between Threads.
     */
    public GameView(FileAccess fileAccess, ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
        setPreferredSize(new Dimension(1272, 809));
        setLayout(new BorderLayout());
        this.fileAccess = fileAccess;
        board = new BoardView(fileAccess);
        notify = new NotifyView(fileAccess.getNotify());
        JPanel info = new JPanel(new BorderLayout());
        info.setBackground(new Color(20, 155, 247));
        chat = new ChatView();
        chat.setBackground(new Color(20, 155, 247));
        chat.setActionListener(this);
        info.add(chat, BorderLayout.WEST);
        ticket = new PlayerTicketView(fileAccess);
        ticket.setActionListener(this);
        info.add(ticket, BorderLayout.CENTER);
        timer = new TimerView();
        timer.setActionListener(this);
        info.add(timer, BorderLayout.EAST);
        board.setPreferredSize(new Dimension(1000, 749));
        board.setLayout(new BorderLayout());
        //Add suggested moves BorderLayout.EAST.
        board.setActionListener(this);
        JPanel jpanel = new JPanel(new BorderLayout());
        jpanel.setOpaque(false);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(notify);
        jpanel.add(centerPanel, BorderLayout.NORTH);
        board.add(jpanel, BorderLayout.CENTER);
        add(board, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
    }
    
    /**
     * Receives the ActionEvents from the views that it has
     * been added to and updates the game model accordingly.
     *
     * @param e the ActionEvent from the views.
     */
    public void actionPerformed(ActionEvent e) {
        //Handle actions from views and put items onto event queue as appropriate
        if (e.getActionCommand().equals("node")) {
            //Player has clicked on a node in board view
            threadCom.putEvent("node_clicked", (Integer) e.getSource());
        } else if (e.getActionCommand().equals("timer")) {
            //Player has run out of time for their move
            threadCom.putEvent("timer_fired", true);
        } else if (e.getActionCommand().equals("timer_warning")) {
            //Player has run out of time for their move
            threadCom.putEvent("timer_warning", true);
        } else if (e.getActionCommand().equals("message")) {
            //Player has pressed enter in the text field.
             threadCom.putEvent("message_entered", (String) e.getSource());
        } else if (e.getActionCommand().equals("ticket")) {
            //Player has clicked on a ticket in the players view
            threadCom.putEvent("ticket_clicked", (Ticket) e.getSource());
        } else if (e.getActionCommand().equals("hint")) {
            //Player has clicked on a ticket in the players view
            threadCom.putEvent("hint_clicked", (Boolean) e.getSource());
        }
    }
    
    /**
     * Calls the BoardView to resize the map when the window is resized.
     * This is necessary to maintain aspect ratio of the map.
     *
     * @param e the ComponentEvent that fires when the window is resized.
     */
    public void componentResized(ComponentEvent e) {
        board.updateDisplay(e);
    }
    
    /**
     * Initialises all sub views at the start of a game.
     * 
     * @param players the List of players in the game.
     */
    public void initialise(List<GamePlayer> players) {
        board.update(players);
        // Needs updating.
        ticket.initialise(players.get(1), this);
        timer.stop();
    }
    
    /**
     * Stops the Timer in the TimerView.
     */
    public void stopTimer() {
        timer.stop();
    }
    
    /**
     * Resets the Timer in the TimerView.
     */
    public void resetTimer() {
        timer.stop();
        timer.start();
    }
    
    /**
     * Highlists a node in the board view.
     *
     * @param location the location of the node to be
     * highlighted.
     */
    public void highlightNode(Integer location) {
        board.highlightNode(location);
    }
    
    /**
     * Updates the message shown by the NotifyView.
     * 
     * @param message the message to be shown to the players.
     */
    public void setNotification(String message) {
        notify.notify(message);
    }
    
    /**
     * Updates the current player.
     *
     * @param colour the color of the new current player.
     */
    public void setCurrentPlayer(Colour colour){
        //players.setCurrentPlayer(colour);
        //TO BE REMOVED
    }
    
    /**
     * Updates the players tickets shown by the PlayersView.
     * 
     * @param colour the colour of the player to update.
     * @param ticket the Ticket whose number has changed.
     * @param ticketNo the new number of tickets for the player.
     */
    public void updatePlayers(Colour colour, Ticket ticket, Integer ticketNo) {
        //players.update(colour, ticket, ticketNo);
        //TODO;
        if (colour.equals(Colour.Blue)) {
            this.ticket.update(colour, ticket, ticketNo);
        }
    }
    
    /**
     * Updates the locations of the players in the BoardView.
     * 
     * @param move the move containing the information to update the view.
     */
    public void updateBoard(Move move) {
        board.update(move);
    }
    
    /**
     * Updates the list of Mr X's previous moves in the MovesView.
     * If it is not Mr X's move, it does nothing.
     * 
     * @param move the move containing the information to update the view.
     */
    public void updateMoves(Move move) {
        //moves.hideLocations();
        //moves.update(move);
        //TO BE REMOVED
    }
    
    /**
     * Enlarges the map and centers the node in the view.
     *
     * @param location the node to be centered in the view.
     */
    public void zoomToNode(Integer location) {
        board.zoomToNode(location);
        //Animation here
    }
    
    /**
     * Shows the whole map in the BoardView.
     */
    public void zoomOut() {
        board.zoomOut();
        //Animation here
    }
  
    /**
     * Shows the location on the specified Move in the MoveView.
     *
     * @param label the JLabel containing the Move in the Move View.
     */
    public void showLocation(JLabel label) {
        //moves.showLocation(label);
        //TO BE REMOVED
    }
    
    /**
     * Adds the specified ActionListener to receive events from all sub views.
     * If listener listener is null, no action is performed.
     * 
     * @param listener the listener to be added to the view.
     */
    public void setActionListener(ActionListener listener) {
        board.setActionListener(listener);
        //players.setActionListener(listener);
        //moves.setActionListener(listener);
        timer.setActionListener(listener);
    }
    
    /**
     * Sets the route to be displayed for the BoardView
     *
     * @param route the route to be added
     */
    public void setRouteHint(List<Integer> route) {
        board.setRouteHint(route);
    }
    
    /**
     * Unused method from the ComponentListener interface.
     */
    public void componentHidden(ComponentEvent e) {}
    /**
     * Unused method from the ComponentListener interface.
     */
    public void componentMoved(ComponentEvent e) {}
    /**
     * Unused method from the ComponentListener interface.
     */
    public void componentShown(ComponentEvent e) {}

}