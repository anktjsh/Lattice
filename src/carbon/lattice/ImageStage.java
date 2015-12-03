/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author Aniket
 */
public class ImageStage extends Stage {

    public ImageStage(Window wind, ImageButton ib) {
        getIcons().addAll(new Image(Lattice.class.getResourceAsStream("messenger.png")));
        initModality(Modality.APPLICATION_MODAL);
        setTitle(ib.getName());
        setScene(new Scene(new ImageDisplay(ib)));
        getScene().getStylesheets().add(LatticeStage.NATIVE);
    }
}
