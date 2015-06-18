package client.application;

import scotlandyard.*;
import client.model.*;
import client.algorithms.*;
import player.*;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.JLabel;

/**
 * A class that runs on a new Thread and starts a new instance of the game.
 */

public class ScotlandYardGame implements Player, Spectator, Runnable {

    private ScotlandYard model;
    private SaveGame saveGame = null;
    private int numPlayers;
    private List<GamePlayer> players;
    private String gameName;
    private String graphName;
    private InputPDA pda;
    private ThreadCommunicator threadCom;
    private FileAccess fileAccess;
    private boolean outOfTime = false;
    private Dijkstra routeFinder;
    private boolean firstRound = true;
    private boolean replaying = false;
    private final boolean aiGame;
    private boolean demo = false;
    private Move aiMove = null;

    private final int kDetectiveWait = 3000;
    private final int kMoveWait = 2000;
    private final int kAnimationWait = 500;
    private final int kRateMoveWait = 10000;

    private int[] detectiveLocations = {26, 29, 50, 53, 91, 94, 103, 112, 117, 123, 138, 141, 155, 174};
    private int[] mrXLocations = {35, 45, 51, 71, 78, 104, 106, 127, 132, 166, 170, 172};

    /**
     * Constructs a new ScotlandYardGame object.
     *
     * @param numPlayers the number of players in the game.
     * @param gameName the name of the game - for the game save.
     * @param graphName the name of the graph file for the game.
     * @param threadCom the TreadCommunicator object to
     * communicate between Threads.
     */
    public ScotlandYardGame(int numPlayers, String gameName, String graphName, ThreadCommunicator threadCom) {
        aiGame = false;
        try {
            this.threadCom = threadCom;
            this.numPlayers = numPlayers;
            this.gameName = gameName;
            this.graphName = graphName;
            routeFinder = new Dijkstra(graphName);
            List<Boolean> rounds = ModelHelper.getRounds();
            model = new ScotlandYardModel(numPlayers - 1, rounds, graphName);
            model.spectate(this);
            int randMrXLocation = randomMrXLocation();
            int[] randDetectiveLocations = randomDetectiveLocations(numPlayers - 1);
            this.players = initialiseGame(randMrXLocation, randDetectiveLocations, false);
            saveGame = new SaveGame(numPlayers, graphName, gameName);
            saveGame.setMrXLocation(randMrXLocation);
            saveGame.setDetectiveLocations(randDetectiveLocations);
            fileAccess = new FileAccess();
        } catch (Exception e) {
            System.err.println("Error setting up new game :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Constructs a ScotlandYardGame from a game save.
     *
     * @param gameName the name of the game to load.
     * @param threadCom the TreadCommunicator object to
     * communicate between Threads.
     */
    public ScotlandYardGame(String gameName, ThreadCommunicator threadCom) {
        aiGame = false;
        try {
            this.threadCom = threadCom;
            fileAccess = new FileAccess();
            saveGame = fileAccess.loadGame(gameName);
            this.numPlayers = saveGame.getNumberOfPlayers();
            this.gameName = saveGame.getGameName();
            this.graphName = saveGame.getGraphFileName();
            routeFinder = new Dijkstra(graphName);
            List<Boolean> rounds = ModelHelper.getRounds();
            model = new ScotlandYardModel(numPlayers - 1, rounds, graphName);
            model.spectate(this);
            players = initialiseGame(saveGame.getMrXLocation(), saveGame.getDetectiveLocations(), false);
            replaying = true;
        } catch (Exception e) {
            System.err.println("Error loading game :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Constructs a networked ScotlandYardGame without a game save.
     *
     * @param model the ScotlandYardView that controls the game.
     * @param graphName the path to the graph file.
     * @param threadCom the ThreadCommunicator object to
     * communicate between Threads.
     */
    public ScotlandYardGame(ScotlandYardView model, String graphName, ThreadCommunicator threadCom) {
        aiGame = true;
        try {
            this.threadCom = threadCom;
            this.graphName = graphName;
            this.routeFinder = new Dijkstra(graphName);
            this.model = (ScotlandYard) model;
            this.fileAccess = new FileAccess();
        } catch (Exception e) {
            System.err.println("Error joining a new game :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public ScotlandYardGame(String graphName, ThreadCommunicator threadCom, boolean demo) {
        aiGame = false;
        this.demo = demo;
        try {
            this.threadCom = threadCom;
            this.numPlayers = 2;
            this.graphName = graphName;
            routeFinder = new Dijkstra(graphName);
            List<Boolean> rounds = ModelHelper.getRounds();
            model = new ScotlandYardModel(numPlayers - 1, rounds, graphName);
            model.spectate(this);
            int randMrXLocation = randomMrXLocation();
            int[] randDetectiveLocations = randomDetectiveLocations(numPlayers - 1);
            this.players = initialiseGame(randMrXLocation, randDetectiveLocations, true);
            fileAccess = new FileAccess();
        } catch (Exception e) {
            System.err.println("Error setting up a demonstration game :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The run method called when the Thread is started.
     * Starts the game.
     */
    public void run() {
        try {
            pda = new InputPDA();
            initialiseViews(getPlayers());
            if (saveGame != null) fileAccess.saveGame(saveGame);
            if (!aiGame) {
                model.start();
                endGame();
            }
        } catch (Exception e) {
            System.err.println("Error playing game :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Called when the game is over.
     * Updates the views and then returns to the SetUpView.
     */
    public void endGame() {
        if (saveGame != null) fileAccess.saveGame(saveGame);
        threadCom.putUpdate("stop_timer", true);
        Set<Colour> winningPlayers = model.getWinningPlayers();
        sendNotification(getWinningMessage(winningPlayers));
        wait(5000);
        if (!aiGame) threadCom.putUpdate("end_game", true);
        threadCom.putUpdate("clear_log", true);
        threadCom.putUpdate("clear_notificaton", true);
    }

    // Returns the List of GamePlayer objects that contain all
    // of the information about the players who are currently in the game.
    // @return the List of GamePlayer objects that contain all
    // of the information about the players who are currently in the game.
    private List<GamePlayer> getPlayers() {
        if (players == null) {
            List<GamePlayer> playerList = new ArrayList<GamePlayer>();
            for (Colour player : model.getPlayers()) {
                playerList.add(new GamePlayer(null, player, model.getPlayerLocation(player), ModelHelper.getTickets(player, model)));
            }
            return playerList;
        } else {
            return players;
        }
    }

    // Returns the List of players in the game.
    // @param mrXLocation the initial location of Mr X.
    // @param detectiveLocations the initial locations of the Detectives.
    // @return the List of players in the game.
    private List<GamePlayer> initialiseGame(int mrXLocation, int[] detectiveLocations, boolean demo) {
        List<GamePlayer> players = new ArrayList<GamePlayer>();
        Colour[] colours = Colour.values();

        Map<Ticket, Integer> mrXTickets = new HashMap<Ticket, Integer>();
        mrXTickets.put(Ticket.Taxi, 10);
        mrXTickets.put(Ticket.Bus, 10);
        mrXTickets.put(Ticket.Underground, 10);
        mrXTickets.put(Ticket.Double, 2);
        mrXTickets.put(Ticket.Secret, 5);
        Player player = this;
        if (demo) player = new GeneHunt(model, graphName, threadCom, this);
        model.join(player, colours[0], mrXLocation, mrXTickets);
        players.add(new GamePlayer(player, colours[0], 0, mrXTickets));

        for (int i = 1; i < numPlayers; i++) {
            Map<Ticket, Integer> detectiveTickets = new HashMap<Ticket, Integer>();
            detectiveTickets.put(Ticket.Taxi, 11);
            detectiveTickets.put(Ticket.Bus, 8);
            detectiveTickets.put(Ticket.Underground, 4);
            detectiveTickets.put(Ticket.Double, 0);
            detectiveTickets.put(Ticket.Secret, 0);
            model.join(this, colours[i], detectiveLocations[i - 1], detectiveTickets);
            players.add(new GamePlayer(this, colours[i], detectiveLocations[i - 1], detectiveTickets));
        }
        return players;
    }

    // Returns an array of random locations for the detectives.
    // @param noOfDetectives the number of detectives to generate random locations for.
    // @return an array of random locations for the detectives.
    private int[] randomDetectiveLocations(int noOfDetectives) {
        int[] locations = new int[noOfDetectives];
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 14; i++) {
            list.add(i);
        }
        Collections.shuffle(list);
        for (int i = 0; i < noOfDetectives; i++) {
            locations[i] = detectiveLocations[list.get(i)];
        }
        return locations;
    }

    // Returns a random location for Mr X.
    // @return a random location for Mr X.
    private int randomMrXLocation() {
        Random random = new Random();
        return mrXLocations[random.nextInt(12)];
    }

    public Integer mrXLocatation() {
        if (model != null) return model.getPlayerLocation(Colour.Black);
        return 0;
    }

    /**
     * Returns the Move chosen by the player.
     * Also updates the views for the start of a Move.
     *
     * @param location the actual location of the player.
     * @param moves the List of valid Moves the player can take.
     * @return the Move chosen by the player.
     */
    public Move notify(int location, Set<Move> moves) {
        threadCom.putUpdate("current_player", model.getCurrentPlayer());
        updateUI(location, model.getCurrentPlayer(), moves);
        outOfTime = false;
        pda.reset();
        Move move = null;
        while (true) {
            if (saveGame != null && saveGame.hasSavedMove()) {
                move = saveGame.getSavedMove();
                wait(kMoveWait);
                break;
            } else {
                replaying = false;
            }
            ThreadCommunicator.Packet packet = threadCom.takeEvent();
            decodeEvents(packet.getId(), packet.getObject());

            if (pda.isAccepted()) {
                move = pda.createMove(model.getCurrentPlayer());
                if (moves.contains(move)) {
                    threadCom.putUpdate("highlight_node", 0);
                    threadCom.putUpdate("human_move", move);
                    threadCom.putUpdate("stop_timer", true);
                    wait(kRateMoveWait);
                    break;
                } else {
                    pda.reset();
                    sendNotification("Invalid move, please try again.");
                }
            } else if (outOfTime) {
                if (moves.contains(aiMove)) move = aiMove;
                else move = moves.iterator().next();
                if (!demo) sendNotification("Out of time, a move has been chosen for you.");
                break;
            } else if (moves.iterator().next() instanceof MovePass) {
                move = moves.iterator().next();
                if (!demo) sendNotification("You don't have any valid moves.");
                break;
            }
        }
        if (saveGame != null) saveGame.addMove(move);
        return move;
    }

    /**
     * Updates the UI after a move has been made.
     *
     * @param move the Move that has been made.
     */
    public void notify(Move move) {
        threadCom.putUpdate("stop_timer", true);
        if (move instanceof MoveTicket) {
            updateUI(move);
        } else if (move instanceof MoveDouble) {
            threadCom.putUpdate("update_log", move);
        }
    }

    // Decodes the id from the queue and performs the appropriate action.
    // @param id the id from the queue.
    // @param object the object from the queue.
    private void decodeEvents(String id, Object object) {
        if (id.equals("node_clicked")) {
            Integer location = (Integer) object;
            threadCom.putUpdate("highlight_node", location);
            pda.transition(location);
        } else if (id.equals("timer_fired")) {
            outOfTime = true;
        } else if (id.equals("ticket_clicked")) {
            Ticket ticket = (Ticket) object;
            threadCom.putUpdate("highlight_node", 0);
            pda.transition(ticket);
        } else if (id.equals("timer_warning")) {
            sendNotification("Hurry up, 30 seconds left!");
        } else if (id.equals("ticket_clicked")) {
            Ticket ticket = (Ticket) object;
            pda.transition(ticket);
        } else if (id.equals("save_game")) {
            if (saveGame != null) fileAccess.saveGame(saveGame);
        }
    }

    // Initialises the views at the start of a game.
    // @param players the List of players in the game.
    private void initialiseViews(List<GamePlayer> players) {
        threadCom.putUpdate("init_views", players);
        threadCom.putUpdate("update_round", 1);
    }

    // Shows a message to the users.
    // @param message the message to be shown to the users.
    private void sendNotification(String message) {
        threadCom.putUpdate("send_notification", message);
    }

    // Updates the UI at the start of a turn.
    // @param location the actual locaton of the player.
    // @param player the colour of the player whose turn it is.
    // @param moves the Set of valid Moves the player can make.
    private void updateUI(Integer location, Colour player, Set<Move> moves) {
        updateTickets(player);
        if (player.equals(Colour.Black) && !replaying && !aiGame && !demo) {
            threadCom.putUpdate("send_notification", "Detectives, Please look away.");
            wait(kDetectiveWait);
        }
        threadCom.putUpdate("reset_timer", true);
        threadCom.putUpdate("valid_moves", moves);
        threadCom.putUpdate("zoom_in", location);
        if (!replaying) threadCom.putUpdate("send_notification", getMessage(player));
    }

    // Updates the UI at the end of a turn.
    // @param move the Move that has been played.
    private void updateUI(Move move) {
        threadCom.putUpdate("stop_timer", true);
        threadCom.putUpdate("update_log", move);
        if (model.getRounds().get(model.getRound()) && move.colour.equals(Colour.Black)) threadCom.putUpdate("update_log_message", "Mr X has been spotted at location " + model.getPlayerLocation(Colour.Black));
        threadCom.putUpdate("update_round", model.getRound());
        updateTickets(move.colour);
        threadCom.putUpdate("update_board", move);
        if (!aiGame) {
            wait(kAnimationWait);
            Integer target = getTarget(move);
            if (!move.colour.equals(Colour.Black)) {
                threadCom.putUpdate("zoom_in", target);
                wait(kMoveWait);
            }
        }
        threadCom.putUpdate("zoom_out", true);
        if (!aiGame) wait(kMoveWait);
    }

    // Returns the target of a Move.
    // @param move the Move for which to return the target.
    // @return the target of a Move.
    private Integer getTarget(Move move) {
        if (move instanceof MoveTicket) return ((MoveTicket) move).target;
        else if (move instanceof MoveDouble) return ((MoveTicket) ((MoveDouble) move).move2).target;
        else return null;
    }

    // Updates the PlayerTicketView with the current players Tickets.
    // @param player the player for whom the PlayerTicketView should update.
    private void updateTickets(Colour player) {
        List<Object> newTickets = new ArrayList<Object>();
        newTickets.add(player);
        Map<Ticket, Integer> tickets = ModelHelper.getTickets(player, model);
        newTickets.add(tickets);
        threadCom.putUpdate("update_tickets", newTickets);
    }

    // Pauses the game thread for the specified time in milliseconds.
    // @param milliseconds the time to pause the game thread for.
    private void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            System.err.println(e.getStackTrace());
        }
    }

    // Returns the message at the start of a turn.
    // @param player the player whose turn it is.
    // @return the message at the start of a turn.
    private String getMessage(Colour player) {
        if (player.equals(Colour.Black)) {
            return "Mr X's turn.";
        } else {
            return player.toString() + " Detective's turn.";
        }
    }

    // Returns the message at the end of a game.
    // @param players the Set of winning players.
    // @return the message at the end of a game.
    private String getWinningMessage(Set<Colour> players) {
        if (players.contains(Colour.Black)) return "Mr X wins!";
        else if (players.size() == 1) return "Detective wins!";
        else return "Detectives win!";
    }

    // Returns the tickets a player has.
    // @param colour the color of the player.
    // @return the tickets a player has.
    private Map<Route, Integer> getPlayerTicketsRoute(Colour colour) {
        Map<Route, Integer> tickets = new HashMap<Route, Integer>();
        tickets.put(Route.Taxi, model.getPlayerTickets(colour, Ticket.Taxi));
        tickets.put(Route.Bus, model.getPlayerTickets(colour, Ticket.Bus));
        tickets.put(Route.Underground, model.getPlayerTickets(colour, Ticket.Underground));
        tickets.put(Route.Boat, model.getPlayerTickets(colour, Ticket.Secret));
        return tickets;
    }

    /**
     * Saves the game.
     */
    public void saveGame() {
        if (!replaying && saveGame != null) fileAccess.saveGame(saveGame);
    }

    public void setAiMove(Move m) {
        aiMove = m;
    }

}
