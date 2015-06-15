package client.aiview;

import client.view.*;
import client.view.Formatter;
import client.application.*;
import player.GameTree;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;

import com.google.gson.*;
import com.google.gson.stream.*;

public class AIView extends AnimatablePanel implements ActionListener, MouseListener, MouseMotionListener {

    private AnimatablePanel.Animator rotateAnimator;
    private ThreadCommunicator threadCom;
    private GraphHandler graphHandler;

    private boolean onTreeView = false;
    private GameTree gameTree = null;

    private JPanel hintPanel;
    private JButton button;
    private Integer hintState = 0;

    public AIView() {
        //Layout
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(20, 20, 20, 20);

        button = Formatter.button("Whats happening here?");
        button.setActionCommand("switch_views");
        button.addActionListener(this);
        add(button, gbc);

        JLabel title = new JLabel("The AI is thinking", SwingConstants.CENTER);
        title.setFont(Formatter.defaultFontOfSize(30));
        title.setForeground(Color.WHITE);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;
        //add(title, gbc);

        addMouseListener(this);
        addMouseMotionListener(this);

        try {
            threadCom = null;
            setBackground(new Color(131, 226, 197));
            setPreferredSize(new Dimension(400, 800));

            FileReader fileReader = new FileReader(new File("resources/GUIResources/AIData.txt"));
            JsonReader reader = new JsonReader(fileReader);
            Gson gson = new Gson();
            Map<String, List<Map<String, Double>>> json = gson.fromJson(reader, Map.class);

            graphHandler = new GraphHandler(json);

            rotateAnimator = createAnimator(0.0, 360.0, 10.0);
            rotateAnimator.setLoops(true);

            Timer time = new Timer(1000, this);
            time.setActionCommand("rep");
            time.start();

        } catch (FileNotFoundException e) {
            System.err.println("Error in the AI :" + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}

    public void mouseMoved(MouseEvent e) {
        if (!onTreeView) return;
        Node closestNode = findClosestNode(e.getPoint());
        if(closestNode != null) {
            closestNode.setSelected(true);

            List<List<Integer>> routes = findRoutes(closestNode, true);
            System.err.println("ROUTE: " + routes);
        }
        repaint();
    }

    public Node findClosestNode(Point position) {
      Double minDist = Double.POSITIVE_INFINITY;
      Node minNode = null;
      for (Node node : graphHandler.getNodes()) {
          if (!node.inTree()) continue;
        Vector v = graphHandler.getOrigin().offsetAdd(node);
        Double x = v.getX();
        Double y = v.getY();

        Double squareDistance = Math.pow(x - position.getX(), 2) + Math.pow(y - position.getY(), 2);
        if (squareDistance < minDist && squareDistance < 100) {
          minDist = squareDistance;
          minNode = node;
        }
      }
      return minNode;
    }

    public List<List<Integer>> findRoutes(Node n, boolean mrX) {
      if (n == null){
        List<List<Integer>> list = new ArrayList<List<Integer>>();
        list.add(new ArrayList<Integer>());
        list.add(new ArrayList<Integer>());
        return list;
      }
      List<List<Integer>> routes = findRoutes(n.parent(), !mrX);
      if(mrX) routes.get(0).add(n.location());
      else routes.get(1).add(n.location());
      return routes;
    }

    private void addHint(String message) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(4, 10, 10, 0);

        hintPanel = new JPanel(new GridBagLayout());
        hintPanel.setPreferredSize(new Dimension(500, 140));
        hintPanel.setOpaque(true);
        hintPanel.setBackground(Color.WHITE);
        Border whiteBorder = new LineBorder(Color.WHITE, 10);
        Border blueBorder = new LineBorder(new Color(131, 226, 197), 1);
        Border compBorder = new CompoundBorder(whiteBorder, blueBorder);
        hintPanel.setBorder(compBorder);

        String firstWord = message.split(" ")[0];
        String theRest = message.replace(firstWord, "");

        JTextPane messageLabel = new JTextPane();
        messageLabel.setContentType("text/html");
        messageLabel.setText("<html><font size=+4 face='Helvetica Neue'>" + firstWord + "</font><font face='Helvetica Neue'>" + theRest + "</font></html>");
        messageLabel.setFont(Formatter.defaultFontOfSize(12));
        messageLabel.setEditable(false);
        messageLabel.setHighlighter(null);
        hintPanel.add(messageLabel, gbc);

        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(hintPanel, gbc);
    }

    private void removeHint() {
        this.remove(hintPanel);
    }

    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        graphHandler.rotateNodes(rotateAnimator.value());

        Dimension size = getSize();
        graphHandler.setOrigin(new Vector(size.getWidth() / 2.0, size.getHeight() / 2.0, 0.0));

        drawEdges(g, graphHandler.getEdges(), graphHandler.getOrigin());
        drawVectors(g, graphHandler.getNodes(), graphHandler.getOrigin());
    }

