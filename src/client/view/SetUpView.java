package client.view;

import scotlandyard.*;
import client.application.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

/**
 * A view to allow players to set up a new game or load an existing one.
 */

public class SetUpView extends JPanel implements KeyListener, ActionListener {
  
    private static final long serialVersionUID = 6820494630590852362L;
    
    private FileAccess fileAccess;
    private JButton replay;
    private JButton load;
    private JButton start;
    private JButton create;
    private JButton join;
    private JList<String> loadList;
    private JList<String> joinList;
    private JTextField newGameField;
    private JTextField createPlayerField;
    private JTextField createGameField;
    private JComboBox<Integer> playerDropDown;
    private JComboBox<Colour> colorDropDown;
    private JPanel singlePanel;
    private JPanel multiPanel;
    private GridBagConstraints constraints;
    private JLabel title;
    private boolean isMultiplayer = false;
    private List<String> savedGames;
    private BufferedImage background;
    private BufferedImage backgroundImage;
    private ActionListener listener;
    private JButton multiplayerButton;
    
    /**
     * Constructs a new SetUpView object.
     *
     * @param fileAccess the FileAccess object to get the images.
     */
    public SetUpView(FileAccess fileAccess) {
        this.fileAccess = fileAccess;
        background = fileAccess.getSetupBackground();
        setBackground(Formatter.primaryColor());
        setLayout(new GridBagLayout());
        
        constraints = new GridBagConstraints();
        
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(new Dimension(100, 64));
        spacer.setBackground(Color.RED);
        constraints.gridy = 0;
        add(spacer, constraints);
        
        
        title = new JLabel("Local Game");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setLayout(new BorderLayout());
        title.setFont(Formatter.defaultFontOfSize(26));
        title.setForeground(Formatter.secondaryColor());
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(title, constraints);
        constraints.fill = GridBagConstraints.NONE;
        
        multiplayerButton = new JButton();
        multiplayerButton.setContentAreaFilled(false);
        multiplayerButton.setOpaque(false);
        multiplayerButton.setBorderPainted(false);
        multiplayerButton.setIcon(new ImageIcon(fileAccess.getSingleplayerIcon()));

        multiplayerButton.setPreferredSize(new Dimension(80, 60));
        multiplayerButton.setActionCommand("multiplayer");
        multiplayerButton.addActionListener(this);
        title.add(multiplayerButton, BorderLayout.EAST);
        
        //
        singlePanel = new JPanel();
        singlePanel.setOpaque(false);
        singlePanel.setBackground(new Color(50, 50, 50, 125));
        constraints.gridy = 2;
        add(singlePanel, constraints);
        
        LoadPanel loadPanel = new LoadPanel();
        loadPanel.setPreferredSize(new Dimension(400, 380));
        singlePanel.add(loadPanel);
        
        NewPanel newPanel = new NewPanel(this);
        newPanel.setPreferredSize(new Dimension(400, 380));
        singlePanel.add(newPanel);
        
        //
        multiPanel = new JPanel();
        multiPanel.setOpaque(false);
        multiPanel.setBackground(new Color(50, 50, 50, 125));
        constraints.gridy = 2;
        
        CreatePanel createPanel = new CreatePanel(this);
        createPanel.setPreferredSize(new Dimension(400, 380));
        multiPanel.add(createPanel);
        
        JoinPanel joinPanel = new JoinPanel();
        joinPanel.setPreferredSize(new Dimension(400, 380));
        multiPanel.add(joinPanel);
        
        backgroundImage = fileAccess.getSetupImage(new Dimension(1200, 800));
    }
    
