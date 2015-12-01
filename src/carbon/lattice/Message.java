/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author Aniket
 */
public class Message implements Serializable, Comparable<Message> {

    public static final int EMOJI = 3, AUDIO = 4, VIDEO = 5, IMAGE = 1, TEXT = 0, IMAGETEXT = 2, FILE = 6;
    private final int type;

    private final byte[] data;
    private final String message;
    private final String metadata;
    private final String to, from;
    private final String timeSent;

    public Message(String a, String ba, int t, String mess, byte[] b, String meta, String time) {
        type = t;
        message = mess;
        data = b;
        metadata = meta;
        to = a;
        from = ba;
        timeSent = time;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public String getMetadata() {
        if (isImage() || isEmoji() || isFile()) {
            return metadata;
        }
        return "";
    }

    public byte[] getData() {
        if (isImage()) {
            return data;
        }
        if (isAudio()) {
            return data;
        }
        if (isVideo()) {
            return data;
        }
        if (isFile()) {
            return data;
        }
        return new byte[]{};
    }

    public String getText() {
        if (isText()) {
            if (message == null) {
                return "";
            }
            return message;
        }
        return "";
    }

    public boolean isImageText() {
        return type == IMAGETEXT;
    }

    public boolean isFile() {
        return type == FILE;
    }

    public boolean isEmoji() {
        return type == EMOJI;
    }

    public boolean isImage() {
        return type == IMAGE || type == IMAGETEXT;
    }

    public boolean isText() {
        return type == TEXT || type == IMAGETEXT;
    }

    public boolean isAudio() {
        return type == AUDIO;
    }

    public boolean isVideo() {
        return type == VIDEO;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public int compareTo(Message o) {
        if (o.timeSent == null) {
            return 1;
        }
        String spl[] = timeSent.split(" ");
        String sp[] = o.timeSent.split(" ");
        LocalDate d = LocalDate.parse(spl[0]);
        LocalDate t = LocalDate.parse(sp[0]);
        int r = d.compareTo(t);
        if (r == 0) {
            LocalTime ti = LocalTime.parse(spl[1] + ":00");
            LocalTime li = LocalTime.parse(sp[1] + ":00");
            return ti.compareTo(li);
        } else {
            return r;
        }
    }

}
