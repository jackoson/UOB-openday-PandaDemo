package client.view;

import scotlandyard.*;
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

public class BoardView extends AnimatablePanel implements MouseListener, MouseMotionListener {
  
    private static final long serialVersionUID = -4785796174751700452L;
    
    private final static int border = 1800;
    private final static int halfBorder = 1000;
  
    private BufferedImage map;
    private Map<Colour, BufferedImage> counters;
    private Map<Colour, Point> locations;
    private KDTree tree;
    private ActionListener aListener;
    private boolean drawASCII = false;
    private Point viewPos = new Point(0, 0), mouseDownPos = new Point(0, 0), mouseDownViewPos = new Point(0, 0);
    private Dimension mapSize;
    private double scaleFactor = 1.0;
    private FileAccess fileAccess;
    private List<Integer> routeHint = new ArrayList<Integer>();
    private Integer selectedNode = 0;
    private List<CounterAnimator> animators;
    private BoardAnimator boardAnimator = null;
    
    private Set<Move> validMoves;
    private BufferedImage cursorImage;
    private Point cursorPos;
  
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
        mapSize = new Dimension(map.getWidth(), map.getHeight());
        this.counters = fileAccess.getCounters();
        locations = new HashMap<Colour, Point>();
        animators = new ArrayList<CounterAnimator>();
        validMoves = new HashSet<Move>();
    }
    
    /**
     * Updates the view when the size of the container changes. 
     * This is used to keep the aspect ratio constant.
     *
     * @param e the ComponentEvent when the container size changes.
     */
    public void updateDisplay(ComponentEvent e) {
        //?
        Dimension size = getSize();
        if (scaleFactor != 1.0) scaleFactor = fitScaleFactor(size);
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
        
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        updateAnimatedCounter();
        updateAnimatedBoard();
        
        g.drawImage(map, -viewPos.x, -viewPos.y, (int)(mapSize.getWidth() * scaleFactor), (int)(mapSize.getHeight() * scaleFactor), null);
        
        drawCounters(g, locations);
        if (routeHint.size() > 0) drawRoute(g, routeHint);
        if (selectedNode > 0) drawSelectedNode(g, selectedNode);
        
        if (cursorImage != null) g.drawImage(cursorImage, null, cursorPos.x + 10, cursorPos.y + 10);
    }
    
    // Draws the player's counters.
    // @param g the Graphics object to draw to.
    // @param locations the Map containing the player's locations.
    private void drawCounters(Graphics2D g, Map<Colour, Point> locations) {
        int size = (int) ((double) counters.get(Colour.Black).getWidth() * scaleFactor);
        int offset = (int) ((double) size / 2.0);
        for (Map.Entry<Colour, Point> entry : locations.entrySet()) {
            Point d = transformPointForMap(entry.getValue());
            int xPos = d.x - offset;
            int yPos = d.y - offset;
            drawCounter(g, xPos, yPos, size, entry.getKey());
        }
    }
    
    // Draws the specified player's counter.
    // @param g the Graphics object to draw to.
    // @param x the x coordinate to draw to.
    // @param y the y coordinate to draw to.
    // @param size the diameter of the scaled counter.
    // @param colour the Colour of the player whose counter is to be drawn.
    private void drawCounter(Graphics2D g, int x, int y, int size, Colour colour) {
        g.drawImage(counters.get(colour), x, y, size, size, null);
    }
    
    /**
     * Draws a route between locations
     *
     * @param g the Graphics object to draw to.
     * @param route list of locations to draw route between
     */
    private void drawRoute(Graphics2D g, List<Integer> route){
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<Integer, Point> positions = fileAccess.getPositions();
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(Math.max((int)(4.0 * (scaleFactor)),2)));
        int radius = (int)(58.0 * scaleFactor);
        for (int i = 0; i < route.size()-1; i++) {
            Point startPos = transformPointForMap(positions.get(route.get(i)));
            Point endPos = transformPointForMap(positions.get(route.get(i+1)));
            double angle = Math.atan2(startPos.x - endPos.x, startPos.y - endPos.y);
            int circleOffsetX = (int)(Math.sin(angle)*radius/2);
            int circleOffsetY = (int)(Math.cos(angle)*radius/2);
            
            g.drawLine(startPos.x - circleOffsetX, startPos.y - circleOffsetY, endPos.x  + circleOffsetX, endPos.y + circleOffsetY);
            g.drawOval(startPos.x - radius/2, startPos.y - radius/2, radius, radius);
        }
        Point startPos = transformPointForMap(positions.get(route.get(route.size()-1)));
        g.drawOval(startPos.x - radius/2, startPos.y - radius/2, radius, radius);
    }
    
    // Draws a circle around the currently selected node.
    // @param g the Graphics object to draw to.
    // @param location the location of the node to be selected.
    private void drawSelectedNode(Graphics2D g, Integer location) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Map<Integer, Point> positions = fileAccess.getPositions();
        Point p = positions.get(location);
        p = transformPointForMap(p);
        
        int radius = (int)(50.0 * scaleFactor);;
        g.setStroke(new BasicStroke(Math.max((int)(4.0 * (scaleFactor)),2)));
        g.setColor(new Color(20, 155, 247));
        g.drawOval(p.x - (radius/2), p.y - (radius/2), radius, radius);
    }
    
    // Transforms a point based on the map scale.
    // @param d the point to be transformed.
    private Point transformPointForMap(Point d) {
        int xPos = (int)unscalePoint(d.x) - viewPos.x;
        int yPos = (int)unscalePoint(d.y) - viewPos.y;
        return new Point(xPos, yPos);
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
        Point loc = tree.getNodeLocation(location);
        zoomToCoordinates(loc.x, loc.y, true);
    }
    
    /**
     * Zooms out so the map fits in the view.
     */
    public void zoomOut() {
        zoomToCoordinates((int) (mapSize.getWidth() / 2.0) - halfBorder, (int) (mapSize.getHeight() / 2.0) - halfBorder, false);
    }
    
    /**
     * Updates the locations of the players and redraws the view.
     *
     * @param players the List of locations of the player's.
     */
    public void update(List<GamePlayer> players) {
        Map<Integer, Point> positions = fileAccess.getPositions();
        for (GamePlayer player : players) {
            if (player.location() == 0) {
                locations.put(player.colour(), new Point(-550, 1463));
            } else {
                locations.put(player.colour(), positions.get(player.location()));
            }
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
            animate(moveTicket);
        } else if (move instanceof MoveDouble) {
            MoveTicket moveTicket = (MoveTicket) ((MoveDouble) move).move2;
            animate(moveTicket);
        }
    }
    
    // Animates the movement of a counter.
    // @param moveTicket the MoveTicket containing the Move to be animated.
    private void animate(MoveTicket moveTicket) {
        Point end = null;
        Point start = locations.get(moveTicket.colour);
        if (moveTicket.target == 0) {
            end = new Point(-100, 100);
        } else {
            end = fileAccess.getPositions().get(moveTicket.target);
        }
        AnimatablePanel.Animator xAnimator = createAnimator(start.getX(), end.getX(), 1.0);
        xAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        AnimatablePanel.Animator yAnimator = createAnimator(start.getY(), end.getY(), 1.0);
        yAnimator.setEase(AnimatablePanel.AnimationEase.EASE_IN_OUT);
        animators.add(new CounterAnimator(moveTicket.colour, xAnimator, yAnimator));
    }
    
    // Updates the position of a counter being animated.
    private void updateAnimatedCounter() {
        for (CounterAnimator animator : animators) {
            locations.remove(animator.counter);
            Point current = new Point(animator.xAnimator.value().intValue(), animator.yAnimator.value().intValue());
            locations.put(animator.counter, current);
        }
    }
    
    /**
     * Resets the list of AnimatablePanel.Animators.
     */
    @Override
    public void animationCompleted() {
        if (Math.round(scaleFactor * 1000) / 1000 == 1.0) scaleFactor = 1.0;
        boardAnimator = null;
        animators.clear();
    }
    
    // Zooms the map and centers the coordinates in the view.?
    // @param xPos the x coordinate to be the center of the view.
    // @param yPos the y coordinate to be the center of the view.
    // @param zoomedIn whether we want to zoom in or out.
    private void zoomToCoordinates(int xPos, int yPos, boolean zoomIn) {
        Dimension size = getSize();
        double newScaleFactor = 1.0;
        if (!zoomIn) newScaleFactor = fitScaleFactor(size);
        
        double oldSF = scaleFactor;
        scaleFactor = newScaleFactor;
        
        double startX = viewPos.getX();
        double startY = viewPos.getY();
        double finalX = unscalePoint(xPos) - ((double) size.width / 2.0);
        double finalY = unscalePoint(yPos) - ((double) size.height / 2.0) + 20;
        Point p = adjustForBounds(new Point((int)finalX, (int)finalY));
        finalX = p.getX();
        finalY = p.getY();
        scaleFactor = oldSF;
        
        AnimatablePanel.AnimationEase ease = AnimatablePanel.AnimationEase.EASE_IN_OUT;
        double duration = 0.6;
        AnimatablePanel.Animator scaleAnimator = createAnimator(scaleFactor, newScaleFactor, duration);
        scaleAnimator.setEase(ease);
        AnimatablePanel.Animator xAnimator = createAnimator(startX, finalX, duration);
        xAnimator.setEase(ease);
        AnimatablePanel.Animator yAnimator = createAnimator(startY, finalY, duration);
        yAnimator.setEase(ease);
        boardAnimator = new BoardAnimator(scaleAnimator, xAnimator, yAnimator);
    }
    
    private void updateAnimatedBoard() {
        if (boardAnimator == null) return;
        scaleFactor = boardAnimator.scaleAnimator.value();
        viewPos.x = boardAnimator.xAnimator.value().intValue();
        viewPos.y = boardAnimator.yAnimator.value().intValue();
    }
    
    //?
    private double fitScaleFactor(Dimension size) {
        double mapRatio = mapSize.getWidth() / mapSize.getHeight();
        double viewRatio = size.getWidth() / (size.getHeight() - 40);
        
        if (viewRatio > mapRatio) return (size.getHeight() - 40) / (mapSize.getHeight() - border);//
        else return size.getWidth() / (mapSize.getWidth() - border);//
    }
    
    //?
    private Point adjustForBounds(Point point) {
        
        Dimension size = getSize();
        int xDiff = (int)((mapSize.getWidth() * scaleFactor) - size.getWidth());
        int yDiff = (int)((mapSize.getHeight() * scaleFactor) - (size.getHeight() - 40));
        
        int boundBorder = halfBorder - 100;
        
        if (xDiff < 0) point.x = xDiff/2;
        else if (point.x < boundBorder && scaleFactor == 1.0) point.x = boundBorder;
        else if (point.x < 0) point.x = 0;
        else if (point.x > (xDiff - boundBorder) && scaleFactor == 1.0) point.x = (xDiff - boundBorder);
        else if (point.x > xDiff) point.x = xDiff;
        
        if (yDiff < 0) point.y = yDiff/2;
        else if (point.y < (halfBorder - 100) && scaleFactor == 1.0) point.y = (halfBorder - 100);
        else if (point.y < 0) point.y = 0;
        else if (point.y > (yDiff - boundBorder) && scaleFactor == 1.0) point.y = (yDiff - boundBorder);
        else if (point.y > yDiff) point.y = yDiff;
        
        return point;
    }
    
    //?
    private int scalePoint(int pos) {
        return (int)((double)pos / scaleFactor) - halfBorder;//
    }
    
    //?
    private int unscalePoint(int pos) {
        return (int)((double)(pos + halfBorder) * scaleFactor);//
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
        int xPos = scalePoint(e.getX() + viewPos.x);
        int yPos = scalePoint(e.getY() + viewPos.y);
        if (e.getClickCount() == 2) {
            boolean zOut = scaleFactor == 1.0;
            if (zOut) zoomOut();
            else zoomToCoordinates(xPos, yPos, !zOut);
        } else if (e.getClickCount() == 1) {
            int point = tree.getNode(xPos, yPos);
            int offset = (int) Math.round(48.0 * scaleFactor);
            Point d = tree.getNodeLocation(point);
            if (((d.x - offset) < xPos && xPos < (d.x + offset))
                  && ((d.y - offset) < yPos && yPos < (d.y + offset))
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
        BoardView.this.grabFocus();
        mouseDownPos.x = e.getX();
        mouseDownPos.y = e.getY();
        mouseDownViewPos.x = viewPos.x;
        mouseDownViewPos.y = viewPos.y;
    }
    
    /**
     * Sets the placement of the map during a drag event.
     *
     * @param e the MouseEvent containing the current location of the cursor.
     */
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            //Scale offset and apply to viewpos
            int offsetX = e.getX() - mouseDownPos.x;
            int offsetY = e.getY() - mouseDownPos.y;
            viewPos.x = mouseDownViewPos.x - offsetX;//might want to scale viewpos as well
            viewPos.y = mouseDownViewPos.y - offsetY;
            viewPos = adjustForBounds(viewPos);//?
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
        if (scaleFactor == 1.0) setCursor(new Cursor(Cursor.MOVE_CURSOR));
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
        if (scaleFactor == 1.0) setCursor(new Cursor(Cursor.MOVE_CURSOR));
        else setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        
        int xPos = scalePoint(e.getX() + viewPos.x);
        int yPos = scalePoint(e.getY() + viewPos.y);

        int point = tree.getNode(xPos, yPos);
        int offset = (int) Math.round(25.0 * scaleFactor);
        Point d = tree.getNodeLocation(point);
        if (((d.x - offset) < xPos && xPos < (d.x + offset))
            && ((d.y - offset) < yPos && yPos < (d.y + offset))) {
            cursorPos = e.getPoint();
            Set<Ticket> validTickets = getValidTickets(point);
            cursorImage = fileAccess.getCursors().get(validTickets);
        } else {
            cursorImage = null;
        }
        repaint();
    }
    
    // Returns the Set of Tickets for which you can use to get the specified node.
    // @param point the node for which to get the Tickets for.
    // @return the Set of Tickets for which you can use to get the specified node.
    private Set<Ticket> getValidTickets(int point) {
        Set<Ticket> tickets = new HashSet<Ticket>();
        for (Move move : validMoves) {
            if (move instanceof MoveTicket && ((MoveTicket) move).target == point) {
                tickets.add(((MoveTicket) move).ticket);
            } else if (move instanceof MoveDouble && ((MoveTicket) ((MoveDouble)move).move2).target == point) {
                //tickets.add(move.moves.get(1).ticket);?
            }
        }
        return tickets;
    }
    
    /**
     * Updates the List of valid Moves.
     *
     * @param validMoves the new List of valid Moves.
     */
    public void updateValidMoves(Set<Move> validMoves) {
        this.validMoves = validMoves;
    }
    
    /**
     * Unused method from the MouseListener interface.
     */
    public void mouseReleased(MouseEvent e) {}
    
    // A class to help animate counters.
    private class CounterAnimator {
        
        /**
         * The AnimatablePanel.Animator for the x coordinate.
         */
        public AnimatablePanel.Animator xAnimator;
        
        /**
         * The AnimatablePanel.Animator for the y coordinate.
         */
        public AnimatablePanel.Animator yAnimator;
        
        /**
         * The Colour of the counter to be animated.
         */
        public Colour counter;
        
        /**
         * Constructs a new CounterAnimator object.
         *
         * @param counter the counter to be animated.
         * @param xAnimator the AnimatablePanel.Animator for the x coordinate.
         * @param yAnimator the AnimatablePanel.Animator for the y coordinate.
         */
        public CounterAnimator(Colour counter, AnimatablePanel.Animator xAnimator, AnimatablePanel.Animator yAnimator) {
            this.counter = counter;
            this.xAnimator = xAnimator;
            this.yAnimator = yAnimator;
        }
        
    }
    
    // A class to help animate counters.
    private class BoardAnimator {
        
        /**
         * The AnimatablePanel.Animator for the x coordinate.
         */
        public AnimatablePanel.Animator xAnimator;
        
        /**
         * The AnimatablePanel.Animator for the y coordinate.
         */
        public AnimatablePanel.Animator yAnimator;
        
        
        /**
         * The AnimatablePanel.Animator for the scale Factor.
         */
        public AnimatablePanel.Animator scaleAnimator;
        
        /**
         * Constructs a new BoardAnimator object.
         *
         * @param scaleAnimator the AnimatablePanel.Animator for the scale Factor.
         * @param xAnimator the AnimatablePanel.Animator for the x coordinate.
         * @param yAnimator the AnimatablePanel.Animator for the y coordinate.
         */
        public BoardAnimator(AnimatablePanel.Animator scaleAnimator, AnimatablePanel.Animator xAnimator,
                               AnimatablePanel.Animator yAnimator) {
            this.scaleAnimator = scaleAnimator;
            this.xAnimator = xAnimator;
            this.yAnimator = yAnimator;
        }
        
    }
}