package client.view;

import client.scotlandyard.*;
import client.application.*;
import client.algorithms.*;
import client.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 * A view to display the game map, the players counters and get which node the users click on.
 */

public class BoardView extends JPanel implements MouseListener, MouseMotionListener {
  
    private static final long serialVersionUID = -4785796174751700452L;
  
    private BufferedImage map;
    private Map<Colour, BufferedImage> counters;
    private Map<Colour, Integer> locations;
    private KDTree tree;
    private ActionListener aListener;
    private boolean zoomed = false;
    private boolean drawASCII = false;
    private int x = 0, y = 0, startX, startY, minX, maxX = 0, minY, maxY = 0, scaledX = 0, scaledY = 0;
    private double scaleFactor;
    private FileAccess fileAccess;
    private List<Integer> routeHint = new ArrayList<Integer>();
    private Integer selectedNode = 0;
  
    /**
     * Constructs a new BoardView object.
     *
     * @param fileAccess the FileAccess object that contains all images.
     */
    public BoardView(FileAccess fileAccess) {
        this.fileAccess = fileAccess;
        addMouseListener(this);
        addMouseMotionListener(this);
        
        tree = new KDTree("/resources/pos.txt");
        this.aListener = null;
        this.map = fileAccess.getMap();
        this.counters = fileAccess.getCounters();
        locations = new HashMap<Colour, Integer>();
    }
    
    // Updates all constants to do with image scaling and keeping aspect ratio.
    // @param size the size of the window.
    private void updateConstants(Dimension size) {
        minX = size.width - map.getWidth();
        minY = size.height - map.getHeight();
        double imgRatio = (double) map.getWidth() / (double) map.getHeight();
        double windowRatio = (double) size.width / (double) size.height;
        if (windowRatio < imgRatio) {
            scaledX = size.width;
            scaledY = scaleY(size.width);
        } else {
            scaledX = scaleX(size.height);
            scaledY = size.height;
        }
        if (zoomed) scaleFactor = 1.0;
        else scaleFactor = (double) scaledX / (double) map.getWidth();
        correctCoordinates();
    }
    
    // Returns the scaled height of the image.
    // @param scaleX the width of the image.
    // @return the scaled height of the image.
    private int scaleY(int scaleX) {
        double ratio = ((double) map.getHeight() / (double) map.getWidth());
        return (int) (scaleX * ratio);
    }
    
    // Returns the scaled width of the image.
    // @param scaleX the height of the image.
    // @return the scaled width of the image.
    private int scaleX(int scaleY) {
        double ratio = ((double) map.getWidth() / (double) map.getHeight());
        return (int) (scaleY * ratio);
    }
    
    /**
     * Updates the view when the size of the container changes. 
     * This is used to keep the aspect ratio constant.
     *
     * @param e the ComponentEvent when the container size changes.
     */
    public void updateDisplay(ComponentEvent e) {
        Dimension size = this.getSize();
        updateConstants(size);
        setPreferredSize(new Dimension(scaledX, scaledY));
        repaint();
    }
 
