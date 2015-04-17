package client.view;

import scotlandyard.*;
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
    private ListView listView;
    private MenuBar bar;
    private FileAccess fileAccess;
    private ThreadCommunicator threadCom = null;
    
    /**
     * Constructs a new GameView object.
     *
     * @param fileAccess the FileAccess object needed to get the 
     * images for the views.
     * @param threadCom the ThreadCommunicator object to communicate
     * between Threads.
     */
    public GameView(FileAccess fileAccess) {
        setPreferredSize(new Dimension(1200, 800));
        setLayout(new BorderLayout());
        this.fileAccess = fileAccess;
        listView = new ListView();
        listView.setListener(this);
        board = new BoardView(fileAccess);
        notify = new NotifyView(fileAccess.getNotify());
        chat = new ChatView();
        chat.setActionListener(this);
        ChatEventView eventView = new ChatEventView(fileAccess);
        ticket = new PlayerTicketView(fileAccess);
        ticket.setActionListener(this);
        timer = new TimerView();
        timer.setActionListener(this);
        board.setLayout(new BorderLayout());
        board.add(listView, BorderLayout.EAST);
        board.setActionListener(this);
        JPanel jpanel = new JPanel(new BorderLayout());
        jpanel.setOpaque(false);
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(notify);
        jpanel.add(centerPanel, BorderLayout.NORTH);
        board.add(jpanel, BorderLayout.CENTER);
        add(board, BorderLayout.CENTER);
        bar = new MenuBar(chat, eventView, ticket, timer);
        board.add(bar, BorderLayout.SOUTH);
    }
    
    /**
     * Receives the ActionEvents from the views that it has
     * been added to and updates the game model accordingly.
     *
     * @param e the ActionEvent from the views.
     */
    public void actionPerformed(ActionEvent e) {
        if (threadCom != null) {
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
            } else if (e.getActionCommand().equals("show_chat")) {
                bar.showChat();
            } else if (e.getActionCommand().equals("hide_chat")) {
                bar.hideChat();
            } else if (e.getActionCommand().equals("list_cell_highlighted")) {
                ListView listView = (ListView) e.getSource();
                @SuppressWarnings("unchecked")
                List<Integer> route = (List<Integer>) listView.highlightedRoute();
                for (Integer i : route);
                setRouteHint(route);
            } else if (e.getActionCommand().equals("list_cell_unhighlighted")) {
                List<Integer> route = new ArrayList<Integer>();
                setRouteHint(route);
            }
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
     * Sets the ThreadCommunicator that the view should use.
     *
     * @param threadCom the ThreadCommunicator to use.
     */
    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }
    
    /**
     * Initialises all sub views at the start of a game.
     * 
     * @param players the List of players in the game.
     */
    public void initialise(List<GamePlayer> players) {
        board.update(players);
        bar.setBackgroundColor(Formatter.colorForPlayer(Colour.Black));
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
    
    public void updateTickets(Colour colour, Map<Ticket, Integer> tickets) {
        bar.setBackgroundColor(Formatter.colorForPlayer(colour));
        ticket.update(tickets);
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
     * Updates the valid Moves shown by RouteView and the List in BoardView.
     *
     * @param moves the List of valid Moves.
     */
    public void updateRoutes(Set<Move> moves) {
        board.updateValidMoves(moves);
        List<ListCellView> v = new ArrayList<ListCellView>();
        Iterator<Move> it = moves.iterator();
        for (int i = 0; i < moves.size(); i++) {
            if(v.size() > 14) break;
            Move move = it.next();
            if (move instanceof MoveDouble) continue;
            List<Move> m = new ArrayList<Move>();
            m.add(move);
            v.add(new RouteView(m, fileAccess));
        }
        listView.setCells(v);
        listView.setPreferredSize(new Dimension(200, 1000));
    }
    
    /**
     * Enlarges the map and centers the node in the view.
     *
     * @param location the node to be centered in the view.
     */
    public void zoomToNode(Integer location) {
        board.zoomToNode(location);
    }
    
    /**
     * Shows the whole map in the BoardView.
     */
    public void zoomOut() {
        board.zoomOut();
    }
    
    /**
     * Adds the specified ActionListener to receive events from all sub views.
     * If listener listener is null, no action is performed.
     * 
     * @param listener the listener to be added to the view.
     */
    public void setActionListener(ActionListener listener) {
        board.setActionListener(listener);
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