    /**
     * Draws the background image of the view.
     *
     * @param g0 the Graphics object to draw to.
     */
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Dimension panelSize = getSize();
        g.drawImage(backgroundImage, (panelSize.width / 2) - 600, 0, null);
        g.drawImage(background, (panelSize.width / 2) - 415, (panelSize.height / 2) - 215, null);
    }
    
    private void showMultiplayer(boolean multi) {
        if (multi) {
            remove(singlePanel);
            add(multiPanel, constraints);
            title.setText("LAN Game");
            multiplayerButton.setIcon(new ImageIcon(fileAccess.getMultiplayerIcon()));
        } else {
            remove(multiPanel);
            add(singlePanel, constraints);
            title.setText("Local Game");
            multiplayerButton.setIcon(new ImageIcon(fileAccess.getSingleplayerIcon()));
        }
        repaint();
        revalidate();
        isMultiplayer = multi;
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("multiplayer")) {
            showMultiplayer(!isMultiplayer);
        }
    }
    
    /**
     * Returns the file path of the selected item in the load list.
     *
     * @return the file path of the selected item in the load list.
     */
    public String loadFilePath() {
        int index = loadList.getSelectedIndex();
        if (index >= 0) return savedGames.get(index);
        return null;
    }
    
    /**
     * Returns the name of the selected item in the load list.
     *
     * @return the name of the selected item in the load list.
     */
    public String newGameName() {
        String gameName = newGameField.getText();
        if (gameName.length() == 0) return null;
        return gameName;
    }
    
    /**
     * Returns the number of players selected by the user.
     *
     * @return the number of players selected by the user.
     */
    public Integer newPlayers() {
        return (Integer)playerDropDown.getSelectedItem();
    }
    
    public String createPlayerName() {
        String playerName = createPlayerField.getText();
        if (playerName.length() == 0) return null;
        return playerName;
    }
    
    public String createGameName() {
        String gameName = createGameField.getText();
        if (gameName.length() == 0) return null;
        return gameName;
    }
    
    public String createColor() {
        return (String)colorDropDown.getSelectedItem();
    }
    
    public String createGameServer() {
        int index = joinList.getSelectedIndex();
        if (index >= 0) return savedGames.get(index);//need to change
        return null;
    }
    
    /**
     * Sets the specified ActionListener to receive click events.
     *
     * @param listener the listener to be added.
     */
    public void setActionListener(ActionListener listener) {
        load.addActionListener(listener);
        start.addActionListener(listener);
        create.addActionListener(listener);
        join.addActionListener(listener);
        this.listener = listener;
    }
    
    /**
     * Refreshes the list of saved games and clears the
     * text field.
     */
    public void refreshSaves() {
        newGameField.setText("");
        
        savedGames = fileAccess.savedGames();
        loadList.setListData(savedGames.toArray(new String[savedGames.size()]));
    }
    
    /**
     * Called when a key is pressed. Starts a new game if
     * the key pressed is ENTER and the JTextField is empty.
     *
     * @param e the KeyEvent containing the key that has been
     * pressed.
     */
    public void keyPressed(KeyEvent e) {
        if (listener != null && e.getKeyCode() == KeyEvent.VK_ENTER
              && !newGameField.getText().isEmpty()) {
            listener.actionPerformed(new ActionEvent(this, 0, "startGame"));
        }
    }
    
    /**
     * Unused method for the KeyListener interface.
     *
     * @param e the KeyEvent containing which key has
     * been released.
     */
    public void keyReleased(KeyEvent e) {}
    
    /**
     * Unused method for the KeyListener interface.
     *
     * @param e the KeyEvent containing which key has
     * been typed.
     */
    public void keyTyped(KeyEvent e) {}
    
    // A view to draw the load list and button.
    private class LoadPanel extends JPanel {
      
        private static final long serialVersionUID = -5596275096590998296L;
      
        /**
         * Constructs a new LoadPanel object.
         */
        public LoadPanel() {
            setBorder(new EmptyBorder(15, 40, 40, 40));
            setOpaque(false);
            
            savedGames = fileAccess.savedGames();
            
            loadList = Formatter.list(savedGames);
            JScrollPane scrollPane = new JScrollPane(loadList);
            scrollPane.setPreferredSize(new Dimension(320, 260));
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220, 255), 1));
            add(scrollPane);
            
            Component spacer = Box.createRigidArea(new Dimension(400,10));
            add(spacer);
            
            load = Formatter.button("Load");
            load.setActionCommand("loadGame");
            add(load);
        }
        
    }
    
    // A view to draw the new game panel.
    private class NewPanel extends JPanel {
      
        private static final long serialVersionUID = -6864680088672987842L;
      
        /**
         * Constructs a new NewPanel object.
         */
        public NewPanel(KeyListener listener) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(15, 40, 40, 40));
            setOpaque(false);
            
            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(false);
            add(container, BorderLayout.NORTH);
            
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.gridx = 0;
            
            JComponent playerFieldLabel = new JLabel("Game Name");
            playerFieldLabel.setFont(Formatter.defaultFontOfSize(18));
            playerFieldLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            constraints.gridy = 0;
            container.add(playerFieldLabel, constraints);
            
            newGameField = new JTextField(10);
            newGameField.addKeyListener(listener);
            newGameField.setPreferredSize(new Dimension(100, 30));
            newGameField.setFont(Formatter.defaultFontOfSize(18));
            Border outside = BorderFactory.createLineBorder(new Color(220, 220, 220, 255), 1);
            Border inside = BorderFactory.createEmptyBorder(0, 5, 0, 5);
            newGameField.setBorder(BorderFactory.createCompoundBorder(outside, inside));
            constraints.gridy = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            container.add(newGameField, constraints);
            constraints.fill = GridBagConstraints.NONE;
            
            JComponent playerLabel = new JLabel("Players");
            playerLabel.setFont(Formatter.defaultFontOfSize(18));
            playerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            constraints.gridy = 2;
            container.add(playerLabel, constraints);
            
            Integer[] numPlayers = {2,3,4,5,6};
            playerDropDown  = new JComboBox<Integer>(numPlayers);
            playerDropDown.setLightWeightPopupEnabled(true);
            playerDropDown.setMaximumSize(new Dimension(80,30));
            constraints.gridy = 3;
            container.add(playerDropDown, constraints);
            
            
            start = Formatter.button("Start");
            start.setActionCommand("startGame");
            constraints.gridy = 4;
            add(start, BorderLayout.SOUTH);
        }
    }
    
    // A view to draw the new game panel.
    private class CreatePanel extends JPanel {
        
        private static final long serialVersionUID = -6864680088672987842L;
        
        /**
         * Constructs a new NewPanel object.
         */
        public CreatePanel(KeyListener listener) {
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(15, 40, 40, 40));
            setOpaque(false);
            
            JPanel container = new JPanel(new GridBagLayout());
            container.setOpaque(false);
            add(container, BorderLayout.NORTH);
            
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.anchor = GridBagConstraints.WEST;
            constraints.weightx = 1;
            constraints.weighty = 1;
            constraints.gridx = 0;
            
            JComponent playerFieldLabel = new JLabel("Student ID");
            playerFieldLabel.setFont(Formatter.defaultFontOfSize(18));
            playerFieldLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            constraints.gridy = 0;
            container.add(playerFieldLabel, constraints);
            
            createPlayerField = new JTextField(10);
            createPlayerField.addKeyListener(listener);
            createPlayerField.setPreferredSize(new Dimension(100, 30));
            createPlayerField.setFont(Formatter.defaultFontOfSize(18));
            Border outside = BorderFactory.createLineBorder(new Color(220, 220, 220, 255), 1);
            Border inside = BorderFactory.createEmptyBorder(0, 5, 0, 5);
            createPlayerField.setBorder(BorderFactory.createCompoundBorder(outside, inside));
            constraints.gridy = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            container.add(createPlayerField, constraints);
            constraints.fill = GridBagConstraints.NONE;
            
            JComponent nameLabel = new JLabel("Game Name");
            nameLabel.setFont(Formatter.defaultFontOfSize(18));
            nameLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            constraints.gridy = 2;
            container.add(nameLabel, constraints);
            
            createGameField = new JTextField(10);
            createGameField.addKeyListener(listener);
            createGameField.setPreferredSize(new Dimension(100, 30));
            createGameField.setFont(Formatter.defaultFontOfSize(18));
            createGameField.setBorder(BorderFactory.createCompoundBorder(outside, inside));
            constraints.gridy = 3;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            container.add(createGameField, constraints);
            constraints.fill = GridBagConstraints.NONE;
            
            
            JComponent playerLabel = new JLabel("Colour");
            playerLabel.setFont(Formatter.defaultFontOfSize(18));
            playerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
            constraints.gridy = 4;
            container.add(playerLabel, constraints);
            
            Colour[] colors = Colour.values();
            colorDropDown  = new JComboBox<Colour>(colors);
            colorDropDown.setLightWeightPopupEnabled(true);
            colorDropDown.setMaximumSize(new Dimension(80,30));
            constraints.gridy = 5;
            container.add(colorDropDown, constraints);
            
            
            create = Formatter.button("Create");
            create.setActionCommand("createGame");
            constraints.gridy = 6;
            add(create, BorderLayout.SOUTH);
        }
    }
    
    // A view to draw the load list and button.
    private class JoinPanel extends JPanel {
        
        private static final long serialVersionUID = -5596275096590998296L;
        
        /**
         * Constructs a new LoadPanel object.
         */
        public JoinPanel() {
            setBorder(new EmptyBorder(15, 40, 40, 40));
            setOpaque(false);
            
            savedGames = fileAccess.savedGames();//Need to change
            
            joinList = Formatter.list(savedGames);
            JScrollPane scrollPane = new JScrollPane(joinList);
            scrollPane.setPreferredSize(new Dimension(320, 260));
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220, 255), 1));
            add(scrollPane);
            
            Component spacer = Box.createRigidArea(new Dimension(400,10));
            add(spacer);
            
            join = Formatter.button("Join");
            join.setActionCommand("joinGame");
            add(join);
        }
        
    }
    
    // Returns the styled version of the element.
    // @param component the element to be styled.
    // @return the styled version of the element.
    private JComponent styleComponent(JComponent component) {
        component.setFont(Formatter.defaultFontOfSize(18));
        component.setMaximumSize(new Dimension(200,30));
        component.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return component;
    }
    
}