/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carbon.lattice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Aniket
 */
public class Files {

    public static ArrayList<String> readAllLines(File path) throws IOException {
        Scanner in = new Scanner(path);
        ArrayList<String> str = new ArrayList<>();
        while (in.hasNextLine()) {
            str.add(in.nextLine());
        }
        return str;
    }

    public static void write(File f, List<String> lines) throws IOException {
        try (FileWriter fw = new FileWriter(f); PrintWriter pw = new PrintWriter(fw)) {
            for (String sg : lines) {
                pw.println(sg);
            }
        }
    }

    public static void copy(File tar, File src) throws IOException {
        FileOutputStream outStream;
        try (FileInputStream inStream = new FileInputStream(src)) {
            outStream = new FileOutputStream(tar);
            byte[] buffer = new byte[1024];
            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }
        }
        outStream.close();
    }

}
