package client.aiview;

import client.view.Formatter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class ButtonView extends JPanel {

    public ButtonView(ActionListener listener) {
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel label = new JLabel("The AI is thinking", SwingConstants.CENTER);
        label.setFont(Formatter.defaultFontOfSize(35));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));

        add(label, BorderLayout.NORTH);

        JButton button = Formatter.button("Look inside }");
        button.setBackground(Color.WHITE);
        button.setForeground(Formatter.aiBackgroundColor());
        button.setFont(Formatter.defaultFontOfSize(20));
        button.setActionCommand("switch_views");
        button.addActionListener(listener);
        button.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(30, 10, 30, 10));
        p.setOpaque(false);
        p.add(button);

        add(p, BorderLayout.SOUTH);
    }

}
