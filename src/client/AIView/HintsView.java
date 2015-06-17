package client.aiview;

import client.view.Formatter;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class HintsView extends JPanel {

    private String messageOne = "MessageOne, this is a great hint. ksjdfahdksjh kjh dkajh aksfsjhakdsjh kjh askjh askdjh kjh kjsh kSDHKajsdhkjAS HKJH Asdkjh kAJH.";
    private String messageTwo = "MessageTwo, this is also a great hint.";
    private String messageThree = "messageThree, this is a great hint too.";

    public HintsView() {
        setOpaque(false);
        setLayout(new BorderLayout());
        //Add the button in the top left corner;
    }

    private JTextPane getPane(String message) {
        String firstWord = message.split(" ")[0];
        String theRest = message.replace(firstWord, "");
        JTextPane messageLabel = new JTextPane();
        messageLabel.setPreferredSize(new Dimension(320, 180));
        messageLabel.setContentType("text/html");
        messageLabel.setText("<html><font size=+2 face='Helvetica Neue'>" + firstWord + "</font><font face='Helvetica Neue'>" + theRest + "</font></html>");
        messageLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        messageLabel.setFont(Formatter.defaultFontOfSize(18));
        messageLabel.setForeground(new Color(0, 0, 0, 204));
        messageLabel.setEditable(false);
        messageLabel.setHighlighter(null);
        messageLabel.setBackground(Color.WHITE);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return messageLabel;
    }

    public void showHint(int hintNo) {
        removeAll();
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        switch (hintNo) {
            case 1:
                p.add(getPane(messageOne), BorderLayout.EAST);
                add(p, BorderLayout.NORTH);
                break;
            case 2:
                p.add(getPane(messageTwo), BorderLayout.WEST);
                add(p, BorderLayout.SOUTH);
                break;
            case 3:
                p.add(getPane(messageThree), BorderLayout.EAST);
                add(p, BorderLayout.SOUTH);
                break;
            default:
                break;
        }
    }

}
