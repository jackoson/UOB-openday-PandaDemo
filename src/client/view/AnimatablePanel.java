import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

class AnimatablePanel extends JPanel implements ActionListener {
    private Timer timer;
    private Double kTimeInterval = 1/50.0;
    
    //Animatable properties
    private Animator preferredSizeX = null;
    private Animator preferredSizeY = null;
    private Animator red = null;
    private Animator green = null;
    private Animator blue = null;
    private Animator alpha = null;
    private List<Animator> activeAnimators;
    
    public AnimatablePanel() {
        timer = new Timer((int)(1000.0*kTimeInterval), this);
        activeAnimators = new ArrayList<Animator>();
    }
    
    public void setPreferredSize(Dimension size, Double duration) {
        setPreferredSize(size, duration, AnimationEase.LINEAR);
    }
    
    public void setPreferredSize(Dimension size, Double duration, AnimationEase ease) {
        preferredSizeX = new Animator(getPreferredSize().getWidth(), duration, size.getWidth());
        preferredSizeX.setEase(ease);
        activeAnimators.add(preferredSizeX);
        preferredSizeY = new Animator(getPreferredSize().getHeight(), duration, size.getHeight());
        preferredSizeY.setEase(ease);
        activeAnimators.add(preferredSizeY);
        
        if (! timer.isRunning()) timer.start();
        animationBegun();
    }
    
    public void setBackground(Color color, Double duration) {
        setBackground(color, duration, AnimationEase.LINEAR);
    }
    
    public void setBackground(Color color, Double duration, AnimationEase ease) {
        Color currentColor = getBackground();
        Double currentRed = ((double)currentColor.getRed())/255;
        Double currentGreen = ((double)currentColor.getGreen())/255;
        Double currentBlue = ((double)currentColor.getBlue())/255;
        Double currentAlpha = ((double)currentColor.getAlpha())/255;
        
        Double red = ((double)color.getRed())/255;
        Double green = ((double)color.getGreen())/255;
        Double blue = ((double)color.getBlue())/255;
        Double alpha = ((double)color.getAlpha())/255;
        
        this.red = new Animator(currentRed, duration, red);
        this.red.setEase(ease);
        activeAnimators.add(this.red);
        this.green = new Animator(currentGreen, duration, green);
        this.green.setEase(ease);
        activeAnimators.add(this.green);
        this.blue = new Animator(currentBlue, duration, blue);
        this.blue.setEase(ease);
        activeAnimators.add(this.blue);
        this.alpha = new Animator(currentAlpha, duration, alpha);
        this.alpha.setEase(ease);
        activeAnimators.add(this.alpha);
        
        if (! timer.isRunning()) timer.start();
        animationBegun();
    }
    
    public void actionPerformed(ActionEvent e) {
        boolean finished = true;
        List<Animator> finishedAnimators = new ArrayList<Animator>();
        for (Animator a : activeAnimators) {
            boolean f = a.step();
            if (f) finishedAnimators.add(a);
            finished &= f;
        }
        for (Animator a : finishedAnimators) {
            activeAnimators.remove(a);
        }
        
        if(preferredSizeX != null && preferredSizeY != null) setPreferredSize(new Dimension(preferredSizeX.value().intValue(), preferredSizeY.value().intValue()));
        if(red != null && green != null && blue != null && alpha != null) setBackground(new Color((int)(255*red.value()), (int)(255*green.value()), (int)(255*blue.value()), (int)(255*alpha.value())));
        
        if (finished) {
            timer.stop();
            animationCompleted();
        }
        revalidate();
        repaint();
    }
    
    public void cancelAllAnimations() {
        if (timer != null) timer.stop();
    }
    //Function for subclasses to override
    public void animationBegun() {
        
    }
    //Function for subclasses to override
    public void animationCompleted() {
        
    }
    
    public Animator createAnimator(Double value, Double duration, Double target) {
        Animator animator = new Animator(value, duration, target);
        activeAnimators.add(animator);
        if (! timer.isRunning()) timer.start();
        animationBegun();
        return animator;
    }
    //Enumeration for ease types
    public enum AnimationEase {
        LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT
    }
    //Helper class to iterate values
    public class Animator {
        private AnimationEase ease = AnimationEase.LINEAR;
        private Double time;
        private Double initial;
        private Double change;
        private Double duration;
        private boolean increasing = true;
        
        public Animator(Double value, Double duration, Double target) {
            this.time = 0.0;
            this.initial = value;
            this.change = target - value;
            this.duration = duration;
            
            if (change < 0) increasing = false;
        }
        
        public boolean step() {
            time += kTimeInterval;
            if (time >= duration) {
                time = duration;
                return true;
            }
            return false;
        }
        
        public Double value() {
            if (ease == AnimationEase.EASE_IN) return change * Math.pow(time/duration, 3) + initial;
            if (ease == AnimationEase.EASE_OUT) return change * (Math.pow((time/duration) - 1, 3) + 1) + initial;
            if (ease == AnimationEase.EASE_IN_OUT) {
                if (time / duration < 0.5) return (change / 2) * Math.pow((time * 2)/duration, 3) + initial;
                else return (change / 2) * (Math.pow(((time * 2)/duration) - 2, 3) + 2) + initial;
            }
            return change * (time / duration) + initial;
        }
        
        public void setEase(AnimationEase ease) {
            this.ease = ease;
        }
    }
}