/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import static carbon.lattice.LatticeStage.IS_DESKTOP;
import java.io.File;
import java.io.IOException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Aniket
 */
public class ImageDisplay extends BorderPane {

    private final ImageView center;
    private final DoubleProperty zoom;
    private double oHeight, oWidth;
    private final Button zoomI, zoomO, screenshot, crop, revert, rotate, close;
    private final ObservableList<Button> buttons;
    private final HBox controls;
    private final StackPane main;
    private Pane anchor;
    private Rectangle rec;

    ImageDisplay(ImageButton j) {
        setPadding(new Insets(5, 10, 5, 10));
        center = new ImageView(j.getView().getImage());
        controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(5, 10, 5, 10));
        setTop(controls);
        oHeight = j.getView().getImage().getHeight();
        oWidth = j.getView().getImage().getWidth();
        BorderPane.setAlignment(controls, Pos.CENTER);
        zoom = new SimpleDoubleProperty(1.0);
        zoom.addListener((ob, older, newer) -> {
            center.setFitHeight(oHeight * newer.doubleValue());
            center.setFitWidth(oWidth * newer.doubleValue());
        });
        center.setFitWidth(oWidth);
        center.setFitHeight(oHeight);
        setCenter(new ScrollPane(main = new StackPane(center)));

        main.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(main, Pos.CENTER);

        controls.getChildren().addAll(zoomI = new Button("Zoom In"), zoomO = new Button("Zoom Out"), screenshot = new Button("Save Image"), crop = new Button("Crop Image"), revert = new Button("Revert to Original"), rotate = new Button("Rotate"), close = new Button("Close Window"));
        close.setOnAction((e) -> {
            ((Stage) getScene().getWindow()).close();
        });
        revert.setOnAction((e) -> {
            zoom.set(1.0);
            oHeight = j.getView().getImage().getHeight();
            oWidth = j.getView().getImage().getWidth();
            center.setImage(j.getView().getImage());
            center.setFitWidth(oWidth);
            center.setFitHeight(oHeight);
            zoom.set(1);
        });
        rotate.setOnAction((E) -> {
            center.setRotate(center.getRotate() + 90);
        });
        crop.setOnAction((e) -> {
            Button select = new Button("Select");
            Button cancel = new Button("Cancel");
            HBox hb;
            hb = new HBox(5, select, cancel);
            main.setCursor(Cursor.CROSSHAIR);
            main.getChildren().add(anchor = new Pane());
            hb.setAlignment(Pos.CENTER);
            select.setOnAction((eh) -> {
                if (rec.getWidth() > 0 && rec.getHeight() > 0) {
                    SnapshotParameters sp = new SnapshotParameters();
                    sp.setViewport(new Rectangle2D(rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight()));
                    Image im = center.snapshot(sp, null);
                    main.getChildren().remove(anchor);
                    anchor.getChildren().remove(rec);
                    rec = null;
                    center.setImage(im);
                    controls.getChildren().remove(hb);
                    getScene().getWindow().sizeToScene();
                    oHeight = im.getHeight();
                    oWidth = im.getWidth();
                    center.setFitHeight(oHeight);
                    center.setFitWidth(oWidth);
                    zoom.set(1);
                } else {
                    cancel.fire();
                }
                crop.setDisable(false);
                zoomO.setDisable(false);
                zoomI.setDisable(false);
                screenshot.setDisable(false);
                revert.setDisable(false);
                rotate.setDisable(false);
            });
            cancel.setOnAction((E) -> {
                main.getChildren().remove(anchor);
                anchor.getChildren().remove(rec);
                rec = null;
                controls.getChildren().remove(hb);
                getScene().getWindow().sizeToScene();
                crop.setDisable(false);
                zoomO.setDisable(false);
                zoomI.setDisable(false);
                screenshot.setDisable(false);
                revert.setDisable(false);
                rotate.setDisable(false);
            });
            anchor.setOnMouseDragged((fe) -> {
                if (rec == null) {
                    rec = new Rectangle(fe.getX(), fe.getY(), 0, 0);
                    rec.setOpacity(0.45);
                    anchor.getChildren().add(rec);
                } else {
                    if (fe.getX() < center.getFitWidth() && fe.getY() < center.getFitHeight() && !(controls.getChildren().contains(hb))) {
                        rec.setWidth(fe.getX() - rec.getX());
                        rec.setHeight(fe.getY() - rec.getY());
                    }
                }
            });
            anchor.setOnMouseReleased((ge) -> {
                if (rec != null && rec.getWidth() > 0 && rec.getHeight() > 0) {
                    controls.getChildren().add(hb);
                    getScene().getWindow().sizeToScene();
                    crop.setDisable(true);
                    zoomO.setDisable(true);
                    zoomI.setDisable(true);
                    screenshot.setDisable(true);
                    revert.setDisable(true);
                    rotate.setDisable(true);
                    main.setCursor(Cursor.DEFAULT);
                }
            });

        });
        screenshot.setOnAction((e) -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Choose a place to save your image");
            String format = getFileFormat(j.getName());
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*." + format));
            File result = fc.showSaveDialog(null);
            if (result != null) {
                if (result.toString().contains(".")) {
                    if (result.toString().substring(result.toString().lastIndexOf('.') + 1).length() <= 0) {
                        result = Service.get().getFile(result.toString() + format);
                    }
                } else {
                    result = Service.get().getFile(result.toString() + "." + format);
                }
                saveImage(center.getImage(), result);
            }
        });
        zoomI.setOnAction((e) -> {
            if (center.getImage() != null) {
                if (zoom.get() < 2) {
                    zoom.set(zoom.get() + 0.1);
                }
            }
        });
        zoomO.setOnAction((e) -> {
            if (center.getImage() != null) {
                if (zoom.get() > 0.2) {
                    zoom.set(zoom.get() - 0.1);
                }
            }
        });
        buttons = FXCollections.observableArrayList(zoomI, zoomO, screenshot, crop, revert, rotate, close);
        if (!IS_DESKTOP) {
            for (Button b : buttons) {
                b.setFont(new Font(10));
            }
        }
    }

    private void saveImage(Image im, File name) {
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(im, null), "png", name);
        } catch (IOException ex) {
        }
    }

    private String getFileFormat(String s) {
        s = getFileName(s);
        return s.substring(s.indexOf('.') + 1);
    }

    public String getFileName(String s) {
        return s.substring(s.lastIndexOf(File.separator) + 1);
    }
}
