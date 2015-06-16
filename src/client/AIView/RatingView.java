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
        GridBagConstraints contstraints = new GridBagConstraints(1, 0, 1, 1, 0.3, 0.0, GridBagConstraints.PAGE_END, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 25);
        add(iconLabel, contstraints);

        String message = "Nice move!";
        if (!goodMove) message = "Hmmmm, good effort.";
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(Formatter.defaultFontOfSize(30));
        messageLabel.setForeground(new Color(255, 255, 255, 204));
        contstraints = new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.PAGE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 20);
        add(messageLabel, contstraints);

        JTextArea reasonLabel = new JTextArea("The following move may have been better as " + reason + ":");
        reasonLabel.setFont(Formatter.defaultFontOfSize(20));
        reasonLabel.setOpaque(false);
        reasonLabel.setForeground(new Color(255, 255, 255, 204));
        reasonLabel.setEditable(false);
        reasonLabel.setHighlighter(null);
        reasonLabel.setLineWrap(true);
        reasonLabel.setWrapStyleWord(true);
        contstraints = new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 50, 0, 50), 30, 50);
        add(reasonLabel, contstraints);

        contstraints = new GridBagConstraints(0, 3, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 50);
        add(new MoveView(suggestedMove), contstraints);
    }

    private class MoveView extends JPanel {

        private Move move;
        private boolean moveDouble = false;

        public MoveView(Move move) {
            this.move = move;
            setOpaque(false);
            if (move instanceof MoveTicket) {
                add(getTicketLabel((MoveTicket) move));
            } else if (move instanceof MoveDouble) {
                MoveTicket m1 = ((MoveDouble) move).move1;
                MoveTicket m2 = ((MoveDouble) move).move2;
                add(getTicketLabel(m1));
                add(getTicketLabel(m2));
                moveDouble = true;
            } else {
                JLabel label = new JLabel("Move Pass", SwingConstants.CENTER);
                label.setFont(Formatter.defaultFontOfSize(20));
                label.setForeground(new Color(255, 255, 255, 204));
                add(label);
            }
         }

        private JLabel getTicketLabel(MoveTicket move) {
            JLabel ticket = new JLabel("" + move.target, new ImageIcon(fileAccess.getLargeTickets().get(move.ticket)), SwingConstants.CENTER);
            ticket.setFont(Formatter.defaultFontOfSize(20));
            ticket.setForeground(new Color(255, 255, 255, 204));
            ticket.setHorizontalTextPosition(JLabel.CENTER);
            ticket.setVerticalTextPosition(JLabel.BOTTOM);
            ticket.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return ticket;
        }

        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension size = getSize();

            if (moveDouble) {
                int x = (int) (size.getWidth() / 2.0);
                int y = (int) (size.getHeight() / 2.0);

                int[] xCoords = new int[3];
                xCoords[0] = x - 6;
                xCoords[1] = x - 6;
                xCoords[2] = x + 6;

                int[] yCoords = new int[3];
                yCoords[0] = y - 10;
                yCoords[1] = y + 10;
                yCoords[2] = y;

                g.setColor(new Color(255, 255, 255, 204));
                g.fillPolygon(xCoords, yCoords, 3);
            }
        }

    }

}
