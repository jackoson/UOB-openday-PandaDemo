package client.application;

import client.scotlandyard.*;

import java.util.*;
import java.io.Serializable;

/**
 * A class that represents the state of a game at any point in time.
 * Implements Serializable for easy game saving.
 */

public class SaveGame implements Serializable {
  
    private static final long serialVersionUID = 2919143291006499190L;
    
    private int numberOfPlayers;
    private String graphFileName;
    private String filename;
    private String gameName;
    private List<SaveMove> moves;
    private transient List<SaveMove> savedMoves;
    private int[] detectiveLocations;
    private int mrXLocation;
    
    /**
     * Blank constructor for the implementation of Serializable.
     */
    public SaveGame() {}
    
    /**
     * Constructs a new SaveGame object to remember the information about a game.
     *
     * @param numberOfPlayers the number of detectives in the game.
     * @param graphFileName the path to the graph file used in the game.
     * @param gameName the name of the game.
     */
    public SaveGame(int numberOfPlayers, String graphFileName, String gameName) {
        this.numberOfPlayers = numberOfPlayers;
        this.graphFileName = graphFileName;
        this.gameName = gameName;
        moves = new ArrayList<SaveMove>();
        
        long unixTime = System.currentTimeMillis() / 1000L;
        String savePath = gameName + "#" + unixTime + ".syg";
        this.filename = savePath;
    }
    
    /**
     * Sets the initial locations of the detectives.
     *
     * @param locations the array of locations of the detectives.
     */
    public void setDetectiveLocations (int[] locations) {
        this.detectiveLocations = locations;
    }
    
    /**
     * Sets the initial location of Mr X.
     *
     * @param location the initial location of Mr X.
     */
    public void setMrXLocation(int location) {
        this.mrXLocation = location;
    }
    
    /**
     * Returns the number of players in the game.
     *
     * @return the number of players in the game.
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }
    
    /**
     * Returns the file name for the graph file associated with the game.
     *
     * @return the file name for the graph file associated with the game.
     */
    public String getGraphFileName() {
        return graphFileName;
    }
    
    /**
     * Returns the file name for the game save.
     *
     * @return the file name for the game save.
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Returns the game name.
     *
     * @return the game name.
     */
    public String getGameName() {
        return gameName;
    }
    
    /**
     * Returns the initial locations of the detectives.
     *
     * @return the initial locations of the detectives.
     */
    public int[] getDetectiveLocations() {
        return detectiveLocations;
    }
    
    /**
     * Returns the initial location of Mr X.
     *
     * @return the initial location of Mr X.
     */
    public int getMrXLocation() {
        return mrXLocation;
    }
    
    /**
     * Returns true if there are saved moves available,
     * false otherwise.
     *
     * @return true if there are saved moves available,
     * false otherwise.
     */
    public boolean hasSavedMove() {
        if (savedMoves == null) {
            savedMoves = moves;
            moves = new ArrayList<SaveMove>();
        }
        if (savedMoves.size() > 0) return true;
        return false;
    }
    
    /**
     * Returns the next saved move in the game.
     *
     * @return the next saved move in the game.
     */
    public Move getSavedMove() {
        SaveMove sMove = savedMoves.get(0);
        Move move = sMove.move();
        savedMoves.remove(0);
        return move;
    }
    
    /**
     * Adds a Move to the save game.
     *
     * @param move the Move to be added to the save game.
     */
    public void addMove(Move move) {
        SaveMove saveMove = null;
        if (move instanceof MoveDouble) {
            saveMove = new SaveMoveDouble((MoveDouble)move);
        } else if (move instanceof MovePass) {
            saveMove = new SaveMovePass((MovePass)move);
        } else if (move instanceof MoveTicket) {
            saveMove = new SaveMoveTicket((MoveTicket)move);
        }
        if (saveMove != null) moves.add(saveMove);
    }
    
    // Base class to make Moves serializable.
    private class SaveMove implements Serializable {
      
        private static final long serialVersionUID = -396196279236881100L;
        
        public final String colour;
        
        /**
         * Constructs a new SaveMove object.
         *
         * @param colour the colour of the player to whom this move belongs.
         */
        public SaveMove(Colour colour) {
            this.colour = colour.toString();
        }
        
        /**
         * Empty constructor for the Serializable implementation.
         */
        public SaveMove() {
            this.colour = "none";
        }
        
        /**
         * Returns the move as a Move object.
         * Always null as moves must be a class which extends this.
         *
         * @return null.
         */
        public Move move() {
            return null;
        }
        
    }
    
    // Class to represent MovePass moves that is serializable.
    private class SaveMovePass extends SaveMove {
      
        private static final long serialVersionUID = -1628246866066100532L;
        
        /**
         * Constructs a new SaveMovePass object.
         *
         * @param move the MovePass to convert to a SaveMovePass.
         */
        public SaveMovePass(MovePass move) {
            super(move.colour);
        }
        
        /**
         * Returns the MovePass associated with this SaveMovePass.
         *
         * @return the MovePass associated with this SaveMovePass.
         */
        @Override
        public Move move() {
            return new MovePass(Colour.valueOf(super.colour));
        }
        
    }
    
    // Class to represent MoveDouble moves that is serializable.
    private class SaveMoveDouble extends SaveMove {
      
        private static final long serialVersionUID = -3883788055927126102L;
        
        private List<SaveMoveTicket> moves;
        
        /**
         * Constructs a new SaveMoveDouble object.
         *
         * @param move the MoveDouble to convert to a SaveMoveDouble.
         */
        public SaveMoveDouble(MoveDouble move) {
            super(move.colour);
            
            moves = new ArrayList<SaveMoveTicket>();
            for (Move m :move.moves) {
                MoveTicket mt = (MoveTicket)m;
                SaveMoveTicket newMove = new SaveMoveTicket(mt);
                moves.add(newMove);
            }
        }

        /**
         * Returns the MoveDouble associated with this SaveMoveDouble.
         *
         * @return the MoveDouble associated with this SaveMoveDouble.
         */
        @Override
        public Move move() {
            if (moves.size() < 2) return null;
            SaveMoveTicket move1 = moves.get(0);
            SaveMoveTicket move2 = moves.get(1);
            MoveDouble move = new MoveDouble(Colour.valueOf(super.colour), move1.move(), move2.move());
            return move;
        }
        
    }
    
    // Class to represent MoveTicket moves that is serializable.
    private class SaveMoveTicket extends SaveMove {
      
        private static final long serialVersionUID = 9187459597507865281L;
        
        private int target;
        private String ticket;
        
        /**
         * Constructs a new SaveMoveTicket object.
         *
         * @param move the MoveTicket to convert to a SaveMoveTicket.
         */
        public SaveMoveTicket(MoveTicket move) {
            super(move.colour);
            this.target = move.target;
            this.ticket = move.ticket.toString();
        }
        
        /**
         * Returns the MoveTicket associated with this SaveMoveTicket.
         *
         * @return the MoveTicket associated with this SaveMoveTicket.
         */
        @Override
        public Move move() {
            MoveTicket move = new MoveTicket(Colour.valueOf(super.colour), target, Ticket.valueOf(ticket));
            return move;
        }
        
    }
    
}