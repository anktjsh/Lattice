/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author Aniket
 */
public class Recording extends BorderPane implements EventHandler<ActionEvent> {

    private final int bufSize = 16384;
    private final Capture capture = new Capture();
    private final Playback playback = new Playback();
    private AudioInputStream audioInputStream;
    private String errStr;
    private double duration;

    private final Button captur, play, conf;
    private final HBox box;
    private final Messenger messenger;

    private static final Polygon playSymbol = new Polygon(new double[]{
        50.0, 25.0,
        5.0, 0.0,
        5.0, 50.0});
    private static final Circle recordSymbol = new Circle(25, Color.RED);
    private static final Rectangle stopSymbol = new Rectangle(47.5, 47.5, Color.BLACK);

    private byte[] bytes;

    public Recording(Messenger mess) {
        messenger = mess;
        setCenter((box = new HBox(10, captur = new Button("Record", recordSymbol), play = new Button("Play", playSymbol), conf = new Button("Send"))));
        captur.setOnAction(Recording.this);
        captur.setContentDisplay(ContentDisplay.TOP);
        captur.setFont(new Font(10));
        box.setAlignment(Pos.CENTER);
        play.setOnAction(Recording.this);
        play.setFont(new Font(10));
        play.setContentDisplay(ContentDisplay.TOP);
        conf.setOnAction(Recording.this);
        conf.setContentDisplay(ContentDisplay.TOP);
        conf.setFont(new Font(10));
        conf.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("send.png"), 50, 50, true, true)));
    }

    @Override
    public void handle(ActionEvent e) {
        Object obj = e.getSource();
        if (obj.equals(play)) {
            if (play.getGraphic() == playSymbol) {
                playBack();
                captur.setDisable(true);
                play.setGraphic(stopSymbol);
                play.setText("Stop");
            } else {
                stopPlay();
                captur.setDisable(false);
                play.setGraphic(playSymbol);
                play.setText("Play");
            }
        } else if (obj.equals(captur)) {
            if (captur.getGraphic() == recordSymbol) {
                startCapture();
                play.setDisable(true);
                captur.setGraphic(stopSymbol);
            } else {
                stopCapture();
                play.setDisable(false);
                captur.setGraphic(recordSymbol);
            }
        } else if (obj.equals(conf)) {
            if (bytes != null) {
                messenger.sendAudio(bytes);
            }
        }
    }

    public void startCapture() {
        capture.start();
    }

    public void stopCapture() {
        capture.stop();
    }

    public void playBack() {
        playback.start();
    }

    public void stopPlay() {
        playback.stop();
    }

    private class Playback implements Runnable {

        SourceDataLine line;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(String message) {
            if ((errStr = message) != null) {
                Platform.runLater(() -> {
//                    Alert al = new Alert(AlertType.ERROR);
//                    al.setHeaderText(message);
//                    al.setContentText(null);
//                    al.initOwner(getScene().getWindow());
//                    al.showAndWait();
                    Service.get().showMessage(message, "Audio Error", getScene().getWindow());
                    play.fire();
                });
            }
            if (thread != null) {
                thread = null;
            }
        }

        @Override
        public void run() {
            // make sure we have something to play
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            // reset to the beginnning of the stream
            try {
                audioInputStream.reset();
            } catch (IOException e) {
                shutDown("Unable to reset the stream\n" + e);
                return;
            }
            // get an AudioInputStream of the desired format for playback
            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            float rate = 44100.0f;
            int channels = 2;
            int frameSize = 4;
            int sampleSize = 16;
            boolean bigEndian = true;
            AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
                    * channels, rate, bigEndian);
            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format,
                    audioInputStream);
            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }
            // define the required attributes for our line,
            // and make sure a compatible line is supported.
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }
            // get and open the source data line for playback.
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            }
            // play back the captured audio data
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = line.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;
            // start the source data line
            line.start();
            while (thread != null) {
                try {
                    if ((numBytesRead = playbackInputStream.read(data)) == -1) {
                        break;
                    }
                    int numBytesRemaining = numBytesRead;
                    while (numBytesRemaining > 0) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (IOException e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            // we reached the end of the stream.
            // let the data play out, then
            // stop and close the line.
            if (thread != null) {
                line.drain();
            }
            line.stop();
            line.close();
            line = null;
            shutDown(null);
            Platform.runLater(() -> {
                captur.setDisable(false);
                play.setGraphic(playSymbol);
                play.setText("Play");
            });

        }
    } // End class Playback

    private class Capture implements Runnable {

        TargetDataLine microphone;
        Thread thread;

        public void start() {
            errStr = null;
            thread = new Thread(this);
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        private void shutDown(String message) {
            if ((errStr = message) != null && thread != null) {
                thread = null;
                System.err.println(errStr);
            }
        }

        @Override
        public void run() {
            duration = 0;
            audioInputStream = null;
            // define the required attributes for our microphone,
            // and make sure a compatible microphone is supported.
            AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
            float rate = 44100.0f;
            int channels = 2;
            int frameSize = 4;
            int sampleSize = 16;
            boolean bigEndian = true;
            AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
                    * channels, rate, bigEndian);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }
            // get and open the target data microphone for capture.
            try {
                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format, microphone.getBufferSize());
            } catch (LineUnavailableException ex) {
                shutDown("Unable to open the line: " + ex);
                return;
            } catch (SecurityException ex) {
                shutDown(ex.toString());
                //JavaSound.showInfoDialog();
                return;
            }
            // play back the captured audio data
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int frameSizeInBytes = format.getFrameSize();
            int bufferLengthInFrames = microphone.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            int numBytesRead;
            microphone.start();
//            SourceDataLine speakers = null;
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
//            try {
//                speakers = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
//                speakers.open(format);
//                speakers.start();
//            } catch (LineUnavailableException ex) {
//            }
            while (thread != null) {
                if ((numBytesRead = microphone.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
//                System.out.println(numBytesRead);
                out.write(data, 0, numBytesRead);
//                speakers.write(data, 0, numBytesRead);
            }
            // we reached the end of the stream.
            // stop and close the microphone.
//            speakers.drain();
//            speakers.close();
            microphone.stop();
            microphone.close();
            microphone = null;
            // stop and close the output stream
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {
            }
            // load bytes into the audio input stream for playback
            byte audioBytes[] = out.toByteArray();
            Recording.this.bytes = audioBytes;
            ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);
            long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format
                    .getFrameRate());
            duration = milliseconds / 1000.0;
            try {
                audioInputStream.reset();
            } catch (IOException ex) {
            }
        }
    }

}