    private void drawVectors(Graphics2D g, Set<Node> nodes, Vector origin) {
        for (Node node : nodes) {
            //if (onTreeView && !node.inTree()) continue;
            //if (!onTreeView && node.inTree()) continue;//?
            g.setColor(node.getColor());
            Vector vector = origin.offsetAdd(node);
            Double diameter = 13.75 - (vector.getZ() * (12.5 / 360.0));
            Double radius = diameter / 2;
            g.fillOval((int)(vector.getX() - radius), (int)(vector.getY() - radius), diameter.intValue(), diameter.intValue());
        }
    }

    private void drawEdges(Graphics2D g, List<Edge<Node>> edges, Vector origin) {
        //System.err.println("Count: " + edges.size());
        for (Edge<Node> edge : edges) {
            Node n1 = edge.getNode1();
            Node n2 = edge.getNode2();
            if (!edge.inTree() && onTreeView) continue;
            Vector node1 = origin.offsetAdd(n1);
            Vector node2 = origin.offsetAdd(n2);
            g.setColor(new Color(255, 255, 255, Math.min(n1.getColor().getAlpha(), n2.getColor().getAlpha())));
            g.drawLine(node1.getX().intValue(), node1.getY().intValue(), node2.getX().intValue(), node2.getY().intValue());
        }
    }

    public void setRep(GraphNodeRep graphNode) {
        graphHandler.setGraphNode(graphNode);
        //Start timer for hints
        Timer timer = new Timer(650, this);
        timer.setActionCommand("show_hint");
        timer.setRepeats(false);
        timer.start();
    }

    public void setGameTree(GameTree gameTree) {
        this.gameTree = gameTree;
    }

    public void showHint(String text) {
        if (gameTree == null) return;
        if (onTreeView) {
            System.err.println("R:" + gameTree.randomNode());
            gameTree.pause();
            addHint(text);

            Timer timer = new Timer(5000, this);
            timer.setActionCommand("hide_hint");
            timer.setRepeats(false);
            timer.start();
        }
    }

    public void hideHint() {
        if (gameTree == null) return;
        gameTree.resume();
        removeHint();
        Timer timer = new Timer(350, this);
        timer.setActionCommand("show_hint");
        timer.setRepeats(false);
        timer.start();
    }

    public void setThreadCom(ThreadCommunicator threadCom) {
        this.threadCom = threadCom;
    }

    public void humanPlaying() {
        if (onTreeView) threadCom.putEvent("timer_fired", true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() != null && e.getActionCommand().equals("rep")) {
            if (onTreeView) graphHandler.updateTree(this);
            if(!onTreeView) graphHandler.updateNodes();
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("switch_views")) {
            humanPlaying();
            if (onTreeView) {
                hintState = 1;
                graphHandler.returnFromTree(this);
                onTreeView = false;
            } else {
                onTreeView = true;
                graphHandler.showTree(this);
                add(button);
            }
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("show_hint")) {
            if (hintState < 4 && onTreeView) {
                if (hintState == 1) {
                    showHint("Hint number one.");
                } else if (hintState == 2) {
                    showHint("Hint number two.");
                } else if (hintState == 3) {
                    showHint("Hint number three.");
                }
                hintState++;
            }
        } else if (e.getActionCommand() != null && e.getActionCommand().equals("hide_hint")) {
            hideHint();
        } else {
            super.actionPerformed(e);
        }
    }

    @Override
    public void animationCompleted() {
        if (!onTreeView) {
            graphHandler.cleanTree(true);
            Double rotateValue = rotateAnimator.value();
            removeAnimator(rotateAnimator);
            rotateAnimator = createAnimator(rotateValue, rotateValue + 360.0, 10.0);
            rotateAnimator.setLoops(true);
        } else {
            graphHandler.finishTreeBuild();
        }
    }

}
