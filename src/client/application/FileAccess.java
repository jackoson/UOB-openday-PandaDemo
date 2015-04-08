package client.application;

import client.scotlandyard.*;
import client.algorithms.*;

import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.security.*;
import java.net.*;
import java.awt.*;

/**
 * A class to handle the IO operations to get images and save/load games.
 */

public class FileAccess {

    private BufferedImage map;
    private BufferedImage notify;
    private BufferedImage circle;
    private BufferedImage setupBackground;
    private Map<Ticket, BufferedImage> tickets;
    private Map<Ticket, BufferedImage> ticketsBlank;
    private Map<Ticket, BufferedImage> ticketsSmall;
    private Map<Colour, BufferedImage> counters;
    private Map<Colour, BufferedImage> players;
    private Map<Integer, Dimension> mapPositions;
    private List<BufferedImage> startTickets;
    private BufferedImage setupImage;
    private BufferedImage warningIcon;
    
    /**
     * Constructs a new FileAccess object.
     * This loads all images to memory so they can be quickly accessed later.
     */
    public FileAccess() {
        tickets = new HashMap<Ticket, BufferedImage>();
        ticketsBlank = new HashMap<Ticket, BufferedImage>();
        ticketsSmall = new HashMap<Ticket, BufferedImage>();
        counters = new HashMap<Colour, BufferedImage>();
        players = new HashMap<Colour, BufferedImage>();
        mapPositions = makePositions();
        startTickets = new ArrayList<BufferedImage>();
        try {
            map = ImageIO.read(this.getClass().getResource("/resources/map.jpg"));
            notify = ImageIO.read(this.getClass().getResource("/resources/notify.png"));
            circle = ImageIO.read(this.getClass().getResource("/resources/badge.png"));
            setupBackground = ImageIO.read(this.getClass().getResource("/resources/setupBackground.png"));
            warningIcon = ImageIO.read(this.getClass().getResource("/resources/warningIcon.jpg"));
            
            counters.put(Colour.Black, ImageIO.read(this.getClass().getResource("/resources/counters/black_counter.png")));
            counters.put(Colour.Blue, ImageIO.read(this.getClass().getResource("/resources/counters/blue_counter.png")));
            counters.put(Colour.Yellow, ImageIO.read(this.getClass().getResource("/resources/counters/yellow_counter.png")));
            counters.put(Colour.Red, ImageIO.read(this.getClass().getResource("/resources/counters/red_counter.png")));
            counters.put(Colour.Green, ImageIO.read(this.getClass().getResource("/resources/counters/green_counter.png")));
            counters.put(Colour.White, ImageIO.read(this.getClass().getResource("/resources/counters/white_counter.png")));
            
            players.put(Colour.Black, ImageIO.read(this.getClass().getResource("/resources/players/black_player.png")));
            players.put(Colour.Blue, ImageIO.read(this.getClass().getResource("/resources/players/blue_player.png")));
            players.put(Colour.Yellow, ImageIO.read(this.getClass().getResource("/resources/players/yellow_player.png")));
            players.put(Colour.Red, ImageIO.read(this.getClass().getResource("/resources/players/red_player.png")));
            players.put(Colour.Green, ImageIO.read(this.getClass().getResource("/resources/players/green_player.png")));
            players.put(Colour.White, ImageIO.read(this.getClass().getResource("/resources/players/white_player.png")));
            
            tickets.put(Ticket.Taxi, ImageIO.read(this.getClass().getResource("/resources/tickets/taxi.png")));
            tickets.put(Ticket.Bus, ImageIO.read(this.getClass().getResource("/resources/tickets/bus.png")));
            tickets.put(Ticket.Underground, ImageIO.read(this.getClass().getResource("/resources/tickets/underground.png")));
            tickets.put(Ticket.SecretMove, ImageIO.read(this.getClass().getResource("/resources/tickets/secret.png")));
            tickets.put(Ticket.DoubleMove, ImageIO.read(this.getClass().getResource("/resources/tickets/double.png")));
            
            ticketsBlank.put(Ticket.Taxi, ImageIO.read(this.getClass().getResource("/resources/tickets/taxi_blank.png")));
            ticketsBlank.put(Ticket.Bus, ImageIO.read(this.getClass().getResource("/resources/tickets/bus_blank.png")));
            ticketsBlank.put(Ticket.Underground, ImageIO.read(this.getClass().getResource("/resources/tickets/underground_blank.png")));
            ticketsBlank.put(Ticket.SecretMove, ImageIO.read(this.getClass().getResource("/resources/tickets/secret_blank.png")));
            
            ticketsSmall.put(Ticket.Taxi, ImageIO.read(this.getClass().getResource("/resources/tickets/taxi_small.png")));
            ticketsSmall.put(Ticket.Bus, ImageIO.read(this.getClass().getResource("/resources/tickets/bus_small.png")));
            ticketsSmall.put(Ticket.Underground, ImageIO.read(this.getClass().getResource("/resources/tickets/underground_small.png")));
            ticketsSmall.put(Ticket.SecretMove, ImageIO.read(this.getClass().getResource("/resources/tickets/secret_small.png")));
            ticketsSmall.put(Ticket.DoubleMove, ImageIO.read(this.getClass().getResource("/resources/tickets/double_small.png")));
            
            startTickets.add(ImageIO.read(this.getClass().getResource("/resources/tickets/start.png")));
            startTickets.add(ImageIO.read(this.getClass().getResource("/resources/tickets/start_blank.png")));
            
            setupImage = ImageIO.read(this.getClass().getResource("/resources/setup_image.png"));
        } catch (Exception e) {
            System.err.println("Error retrieving images :" + e.getStackTrace());
            System.exit(1);
        }
        
        //Create save folder
        File dir = new File (jarPath() + "/SavedGames/");
        if (!dir.exists()) {
            try {
                dir.mkdir();
                System.err.println(jarPath() + "/SavedGames/");
            } catch (SecurityException e) {
                System.err.println(e);
            }
        }
    }
    
