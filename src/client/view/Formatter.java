package client.view;

import scotlandyard.*;

import java.awt.*;
import java.util.List;
import javax.swing.*;

public class Formatter {
  
    static public Font defaultFontOfSize(int size) {
        return new Font("Helvetica Neue", Font.PLAIN, size);
    }
    
    static public Font boldFontOfSize(int size) {
        return new Font("Helvetica Neue", Font.BOLD, size);
    }
    
    static public Color greyColor() {
        return new Color(100, 100, 100);
    }
    
    static public Color primaryColor() {
        return new Color(51,135,253);
    }
    
    static public Color secondaryColor() {
        return new Color(251, 68, 60, 255);
    }
    
    static public Color colorForPlayer(Colour player) {
        if (player.equals(Colour.Black)) return new Color(51, 51, 51);
        if (player.equals(Colour.Blue)) return new Color(0, 107, 205);
        if (player.equals(Colour.Green)) return new Color(51, 154, 32);
        if (player.equals(Colour.Yellow)) return new Color(236, 200, 79);
        if (player.equals(Colour.Red)) return new Color(150, 0, 11);
        if (player.equals(Colour.White)) return new Color(237, 237, 237);
        return null;
    }
    
    
    // Returns a button with the correct text and styling.
    // @param label the text to be shown by the button.
    // @return a button with the correct text and styling.
    static public JButton button(String label) {
        JButton button = new JButton(label);
        button.setBackground(secondaryColor());
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setFont(boldFontOfSize(18));
        button.setPreferredSize(new Dimension(320, 40));
        return button;
    }
    
    static public JList list(List<String> items) {
        JList list = new JList<String>(items.toArray(new String[items.size()]));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setPrototypeCellValue("PROTOTYPE");
        list.setFont(defaultFontOfSize(18));
        list.setCellRenderer(new CustomCellRenderer());
        return list;
    }
    
    // Adds styling to the list elements.
    static private class CustomCellRenderer extends JLabel implements ListCellRenderer<Object> {
        
        private static final long serialVersionUID = -8212787301551146954L;
        
        /**
         * Returns the styled component.
         *
         * @param list the list that contains the elements.
         * @param value the value of the list item.
         * @param index the position of the item in the list.
         * @param isSelected the flag to tell whether the item is selected.
         * @param cellHasFocus the flag to tell whether the item has focus.
         * @return the Styled component.
         */
        public Component getListCellRendererComponent(
                                                      JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            String s = value.toString();
            setText(s.split("#")[0]);
            if (isSelected) {
                setBackground(new Color(25, 219, 182, 255));
                setForeground(Color.WHITE);
            } else {
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
            setOpaque(true);
            return this;
        }
        
    }
}