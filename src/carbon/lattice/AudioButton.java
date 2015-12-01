/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

//import it.sauronsoftware.jave.AudioAttributes;
//import it.sauronsoftware.jave.Encoder;
//import it.sauronsoftware.jave.EncoderException;
//import it.sauronsoftware.jave.EncodingAttributes;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 *
 * @author Aniket
 */
public class AudioButton extends Button {

    private final String path;
    private final AudioClip clip;
    private final Polygon playSymbol = new Polygon(new double[]{
        50.0, 25.0,
        5.0, 0.0,
        5.0, 50.0});
    private final Rectangle stopSymbol = new Rectangle(47.5, 47.5, Color.BLACK);

    public AudioButton(Contact c, byte[] b) {
        path = "cache" + File.separator + LatticeStage.getName() + File.separator + c.getUsername() + File.separator + getFileName(c) + ".wav";
        File f;
        f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + c.getUsername());
        if (!f.exists()) {
            f.mkdirs();
        }
        saveToFile(getStream(b), f = Service.get().getFile(path));
        clip = new AudioClip(f.toURI().toString());
        init();
    }

    private int getFileName(Contact c) {
        File f;
        int count = 0;
        do {
            count++;
            f = Service.get().getFile("cache" + File.separator + LatticeStage.getName() + File.separator + c.getUsername() + File.separator + count + ".wav");
        } while (f.exists());
        return count;
    }

    public AudioButton(Contact c, String pa) {
        path = pa;
        clip = new AudioClip(Service.get().getFile(path).toURI().toString());
        init();
    }

    public AudioButton copy() {
        return new AudioButton(null, path);
    }

    private AudioInputStream getStream(byte[] b) {
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 44100.0f;
        int channels = 2;
        int sampleSize = 16;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
                * channels, rate, bigEndian);
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        return new AudioInputStream(bais, format, b.length / format.getFrameSize());
    }

    private void saveToFile(AudioInputStream strea, File out) {
        try {
            strea.reset();
        } catch (IOException ex) {
            return;
        }
        AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
        if (AudioSystem.isFileTypeSupported(fileType,
                strea)) {
            try {
                AudioSystem.write(strea, fileType, out);
            } catch (IOException ex) {
            }
        }
    }

    private void init() {
        setGraphic(playSymbol);
        setOnAction((e) -> {
            if (getGraphic() == playSymbol) {
                setGraphic(stopSymbol);
                clip.play();
                (new Thread(() -> {
                    while (clip.isPlaying()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ef) {
                        }
                    }
                    Platform.runLater(() -> {
                        setGraphic(playSymbol);
                    });
                })).start();
            } else {
                setGraphic(playSymbol);
                clip.stop();
            }
        });
        setContextMenu(new ContextMenu());
        getContextMenu().getItems().addAll(new MenuItem("Save as MP3"), new MenuItem("Save as WAV"), new MenuItem("Play"));
        getContextMenu().getItems().get(0).setOnAction((E) -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV File", "*.mp3"));
            fc.setTitle("");
            File fi = fc.showSaveDialog(getScene().getWindow());
            if (fi != null) {
                if (!fi.getAbsolutePath().endsWith(".mp3")) {
                    if (fi.getAbsolutePath().endsWith(".")) {
                        fi = Service.get().getFile(fi.getAbsolutePath() + "mp3");
                    } else {
                        fi = Service.get().getFile(fi.getAbsolutePath() + ".mp3");
                    }
                }
                wavToMP3(Service.get().getFile(path), fi);
            }
        });
        getContextMenu().getItems().get(1).setOnAction((E) -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("WAV File", "*.wav"));
            fc.setTitle("");
            File fi = fc.showSaveDialog(getScene().getWindow());
            if (fi != null) {
                if (!fi.getAbsolutePath().endsWith(".wav")) {
                    if (fi.getAbsolutePath().endsWith(".")) {
                        fi = Service.get().getFile(fi.getAbsolutePath() + "wav");
                    } else {
                        fi = Service.get().getFile(fi.getAbsolutePath() + ".wav");
                    }
                }
                try {
                    Files.copy(Service.get().getFile(path), fi);
                } catch (IOException ex) {
                }
            }
        });
        getContextMenu().getItems().get(2).setOnAction((E) -> {
            fire();
        });
    }

    private void wavToMP3(File src, File ar) {
//        AudioAttributes audio = new AudioAttributes();
//        audio.setCodec("libmp3lame");
//        audio.setBitRate(128000);
//        audio.setChannels(2);
//        audio.setSamplingRate(44100);
//        EncodingAttributes attrs = new EncodingAttributes();
//        attrs.setFormat("mp3");
//        attrs.setAudioAttributes(audio);
//        Encoder encoder = new Encoder();
//        try {
//            encoder.encode(src, ar, attrs);
//        } catch (IllegalArgumentException | EncoderException ex) {
//        }
    }

    public String getPath() {
        return path;
    }
}
