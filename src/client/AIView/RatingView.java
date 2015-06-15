package client.aiview;

import client.view.Formatter;
import client.application.FileAccess;
import scotlandyard.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.*;

public class RatingView extends JPanel {

    private FileAccess fileAccess;

    public RatingView(FileAccess fileAccess) {
        this.fileAccess = fileAccess;
        setLayout(new GridBagLayout());
        setBackground(new Color(47, 128, 131));
    }

    public void update(boolean goodMove, Move suggestedMove, String reason) {
        removeAll();

        ImageIcon icon = new ImageIcon(fileAccess.getGoodMove());
        if (!goodMove) icon = new ImageIcon(fileAccess.getBadMove());
        JLabel iconLabel = new JLabel(icon);
        GridBagConstraints contstraints = new GridBagConstraints(1, 0, 3, 1, 0.0, 1.0, GridBagConstraints.PAGE_END, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        add(iconLabel, contstraints);

        String message = "Nice move!";
        if (!goodMove) message = "Hmmmm :(, good effort!";
        JLabel messageLabel = new JLabel(message);
        contstraints = new GridBagConstraints();
        contstraints.gridx = 0;
        contstraints.gridy = 0;
        contstraints.gridwidth = 1;
        contstraints.gridheight = 1;
        contstraints.anchor = GridBagConstraints.CENTER;
        add(messageLabel, contstraints);

        JLabel reasonLabel = new JLabel(reason + " We suggest this move:" + suggestedMove.toString());
        contstraints = new GridBagConstraints();
        add(reasonLabel, contstraints);
    }

}
