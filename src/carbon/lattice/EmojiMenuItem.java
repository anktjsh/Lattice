/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Aniket
 */
public class EmojiMenuItem extends MenuItem {

    private final String apth;

    public EmojiMenuItem(Messenger mess, String path, Image im) {
        super("", new ImageView(im));
        setOnAction((e) -> {
            mess.sendEmoji(path);
        });
        apth = path;
    }

    public EmojiMenuItem(Messenger mess, String path) {
        super("");
        setGraphic(new ImageView(new Image(getClass().getResourceAsStream("emoji/emoji/" + path), 20, 20, true, true)));
        setOnAction((e) -> {
            mess.sendEmoji(path);
        });
        apth = path;
    }

    public String getPath() {
        return apth;
    }

    @Override
    public boolean equals(Object j) {
        if (j != null && j instanceof EmojiMenuItem) {
            EmojiMenuItem ei = (EmojiMenuItem) j;
            if (ei.getPath().equals(getPath())) {
                return true;
            }
        }
        return false;
    }
}