    /**
     * Draws the map and player's counters to the view.
     *
     * @param g0 the Graphics object to draw to.
     */
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        
        updateConstants(getSize());
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        if (!zoomed) g.drawImage(map, 0, 0, scaledX, scaledY, null);
        else g.drawImage(map, null, x, y);
        setBackground(UIManager.getColor("Panel.background"));
        drawCounters(g, locations);
        if (routeHint.size() > 0) drawRoute(g, routeHint);
        if (selectedNode > 0) drawSelectedNode(g, selectedNode);
    }
    
    // Draws the player's counters.
    // @param g the Graphics object to draw to.
    // @param locations the Map containing the player's locations.
    private void drawCounters(Graphics2D g, Map<Colour, Integer> locations) {
        Map<Integer, Dimension> positions = fileAccess.getPositions();
        int size = (int) ((double) counters.get(Colour.Black).getWidth() * scaleFactor);
        int offset = (int) ((double) size / 2.0);
        for (Map.Entry<Colour, Integer> entry : locations.entrySet()) {
            if (entry.getValue() != 0) {
                Dimension d = positions.get(entry.getValue());
                d = transformPointForMap(d);
                int xPos = d.width - offset;
                int yPos = d.height - offset;
                g.drawImage(counters.get(entry.getKey()), xPos, yPos, size, size, null);
            }
        }
    }
    
    /**
     * Draws a route between locations
     *
     * @param g the Graphics object to draw to.
     * @param route list of locations to draw route between
     */
    private void drawRoute(Graphics2D g, List<Integer> route){
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<Integer, Dimension> positions = fileAccess.getPositions();
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(Math.max((int)(4.0 * (scaleFactor)),2)));
        int radius = (int)(58.0 * scaleFactor);
        for (int i = 0; i < route.size()-1; i++) {
            Dimension startPos = transformPointForMap(positions.get(route.get(i)));
            Dimension endPos = transformPointForMap(positions.get(route.get(i+1)));
            double angle = Math.atan2(startPos.width - endPos.width, startPos.height - endPos.height);
            int circleOffsetX = (int)(Math.sin(angle)*radius/2);
            int circleOffsetY = (int)(Math.cos(angle)*radius/2);
            
            g.drawLine(startPos.width - circleOffsetX, startPos.height - circleOffsetY, endPos.width  + circleOffsetX, endPos.height + circleOffsetY);
            g.drawOval(startPos.width - radius/2, startPos.height - radius/2, radius, radius);
        }
        Dimension startPos = transformPointForMap(positions.get(route.get(route.size()-1)));
        g.drawOval(startPos.width - radius/2, startPos.height - radius/2, radius, radius);
    }
    
    // Draws a circle around the currently selected node.
    // @param g the Graphics object to draw to.
    // @param location the location of the node to be selected.
    private void drawSelectedNode(Graphics2D g, Integer location) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<Integer, Dimension> positions = fileAccess.getPositions();
        Dimension d = positions.get(location);
        d = transformPointForMap(d);
        
        int radius = (int)(50.0 * scaleFactor);;
        g.setStroke(new BasicStroke(Math.max((int)(4.0 * (scaleFactor)),2)));
        g.setColor(new Color(20, 155, 247));
        g.drawOval(d.width - (radius/2), d.height - (radius/2), radius, radius);
    }
    
    // Transforms a point based on the map scale.
    // @param d the point to be transformed.
    private Dimension transformPointForMap(Dimension d) {
        int xPos = x + (int) Math.round((double) d.width * scaleFactor);
        int yPos = y + (int) Math.round((double) d.height * scaleFactor);
        return new Dimension(xPos, yPos);
    }
    
    /**
     * Highlists a node or removes highlight if location
     * equals zero.
     *
     * @param location the location of the node to be
     * highlighted.
     */
    public void highlightNode(Integer location) {
        selectedNode = location;
        repaint();
    }
    
    /**
     * Zooms in and centers the node in the view.
     * 
     * @param location the location of the node to zoom to.
     */
    public void zoomToNode(Integer location) {
        Dimension loc = tree.getNodeLocation(location);
        zoomToCoordinates(loc.width, loc.height, true);
    }
    
    /**
     * Zooms out so the map fits in the view.
     */
    public void zoomOut() {
        zoomToCoordinates(0, 0, false);
    }
    
    /**
     * Updates the locations of the players and redraws the view.
     *
     * @param players the List of locations of the player's.
     */
    public void update(List<GamePlayer> players) {
        for (GamePlayer player : players) {
            locations.put(player.colour(), player.location());
        }
        repaint();
    }
    
    /**
     * Updates the locations of the players and redraws the view.
     * This uses a Move to get the information.
     *
     * @param move the Move containing the updated location of the player.
     */
    public void update(Move move) {
        if (move instanceof MoveTicket) {
            MoveTicket moveTicket = (MoveTicket) move;
            locations.remove(moveTicket.colour);
            locations.put(moveTicket.colour, moveTicket.target);
        } else if (move instanceof MoveDouble) {
            MoveTicket moveTicket = (MoveTicket) ((MoveDouble) move).moves.get(1);
            locations.remove(moveTicket.colour);
            locations.put(moveTicket.colour, moveTicket.target);
        }
        repaint();
    }
    
    // Corrects the coordinates so the map can only be panned in a set area.
    private void correctCoordinates() {
        if (x < minX) x = minX;
        else if (x > maxX) x = maxX;
        if (y < minY) y = minY;
        else if (y > maxY) y = maxY;
    }
    
    // Zooms the map and centers the coordinates in the view.
    // @param xPos the x coordinate to be the center of the view.
    // @param yPos the y coordinate to be the center of the view.
    // @param zoomedIn whether we want to zoom in or out.
    private void zoomToCoordinates(int xPos, int yPos, boolean zoomedIn) {
        Dimension size = getSize();
        if (!zoomedIn) {
            x = 0;
            y = 0;
        } else {
            x = (int) ((double) size.width / 2.0) - xPos;
            y = (int) ((double) size.height / 2.0) - yPos;
            correctCoordinates();
        }
        zoomed = zoomedIn;
        updateDisplay(null);
    }
    
    /**
     * Adds the specified ActionListener to recieve when the user clicks on a node.
     * If listener listener is null, no action is performed.
     * 
     * @param listener the listener to be added to the view.
     */
    public void setActionListener(ActionListener listener) {
        aListener = listener;
    }
    
    /**
     * Sends an ActionEvent to the specified ActionListener when the user clicks on a node.
     * 
     * @param e the MouseEvent containing the location of the click.
     */
    public void mouseClicked(MouseEvent e) {
        double clickX = e.getX();
        double clickY = e.getY();
        int xPos = (int) Math.round(clickX / scaleFactor) - x;
        int yPos = (int) Math.round(clickY / scaleFactor) - y;
        if (e.getClickCount() == 2) {
            Dimension viewSize = getSize();
            double offsetX = (viewSize.width / 2) - clickX;
            double offsetY = (viewSize.height / 2) - clickY;
            zoomToCoordinates(xPos + (int)offsetX, yPos+ (int)offsetY, !zoomed);
        } else if (e.getClickCount() == 1) {
            int point = tree.getNode(xPos, yPos);
            int offset = (int) Math.round(44.0 * scaleFactor);
            Dimension d = tree.getNodeLocation(point);
            if (((d.width - offset) < xPos && xPos < (d.width + offset))
                  && ((d.height - offset) < yPos && yPos < (d.height + offset))
                  && aListener != null) {
                aListener.actionPerformed(new ActionEvent(new Integer(point), 0, "node"));
            }
        }
    }
    
    /**
     * Draws the route to be displayed to the user.
     *
     * @param route the List of nodes in the route to be displayed.
     */
    public void setRouteHint(List<Integer> route) {
        routeHint = route;
        repaint();
    }
    
    /**
     * Sets the starting position of the view for a drag.
     *
     * @param e the MouseEvent containing the location of the click.
     */
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
    }
    
    /**
     * Sets the placement of the map during a drag event.
     *
     * @param e the MouseEvent containing the current location of the cursor.
     */
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && zoomed) {
            x -= startX - e.getX();
            y -= startY - e.getY();
            correctCoordinates();        
            startX = e.getX();
            startY = e.getY();
            repaint();
        }
    }
    
    /**
     * Sets the cursor to the MOVE_CURSOR when the mouse enters the view and
     * the map is zoomed in.
     *
     * @param e the MouseEvent containing the current location of the mouse.
     */
    public void mouseEntered(MouseEvent e) {
        if (zoomed) setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }
    
    /**
     * Sets the cursor to the DEFAULT_CURSOR when the mouse exits the view.
     *
     * @param e the MouseEvent containing the current location of the mouse.
     */
    public void mouseExited(MouseEvent e) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * Sets the cursor to the MOVE_CURSOR when the map is zoomed in, sets it 
     * to the DEFAULT_CURSOR when the map is not zoomed in.
     *
     * @param e the MouseEvent containing the current location of the mouse.
     */
    public void mouseMoved(MouseEvent e) {
        if (zoomed) setCursor(new Cursor(Cursor.MOVE_CURSOR));
        else setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    /**
     * Unused method from the MouseListener interface.
     */
    public void mouseReleased(MouseEvent e) {}
    
}