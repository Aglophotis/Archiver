package ru.mirea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Packer {
    private final static int BUFFER_SIZE_PACK = 64000;

    public static void pack(ArrayList<String> files, boolean isPack) throws IOException {
        final byte[] bytes = new byte[BUFFER_SIZE_PACK];
        int quantitySymbols;
        String path = new File(".").getCanonicalPath() + "\\original\\" + files.get(files.size() - 1) + ".afk";
        FileOutputStream fileOutputStream = new FileOutputStream(path);

        if (!isPack) {
            for (int i = 0; i < files.size() - 1; i++) {
                byte[] meta = getInfo(files.get(i)).getBytes();
                fileOutputStream.write(meta, 0, meta.length);

                path = new File(".").getCanonicalPath() + "\\original\\" + files.get(i);
                final FileInputStream fileInputStream = new FileInputStream(path);

                while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE_PACK)) > 0) {
                    fileOutputStream.write(bytes, 0, quantitySymbols);
                }
            }
            fileOutputStream.close();
        } else {
            for (int i = 0; i < files.size() - 1; i++) {
                path = new File(".").getCanonicalPath() + "\\original\\" + files.get(i);
                FileInputStream fileInputStream = new FileInputStream(path);

                String fileName = files.get(i) + ":";

                while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE_PACK)) > 0) {
                    char[] characters = new char[quantitySymbols];
                    for (int j = 0; j < quantitySymbols; j++) {
                        characters[j] = (char) bytes[j];
                    }
                    String s1 = new String(characters);
                    String s2 = Compressor.compression(s1);

                    if (s2.equals("-1")) {
                        String meta;
                        if (fileName.length() != 0){
                            meta = "0" + fileName + quantitySymbols + ":";
                        } else {
                            meta = "3" + quantitySymbols + ":";
                        }

                        fileOutputStream.write(meta.getBytes(), 0, meta.length());
                        fileOutputStream.write(bytes, 0, quantitySymbols);
                        fileName = "";
                        continue;
                    }
                    if (s2.equals("-2")) {
                        String meta;
                        if (fileName.length() != 0){
                            meta = "4" + fileName + quantitySymbols + ":";
                        } else{
                            meta = "5" + quantitySymbols + ":";
                        }

                        fileOutputStream.write(meta.getBytes(), 0, meta.length());
                        fileOutputStream.write(bytes, 0, 1);
                        fileName = "";
                        continue;
                    }

                    String meta;
                    if (fileName.length() != 0){
                        meta = "1" + fileName;
                    } else{
                        meta = "2";
                    }
                    fileOutputStream.write(meta.getBytes(), 0, meta.length());
                    characters = s2.toCharArray();
                    byte[] tmpBytes = new byte[characters.length];
                    for (int j = 0; j < characters.length; j++) {
                        tmpBytes[j] = (byte) characters[j];
                    }
                    fileOutputStream.write(tmpBytes, 0, s2.length());
                    fileName = "";
                }
                fileInputStream.close();
            }
            fileOutputStream.close();
        }
    }

    private static String getInfo(String fileName) throws IOException {
        String metaInfo = "0";
        String path = new File(".").getCanonicalPath() + "\\original\\" + fileName;
        if (!(new File(path).exists()))
            throw new IllegalArgumentException("Unknown name of file: " + fileName);
        long bytes = Files.size(Paths.get(path));
        metaInfo +=  fileName + ":" + bytes + ":";
        return metaInfo;
    }

}
