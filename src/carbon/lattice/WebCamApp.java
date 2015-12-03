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
import static carbon.lattice.LatticeStage.NATIVE;
import com.github.sarxos.webcam.Webcam;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

public class WebCamApp implements Initializable {

    private final Stage stage;

    public WebCamApp(Window w) {
        stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WebCamPreview.fxml"));
        fxmlLoader.setController(WebCamApp.this);
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        Scene scene = new Scene(root, 900, 690);
        scene.getStylesheets().add(NATIVE);
        if (w != null) {
            stage.getIcons().addAll(((Stage) w).getIcons());
        }
        stage.initOwner(w);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Camera");
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public final Optional<Image> showAndWait() {
        stage.showAndWait();
        if (hasImage.get()) {
            return Optional.ofNullable(imageProperty.get());
        } else {
            return Optional.empty();
        }
    }

    public boolean hasCameraImage() {
        return hasImage.get();
    }

    public Image getCameraImage() {
        return imageProperty.get();
    }

    @FXML
    Button btnStartCamera;
    @FXML
    Button btnStopCamera;
    @FXML
    Button btnDisposeCamera;
    @FXML
    ComboBox<WebCamInfo> cbCameraOptions;
    @FXML
    BorderPane bpWebCamPaneHolder;
    @FXML
    FlowPane fpBottomPane;
    @FXML
    ImageView imgWebCamCapturedImage;
    private BufferedImage grabbedImage;
    private Webcam selWebCam = null;
    private boolean stopCamera = false;
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private final BooleanProperty hasImage = new SimpleBooleanProperty(false);
    private final String cameraListPromptText = "Choose Camera";

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        hasImage.set(false);
        imageProperty.set(null);
        fpBottomPane.setDisable(true);
        ObservableList<WebCamInfo> options = FXCollections.observableArrayList();
        int webCamCounter = 0;
        for (Webcam webcam : Webcam.getWebcams()) {
            WebCamInfo webCamInfo = new WebCamInfo();
            webCamInfo.setWebCamIndex(webCamCounter);
            webCamInfo.setWebCamName(webcam.getName());
            options.add(webCamInfo);
            webCamCounter++;
        }
        cbCameraOptions.setItems(options);
        cbCameraOptions.setPromptText(cameraListPromptText);
        cbCameraOptions.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends WebCamInfo> arg2, WebCamInfo arg3, WebCamInfo arg4) -> {
            if (arg4 != null) {
                System.out.println("WebCam Index: " + arg4.getWebCamIndex() + ": WebCam Name:" + arg4.getWebCamName());
                initializeWebCam(arg4.getWebCamIndex());
            }
        });
        Platform.runLater(this::setImageViewSize);
        btnStopCamera.setText("Take Picture");
        btnDisposeCamera.setText("Select");
    }

    protected void setImageViewSize() {
        double height = bpWebCamPaneHolder.getHeight();
        double width = bpWebCamPaneHolder.getWidth();
        imgWebCamCapturedImage.setFitHeight(height);
        imgWebCamCapturedImage.setFitWidth(width);
        imgWebCamCapturedImage.prefHeight(height);
        imgWebCamCapturedImage.prefWidth(width);
        imgWebCamCapturedImage.setPreserveRatio(true);
    }

    protected void initializeWebCam(final int webCamIndex) {
        Task<Void> webCamIntilizer = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (selWebCam == null) {
                    selWebCam = Webcam.getWebcams().get(webCamIndex);
                    selWebCam.open();
                } else {
                    closeCamera();
                    selWebCam = Webcam.getWebcams().get(webCamIndex);
                    selWebCam.open();
                }
                startWebCamStream();
                return null;
            }
        };
        new Thread(webCamIntilizer).start();
        fpBottomPane.setDisable(false);
        btnStartCamera.setDisable(true);
        btnDisposeCamera.setDisable(true);
    }

    protected void startWebCamStream() {
        stopCamera = false;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (!stopCamera) {
                    try {
                        if ((grabbedImage = selWebCam.getImage()) != null) {
                            Platform.runLater(() -> {
                                final Image mainiamge = SwingFXUtils
                                        .toFXImage(grabbedImage, null);
                                imageProperty.set(mainiamge);
                            });
                            grabbedImage.flush();
                        }
                    } catch (Exception e) {
                    } finally {
                    }
                }
                return null;
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);

    }

    private void closeCamera() {
        if (selWebCam != null) {
            selWebCam.close();
        }
    }

    public void stopCamera(ActionEvent event) {
        stopCamera = true;
        btnStartCamera.setDisable(false);
        btnDisposeCamera.setDisable(false);
        btnStopCamera.setDisable(true);
    }

    public void startCamera(ActionEvent event) {
        stopCamera = false;
        startWebCamStream();
        btnStartCamera.setDisable(true);
        btnDisposeCamera.setDisable(true);
        btnStopCamera.setDisable(false);
    }
    /*
     import java.util.ArrayList;
     import java.util.List;

     public class TestClass {
     public static List<String> strings = new ArrayList<String>()
     {{
     add("Hello World");
     System.out.println(get(0));
     }};
    
     public static void main(String[] args) {}
     }
     */

    public void disposeCamera(ActionEvent event) {
        stopCamera = true;
        closeCamera();
        Webcam.shutdown();
        btnStopCamera.setDisable(true);
        btnStartCamera.setDisable(true);
        btnDisposeCamera.setDisable(true);
        hasImage.set(true);
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    class WebCamInfo {

        private String webCamName;
        private int webCamIndex;

        public String getWebCamName() {
            return webCamName;
        }

        public void setWebCamName(String webCamName) {
            this.webCamName = webCamName;
        }

        public int getWebCamIndex() {
            return webCamIndex;
        }

        public void setWebCamIndex(int webCamIndex) {
            this.webCamIndex = webCamIndex;
        }

        @Override
        public String toString() {
            return webCamName;
        }

    }
}