    /**
     * Returns the map image.
     *
     * @param size the size of the image to be generated.
     * @return the map image.
     */
    public BufferedImage getSetupImage(Dimension size) {
        ImgToASCII ascii = new ImgToASCII(setupImage, size);
        ascii.convert();
        return ascii.toImage();
    }
    
    /**
     * Returns the map image.
     *
     * @return the map image.
     */
    public BufferedImage getMap() {
        return map;
    }
    
    /**
     * Returns the NotifyView background image.
     *
     * @return the background image.
     */
    public BufferedImage getNotify() {
        return notify;
    }
    
    /**
     * Returns the PlayersView background circle image.
     *
     * @return the background circle image.
     */
    public BufferedImage getCircle() {
        return circle;
    }
    
    /**
     * Returns the SetUpView background image.
     *
     * @return the background image.
     */
    public BufferedImage getSetupBackground() {
        return setupBackground;
    }
    
    /**
     * Returns the warning icon.
     *
     * @return the warning icon.
     */
    public ImageIcon getWarningIcon() {
        return new ImageIcon(warningIcon);
    }
    
    /**
     * Returns a List the start Ticket images.
     *
     * @return a List the start Ticket images.
     */
    public List<BufferedImage> getStartTickets() {
        return startTickets;
    }
    
    /**
     * Returns a Map of the Ticket images.
     *
     * @return the Map of Ticket images.
     */
    public Map<Ticket, BufferedImage> getTickets() {
        return tickets;
    }
    
    /**
     * Returns a Map of the back of the Ticket images.
     *
     * @return the Map of the back of the Ticket images.
     */
    public Map<Ticket, BufferedImage> getTicketsBlank() {
        return ticketsBlank;
    }
    
    /**
     * Returns a Map of small Ticket images.
     *
     * @return the Map of small Ticket images.
     */
    public Map<Ticket, BufferedImage> getTicketsSmall() {
        return ticketsSmall;
    }
    
    /**
     * Returns a Map of the counter images.
     *
     * @return the Map of counter images.
     */
    public Map<Colour, BufferedImage> getCounters() {
        return counters;
    }
    
    /**
     * Returns a Map of the PlayerView background images.
     *
     * @return the Map of background images.
     */
    public Map<Colour, BufferedImage> getPlayers() {
        return players;
    }
    
    /**
     * Returns a List of the saved game names and paths.
     *
     * @return the List of saved games.
     */
    public List<String> savedGames() {
        List<String> games = new ArrayList<String>();
        File dir = new File (jarPath() + "/SavedGames/");
        for (File file : dir.listFiles()) {
            String name = file.getName();
            if (!file.isDirectory() && name.endsWith(".syg")) {
                games.add(name);
            }
        }
        return games;
    }
    
    /**
     * Creates a Map of the locations and their coordinates.
     *
     * @return the Map of locations
     */
    public Map<Integer, Dimension> makePositions() {
        Map<Integer, Dimension> positions = new HashMap<Integer, Dimension>();
        Scanner scanner = null;
        try {
            scanner = new Scanner(this.getClass().getResourceAsStream("/resources/pos.txt"));
        } catch (Exception e) {
            System.err.println("Error getting graph position file :" + e.getStackTrace());
            System.exit(1);
        }
        
        String topLine = scanner.nextLine();
        int numberOfNodes = Integer.parseInt(topLine);
        for (int i = 0; i < numberOfNodes; i++) {
            String line = scanner.nextLine();
            String[] parts = line.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int loc = Integer.parseInt(parts[0]);
            positions.put(loc, new Dimension(x,y));
        }
        return positions;
    }
    
    /**
     * Returns a Map of the locations and their coordinates.
     *
     * @return the Map of locations
     */
    public Map<Integer, Dimension> getPositions() {
        return mapPositions;
    }
    
    // Returns the path to the directory the jar file is being run from.
    // @return the path to the directory the jar file is being run from.
    private String jarPath() {
        try {
            ProtectionDomain domain = FileAccess.class.getProtectionDomain();
            CodeSource codeSource = domain.getCodeSource();
            URL url = codeSource.getLocation();
            String path = url.toURI().getPath();
            return path.substring(0, path.lastIndexOf('/'));
        } catch (URISyntaxException e) {
            System.err.println(e);
        }
        return null;
    }
    
    /**
     * Creates a new save game.
     *
     * @param game the name of the game to be saved.
     */
    public void saveGame(SaveGame game) {
        try {
            String filename = game.getFilename();
            FileOutputStream file = new FileOutputStream(jarPath() + "/SavedGames/" + filename);
            BufferedOutputStream buffer = new BufferedOutputStream(file);
            ObjectOutputStream save = new ObjectOutputStream(buffer);
            save.writeObject(game);
            save.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns a save game.
     *
     * @param filename the name of the game to be loaded.
     * @return the SaveGame object of the game.
     */
    public SaveGame loadGame(String filename) {
        try {
            InputStream file = new FileInputStream(jarPath() + "/SavedGames/" + filename);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                SaveGame game = (SaveGame) input.readObject();
                return game;
            } catch (ClassNotFoundException e) {
                System.err.println(e);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

}