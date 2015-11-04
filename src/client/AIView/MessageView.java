package client.aiview;

import client.view.Formatter;
import client.application.*;
import player.GameTree;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MessageView extends JPanel implements ActionListener {

    private String[] messages;
    private boolean next;
    private JLabel label;
    private JButton skipButton;
    private JTextPane hintPane;
    private Timer timer;
    private GameTree gameTree = null;

    private int messageNo = 0;

    public MessageView(String title, String[] messages, boolean next) {
        setOpaque(false);
        setLayout(new BorderLayout());
        this.messages = messages;
        this.next = next;

        timer = new Timer(10000, this);
        timer.setRepeats(false);

        label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(Formatter.defaultFontOfSize(40));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        add(label, BorderLayout.NORTH);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(Color.WHITE);

        hintPane = new JTextPane();
        hintPane.setPreferredSize(new Dimension(320, 180));
        hintPane.setContentType("text/html");
        hintPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        hintPane.setFont(Formatter.defaultFontOfSize(18));
        hintPane.setForeground(new Color(0, 0, 0, 204));
        hintPane.setEditable(false);
        hintPane.setHighlighter(null);
        hintPane.setBackground(Color.WHITE);
        hintPane.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        bottomContainer.add(hintPane, BorderLayout.CENTER);

        if (next) skipButton = Formatter.button("Next");
        else skipButton = Formatter.button("Skip");
        skipButton.setBackground(Color.WHITE);
        skipButton.setForeground(Formatter.aiBackgroundColor());
        skipButton.setFont(Formatter.defaultFontOfSize(20));
        skipButton.addActionListener(this);
        skipButton.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        bottomContainer.add(skipButton, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);

        showHint(0);
    }

    public void start(GameTree gameTree) {
        this.gameTree = gameTree;
        messageNo = 0;
        showHint(messageNo);
        timer.restart();
    }

    public void stop() {
        gameTree = null;
        timer.stop();
    }

    public void setTitle(String title) {
        label.setText(title);
    }

    public void actionPerformed(ActionEvent e) {
        timer.stop();
        if (next) messageNo = ++messageNo % messages.length;
        else messageNo++;
        if (messageNo < messages.length) {
            showHint(messageNo);
            timer.restart();
        } else {
            if (gameTree != null) gameTree.setCanFinish(true);
            gameTree = null;
        }
    }

    private void showHint(int messageNo) {
        hintPane.setText(messages[messageNo]);
    }

}
