/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.File;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;

/**
 *
 * @author Aniket
 */
public class ImageButton extends Button {

    private final String name;
    private final Messenger mess;

    public ImageButton(Messenger ms, ImageView im, String name) {
        this.name = name;
        mess = ms;
        setGraphic(im);
        setOnAction((e) -> {
            new ImageStage(getScene().getWindow(), this).showAndWait();
        });
        setContextMenu(new ContextMenu());
        getContextMenu().getItems().add(new MenuItem("Open"));
        getContextMenu().getItems().get(0).setOnAction(getOnAction());
    }

    public ImageView getView() {
        return (ImageView) getGraphic();
    }

    public String getPath() {
        return Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + mess.getRecipient().getUsername() + File.separator + name).toString();
    }

    public String getName() {
        return name;
    }
}
