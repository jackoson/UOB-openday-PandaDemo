package client.view;

import scotlandyard.*;
import client.application.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A view to display a countdown timer.
 */

public class TimerView extends JPanel implements ActionListener {

    private static final long serialVersionUID = 4200788666766079225L;

    private Timer timer;
    private ActionListener aListener;
    private int maxTime = 260;
    private int timeLeft;
    private JLabel label;
    private boolean drawInfinity = false;

    /**
     * Constructs a new TimerView with a 4 minute countdown.
     */
    public TimerView() {
        setPreferredSize(new Dimension(80, 40));
        setLayout(new CardLayout());
        setOpaque(false);
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.setOpaque(false);
        this.aListener = null;
        timeLeft = maxTime;
        label = new JLabel("" + timeLeft + "s");
        label.setForeground(Color.white);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(label);

        JLabel infinity = new JLabel("\u221E", SwingConstants.CENTER);
        infinity.setForeground(Color.white);
        infinity.setFont(new Font("SansSerif", Font.PLAIN, 26));

        add(panel, "LABEL");
        add(infinity, "INFINITY");

        timer = new Timer(1000, this);
        timer.setRepeats(true);
    }

    /**
     * Overridden method to draw the timer.
     *
     * @param g0 the graphics object to draw to.
     */
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                            RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.white);
        if (!drawInfinity) {
            g.setStroke(new BasicStroke(2.0f));
            g.drawArc(49, 9, 22, 22, 90, calcAngle());
        }
    }

    /**
     * Returns the angle that the outer circle should draw to.
     *
     * @return the angle that the outer circle should draw to.
     */
    private int calcAngle() {
        int angle = (int) Math.round(((double) (maxTime - timeLeft) / (double) maxTime) * 360);
        return angle;
    }

    /**
     * Starts the timer.
     */
    public void start() {
        timer.start();
        drawInfinity = false;
        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, "LABEL");
    }

    /**
     * Stops the timer.
     */
    public void stop() {
        timeLeft = maxTime;
        drawInfinity = true;
        timer.stop();
        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, "INFINITY");
    }

    /**
     * Sets the max time for the timer in seconds.
     *
     * @param maxTime the max time for the timer.
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }

    /**
     * Adds the specified ActionListener to receive when the timer has run out.
     * If listener listener is null, no action is performed.
     *
     * @param listener the listener to be added to the view.
     */
    public void setActionListener(ActionListener listener) {
        aListener = listener;
    }

    /**
     * Is called when the timer runs out. This method then notifies the
     * ActionListener that has been set.
     *
     * @param e the ActionEvent from the timer.
     */
    public void actionPerformed(ActionEvent e) {
        timeLeft--;
        label.setText("" + timeLeft + "s");
        if (timeLeft == 0) {
            timer.stop();
            if (aListener != null) {
                aListener.actionPerformed(new ActionEvent(this, 0, "timer"));
            }
        } else if (timeLeft == 30) {
            if (aListener != null) {
                aListener.actionPerformed(new ActionEvent(this, 0, "timer_warning"));
            }
        } else {
            repaint();
        }
    }

}
