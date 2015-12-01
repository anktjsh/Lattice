/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

/**
 *
 * @author Aniket
 */
public class Dimension {
    private final double width, height;
    public Dimension() {
        this(0, 0);
    }
    
    public Dimension(double a, double b) {
        width = a;
        height = b;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
