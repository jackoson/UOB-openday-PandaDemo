package client.view;

import java.awt.*;

public class Formatter {
    static public Font defaultFontOfSize(int size) {
        return new Font("Helvetica Neue", 0, size);
    }
    
    static public Color greyColor() {
        return new Color(100, 100, 100);
    }
}