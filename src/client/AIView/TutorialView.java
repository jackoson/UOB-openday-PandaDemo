package client.aiview;

import client.view.Formatter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class TutorialView extends JPanel {

    private String title = "How to play:";
    private String[] bullets = {
        "<font size=+2>The aim:</font> The Detective (Blue counter) must land on the same location as Mr X (Black counter) to win.",
        "<font size=+2>To move:</font> Click on the location you would like to move to, then click on the ticket you want to use. You can only move to a location to which you can travel using your available tickets.",
        "<font size=+2>Tips:</font> It is best to go to the locations with more transport options when Mr X is not visible."
    };

    public TutorialView() {
        setBackground(Formatter.aiBackgroundColor());
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(new Color(255, 255, 255, 204));
        titleLabel.setFont(Formatter.boldFontOfSize(48));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(100, 30, 30, 30));
        add(titleLabel, BorderLayout.NORTH);

        //Panel for bullet points.
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 40, 200, 40));

        for (String bullet : bullets) {
            JEditorPane bulletLabel = new JEditorPane();
            bulletLabel.setContentType("text/html");
            bulletLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            bulletLabel.setFont(Formatter.defaultFontOfSize(22));
            bulletLabel.setForeground(new Color(255, 255, 255, 204));
            bulletLabel.setText("<html>" + bullet + "</html>");
            bulletLabel.setEditable(false);
            bulletLabel.setOpaque(false);
            bulletLabel.setHighlighter(null);
            bulletLabel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
            panel.add(bulletLabel);
        }
        add(panel);
    }

}