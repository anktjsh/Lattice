/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

/**
 *
 * @author Aniket
 */
public class FileButton extends Button {

    private static final Image file = new Image(FileButton.class.getResourceAsStream("file.png"), 80, 80, true, true);
    private final Contact c;
    private final String p;
    private final byte[] b;

    public FileButton(Contact con, String path, byte[] b) {
        this.b = b;
        c = con;
        p = path;
        File f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + con.getUsername() + File.separator
                + path);
        if (!f.exists()) {
            try {
                writeToFile(b, f);
            } catch (IOException ex) {
            }
        }
        setGraphic(new ImageView(file));
        setMaxSize(100, 100);
        setOnAction((e) -> {
            try {
                Desktop.getDesktop().open(f);
            } catch (IOException ex) {
                Service.get().showMessage("No Application To Display This File", "Error", getScene().getWindow(), AlertType.ERROR);
            }
        });
        setContextMenu(new ContextMenu(new MenuItem("Save File"), new MenuItem("Open File")));
        getContextMenu().getItems().get(0).setOnAction((E) -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("File", "*" + path.substring(path.indexOf('.'))));
            fc.setTitle("");
            File fi = fc.showSaveDialog(getScene().getWindow());
            if (fi != null) {
                if (!fi.getAbsolutePath().endsWith(path.substring(path.indexOf('.')))) {
                    if (fi.getAbsolutePath().endsWith(".")) {
                        fi = Service.get().getFile(fi.getAbsolutePath() + path.substring(path.indexOf('.') + 1));
                    } else {
                        fi = Service.get().getFile(fi.getAbsolutePath() + path.substring(path.indexOf('.')));
                    }
                }
                try {
                    Files.copy(f, fi);
                } catch (IOException ex) {
                }
            }
        });
        getContextMenu().getItems().get(1).setOnAction((e) -> {
            fire();
        });
    }

    public FileButton copy() {
        return new FileButton(c, p, b);
    }

    private void writeToFile(byte[] b, File f) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(b);
        }
    }
}
