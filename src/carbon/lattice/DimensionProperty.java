/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Aniket
 */
public class DimensionProperty {
    
    private Dimension dim;
    private double tempx, tempy;
    private boolean changedx, changedy;
    private final ObservableList<DimensionListener> changes = FXCollections.observableArrayList();
    
    public DimensionProperty() {
        dim = new Dimension();
    }
    
    public DimensionProperty(Dimension di) {
        dim = di;
    }
    
    public DimensionProperty(double a, double b) {
        dim = new Dimension(a, b);
    }
    
    void setWidth(double b) {
        tempx = b;
        changedx = true;
        if(changedx&&changedy) {
            set(new Dimension(tempx, tempy));
            activateListeners();
        }
    }
    
    void setHeight(double b) {
        tempy = b;
        changedy = true;
        if (changedx&&changedy) {
            set(new Dimension(tempx, tempy));
            activateListeners();
        }
    }
    
    public double getWidth() {
        if (changedx) {
            return tempx;
        } else {
            return dim.getWidth();
        }
    }
    
    public double getHeight() {
        if (changedy) {
            return tempy;
        } else {
            return dim.getHeight();
        }
    }
    
    public void set(double a, double b) {
        set(new Dimension(a, b));
    }
    
    public void set(Dimension di) {
        dim = di;
        activateListeners();
    }
    
    private void activateListeners() {
        tempx = 0;
        tempy = 0;
        changedx = false;
        changedy =false;
        for (DimensionListener cl : changes) {
            cl.dimensionChanged(dim);
        }
    }
    
    public void addDimensionListener(DimensionListener dl) {
        changes.add(dl);
    }
    
    public ObservableList<DimensionListener> getDimensionListeners() {
        return changes;
    }
}
