package client.application;

import scotlandyard.*;
import net.*;
import client.view.*;
import client.view.Formatter;
import client.model.*;
import player.*;
import client.aiview.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;

/**
 * A class to start the application.
 */

public class ScotlandYardApplication implements WindowListener, ActionListener, Runnable {

    public boolean DEBUG = true;
    private boolean demo = false;
    private ScotlandYardGame game;
    private GameView gameView;
    private AIView aiView;
    private SetUpView setUpView;
    private FileAccess fileAccess;
    private ThreadCommunicator threadCom;
    private JPanel container;

    private final int kNormalTimer = 260;
    private final int kDemoTime = 20;

    /**
     * Is the entry point for the game.
     *
     * @param args the arguments from the command line.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { }
        ScotlandYardApplication application = new ScotlandYardApplication();

        SwingUtilities.invokeLater(application::go);
    }

    public ScotlandYardApplication() {
        this.fileAccess = new FileAccess();
        this.aiView = new AIView(fileAccess);
    }

    /**
     * Starts the window and adds the SetUpView.
     */
    public void go() {
        JFrame window = new JFrame();
        window.setMinimumSize(new Dimension(1200, 800));
        window.setPreferredSize(new Dimension(1200, 800));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setBackground(Formatter.aiBackgroundColor());
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setUndecorated(true);
        container = new JPanel(new CardLayout());
        setUpView = new SetUpView(fileAccess);
        setUpView.setActionListener(this);
        container.add(setUpView);
        gameView = new GameView(fileAccess);
        Action menu = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (game != null) {
                    game.saveGame();
                    endGame();
                }
            }
        };
        gameView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F10"), "menu");
        gameView.getActionMap().put("menu", menu);
        window.addComponentListener(gameView);
        container.add(gameView);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Formatter.aiBackgroundColor());
        GridBagConstraints constraints = new GridBagConstraints(0,
                                                                0,
                                                                2,
                                                                1,
                                                                0.5,
                                                                1.0,
                                                                GridBagConstraints.CENTER,
                                                                GridBagConstraints.BOTH,
                                                                new Insets(0, 0, 0, 0),
                                                                0,
                                                                0);
        GridBagConstraints aiconstraints = new GridBagConstraints(3,
                                                                  0,
                                                                  1,
                                                                  1,
                                                                  0.6,
                                                                  1.0,
                                                                  GridBagConstraints.CENTER,
                                                                  GridBagConstraints.BOTH,
                                                                  new Insets(0, 0, 0, 0),
                                                                  0,
                                                                  0);
        panel.add(container, constraints);
        panel.add(aiView, aiconstraints);
        window.add(panel);
        window.pack();
        window.setTitle("Scotland Yard");
        window.setLocationByPlatform(true);
        window.addWindowListener(this);
        window.setVisible(true);

        if (DEBUG){//?
            threadCom = new ThreadCommunicator();
            demo = true;
            beginGame(kDemoTime);
            newGame();
        }
    }

    /**
     * Called when the window is being closed. This then tells the
     * game to save it's current state.
     *
     * @param e the WindowEvent sent when the window closes.
     */
    public void windowClosing(WindowEvent e) {
        if (game != null) {
            game.saveGame();
        }
    }

    /**
     * Called when the buttons in the SetUpView are clicked by the user.
     *
     * @param e the ActionEvent containing information about which button
     * has been clicked.
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("startGame")) {
            if(validGameName(setUpView.newGameName())) {
                threadCom = new ThreadCommunicator();
                beginGame(kNormalTimer);
                newGame();
            } else {
                JOptionPane.showMessageDialog(null, "Game name must be at least " +
                        "one character long and not contain any #, /, \\. \nPlease choose another",
                        "Invalid Name", JOptionPane.WARNING_MESSAGE, fileAccess.getWarningIcon());
            }
        } else if (e.getActionCommand().equals("loadGame")) {
            if (setUpView.loadFilePath() != null) {
                threadCom = new ThreadCommunicator();
                beginGame(kNormalTimer);
                loadGame();
            }
        } else if (e.getActionCommand().equals("joinGame")) {
            try {
                threadCom = new ThreadCommunicator();
                String idString = setUpView.joinUsername();
                List<String> studentIds = Arrays.asList(idString.split(" "));
                String hostname = setUpView.joinIP();
                int port = Integer.parseInt(setUpView.joinPort());
                // Starts the GeneHuntPlayerFactory and PlayerClient on a new Thread.
                new Thread(new ScotlandYardAIGame(this, threadCom, hostname, port, studentIds)).start();
            } catch (Exception exc) {
                System.err.println("Error joining game :" + exc);
                exc.printStackTrace();
                System.exit(1);
            }
        }
    }

    // Returns true if  the given string is a valid game
    // name.
    // @param name the string to be checked.
    private boolean validGameName(String name) {
        if (name == null) return false;
        boolean containsHash = name.indexOf("#") != -1;
        boolean containsBackslash = name.indexOf("/") != -1;
        boolean containsForwardSlash = name.indexOf("\\") != -1;
        return (!containsHash) && (!containsBackslash) && (!containsForwardSlash);
    }

    /**
     * Updates the ThreadCommunicator object used by the GameView.
     * Then displays the GameView and hides the SetUpView.
     *
     * @param timerTime the max time for the Timer in the TimerView.
     */
    public void beginGame(int timerTime) {
        gameView.setTimerMaxTime(timerTime);
        gameView.setThreadCom(threadCom);
        aiView.setThreadCom(threadCom);
        CardLayout cl = (CardLayout) container.getLayout();
        cl.next(container);
        new Thread(this).start();
    }

    /**
     * Starts a the specified ScotlandYardGame in a new Thread.
     *
     * @param game the ScotlandYardGame to be started in a new Thread.
     */
    public void newAIGame(ScotlandYardGame game) {
        this.game = game;
        new Thread(game).start();

    }

    // Starts a new ScotlandYardGame in a new Thread.
    private void newGame() {
        int playerNo = setUpView.newPlayers();
        String gameName = setUpView.newGameName();
        if (demo) game = new ScotlandYardGame("resources/graph.txt", threadCom, demo);
        else game = new ScotlandYardGame(playerNo, gameName, "resources/graph.txt", threadCom);
        new Thread(game).start();
    }

    // Loads a previously played ScotlandYardGame in a new Thread.
    private void loadGame() {
        game = new ScotlandYardGame(setUpView.loadFilePath(), threadCom);
        new Thread(game).start();
    }

    // Removes the GameView and shows the SetUpView view
    public void endGame() {
        game.endGame();//?Sometimes null
        gameView.setThreadCom(null);
        game = null;
        CardLayout cl = (CardLayout) container.getLayout();
        cl.next(container);
        setUpView.refreshSaves();
        threadCom.clearEvents();
        threadCom.clearUpdates();
        if (demo) {
            threadCom = new ThreadCommunicator();
            demo = true;
            beginGame(kDemoTime);
            newGame();
        }
    }

    /**
     * Takes items off the queue and updates the views appropriately.
     */
    public void run() {
        while (true) {
            try {
                ThreadCommunicator.Packet packet = threadCom.takeUpdate();
                decodeUpdate(packet.getId(), packet.getObject());
            } catch (Exception e) {
                System.err.println("Error taking items from the queue :" + e);
                e.printStackTrace();
            }
        }
    }

    // Decodes the id of the update and acts accordingly.
    // The 'empty loop technique' is used so that if the List does not only
    // contain GamePlayer objects an exception will be thrown near to the cause.
    // @param id the id of the update.
    // @param object the object associated with the id.
    private void decodeUpdate(String id, Object object) {
        if (id.equals("init_views")) {
            @SuppressWarnings("unchecked")
            List<GamePlayer> players = (List<GamePlayer>) object;
            for (GamePlayer player : players);
            gameView.initialise(players);
        } else if (id.equals("update_board")) {
            Move move = (Move) object;
            gameView.updateBoard(move);
        } else if (id.equals("update_tickets")) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) object;
            Colour colour = (Colour) list.get(0);
            @SuppressWarnings("unchecked")
            Map<Ticket, Integer> tickets = (Map<Ticket, Integer>) list.get(1);
            for (Map.Entry<Ticket, Integer> e : tickets.entrySet());
            gameView.updateTickets(colour, tickets);
        } else if (id.equals("send_notification")) {
            String message = (String) object;
            gameView.setNotification(message);
        } else if (id.equals("zoom_in")) {
            if(!aiView.onTreeView()) {
                Integer location = (Integer) object;
                gameView.zoomToNode(location);
            }
        } else if (id.equals("zoom_out")) {
            gameView.zoomOut();
        } else if (id.equals("reset_timer")) {
            gameView.resetTimer();
        } else if (id.equals("stop_timer")) {
            gameView.stopTimer();
        } else if (id.equals("show_route")) {
            @SuppressWarnings("unchecked")
            List<RouteHint> list = (List<RouteHint>) object;
            for (RouteHint item : list);
            gameView.setRouteHint(list);
        } else if (id.equals("select_ticket")) {
            @SuppressWarnings("unchecked")
            List<RouteHint> list = (List<RouteHint>) object;
            for (RouteHint item : list);
            gameView.setRouteHint(list);
        } else if (id.equals("end_game")) {
            endGame();
        } else if (id.equals("highlight_node")) {
            Integer location = (Integer) object;
            gameView.highlightNode(location);
        } else if (id.equals("valid_moves")) {
            @SuppressWarnings("unchecked")
            Set<Move> moves = (Set<Move>) object;
            for (Move move : moves);
            gameView.updateRoutes(moves);
        } else if (id.equals("update_log")) {
            Move move = (Move) object;
            gameView.updateLog(move);
        } else if (id.equals("update_log_message")) {
            String string = (String) object;
            gameView.updateLog(string);
        } else if (id.equals("clear_log")) {
            gameView.clearLog();
        } else if (id.equals("update_round")) {
            Integer roundNo = (Integer) object;
            gameView.updateRoundCounter(roundNo);
        } else if (id.equals("ai_set_rep")) {
            TreeNode rep = (TreeNode) object;
            aiView.setRep(rep);
        } else if (id.equals("link_tree")) {
            GameTree gameTree = (GameTree) object;
            aiView.setGameTree(gameTree);
        }  else if (id.equals("ai_end")) {
            aiView.stop();
        } else if (id.equals("get_move")) {
            Move move = (Move) object;
            //For rating player moves
        } else if (id.equals("clear_notificaton")) {
            gameView.clearNotification();
        } else if (id.equals("current_player")) {
            Colour colour = (Colour) object;
            gameView.setCurrentPlayer(colour);
        } else if (id.equals("ai_thought")) {
            aiView.setTitle("The AI has finished thinking");
        }
    }

    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowOpened(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowClosed(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowIconified(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowDeiconified(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowActivated(WindowEvent e) {}
    /**
     * Unused method from the WindowListener interface.
     *
     * @param e the WindowEvent from the listener.
     */
    public void windowDeactivated(WindowEvent e) {}

}
