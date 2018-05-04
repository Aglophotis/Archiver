package ru.mirea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Archiver {
    private static boolean isPack = false;

    private final static int BUFFER_SIZE_UNPACK = 64000;
    private final static int BUFFER_SIZE_PACK = 64000;

    public static void checkCommandLine(String[] args, boolean[] flags, ArrayList<String> files) {
        int flagsQuantity = 0;
        while (args[flagsQuantity].charAt(0) == '-') {
            switch (args[flagsQuantity]) {
                case "-pack":
                    flags[0] = true;
                    break;
                case "-unpack":
                    flags[0] = false;
                    break;
                case "-compression":
                    flags[1] = true;
                    isPack = true;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid flag: " + args[flagsQuantity]);
            }
            flagsQuantity++;
        }

        if (flagsQuantity == 0) {
            throw new IllegalArgumentException("incorrect number of flags");
        }

        if ((flags[0] && args.length - flagsQuantity - 1 > 0) || (!flags[0] && args.length - flagsQuantity > 0)) {
            for (int i = flagsQuantity; i < args.length; i++) {
                files.add(args[i]);
            }
        } else {
            throw new IllegalArgumentException("incorrect number of files");
        }
    }

    private static String getInfo(String fileName) throws IOException {
        String metaInfo = "0";
        String path = new File(".").getCanonicalPath() + "\\original\\" + fileName;
        if (!(new File(path).exists()))
            throw new IllegalArgumentException("Unknown name of file");
        long bytes = Files.size(Paths.get(path));
        metaInfo +=  fileName + ":" + bytes + ":";
        return metaInfo;
    }

    public static void pack(ArrayList<String> files) throws IOException {
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
                    String s2 = HuffmanCompression.compression(s1);

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

    private static void getInfo(FileInputStream input, String[] strData, int[] intData) throws IOException {
        String tmp = "";
        int b = input.read();
        while (b != ':') {
            tmp += (char)b;
            b = input.read();
        }
        intData[0] = Integer.parseInt(tmp);
        System.out.println(intData[0]);
        tmp = "";
        b = input.read();

        for (int i = 0; i < intData[0]; i++) {
            tmp += (char)b;
            b = input.read();
        }
        strData[1] = tmp;
        tmp = "";
        b = input.read();

        while (b != ':') {
            tmp += (char)b;
            b = input.read();
        }
        intData[1] = Integer.parseInt(tmp);
        System.out.println(intData[1]);
        tmp = "";
        b = input.read();
        for (int i = 0; i < intData[1]; i++) {
            tmp += (char)b;
            b = input.read();
        }
        strData[2] = tmp;
        tmp = "";
        b = input.read();

        while (b != ':') {
            tmp += (char)b;
            b = input.read();
        }
        intData[2] = Integer.parseInt(tmp);
    }

    private static int getInfo(FileInputStream input) throws IOException {
        String tmp = "";
        int b = input.read();
        while (b != ':') {
            tmp += (char)b;
            b = input.read();
        }
        return Integer.parseInt(tmp);
    }

    public static void unpack(ArrayList<String> files) throws IOException {
        FileOutputStream fileOutputStream = null;

        for (String file : files) {
            String path = new File(".").getCanonicalPath() + "\\original\\" + file;
            String fileName;
            if (!(new File(path).exists()))
                throw new IllegalArgumentException("Unknown name of file");
            final FileInputStream fileInputStream = new FileInputStream(path);

            char tmp = (char)fileInputStream.read();
            while (fileInputStream.available() != 0) {
                if (tmp == '0') {
                    if (fileOutputStream != null)
                        fileOutputStream.close();

                    fileName = "";
                    int b = fileInputStream.read();
                    while (b != ':') {
                        fileName += (char)b;
                        b = fileInputStream.read();
                    }

                    path = new File(".").getCanonicalPath() + "\\results\\" + fileName;
                    fileOutputStream = new FileOutputStream(path);

                    unpackWithoutCompression(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                } else if (tmp == '1'){
                    if (fileOutputStream != null)
                        fileOutputStream.close();

                    fileName = "";
                    int b = fileInputStream.read();
                    while (b != ':') {
                        fileName += (char)b;
                        b = fileInputStream.read();
                    }

                    path = new File(".").getCanonicalPath() + "\\results\\" + fileName;
                    fileOutputStream = new FileOutputStream(path);
                    unpackWithCompression(fileInputStream, fileOutputStream);
                } else if (tmp == '2'){
                    unpackWithCompression(fileInputStream, fileOutputStream);
                } else if (tmp == '3'){
                    unpackWithoutCompression(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                } else if (tmp == '4') {
                    if (fileOutputStream != null)
                        fileOutputStream.close();

                    fileName = "";
                    int b = fileInputStream.read();
                    while (b != ':') {
                        fileName += (char)b;
                        b = fileInputStream.read();
                    }

                    path = new File(".").getCanonicalPath() + "\\results\\" + fileName;
                    fileOutputStream = new FileOutputStream(path);
                    unpackRepeat(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                } else if (tmp == '5'){
                    unpackRepeat(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                }
                tmp = (char)fileInputStream.read();
            }
            fileInputStream.close();
        }
    }

    private static void unpackWithCompression(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException{
        String[] strData = new String[4];
        int[] intData = new int[4];
        getInfo(fileInputStream, strData, intData);

        byte[] bytes = new byte[intData[2]];
        fileInputStream.read(bytes, 0, intData[2]);
        char[] characters = new char[bytes.length];
        for (int j = 0; j < characters.length; j++) {
            characters[j] = (char) bytes[j];
        }

        String result = new String(characters);
        StringBuilder tmpStr = new StringBuilder(result);
        result = HuffmanCompression.decompression(strData, tmpStr);

        char[] tmpCharacters = result.toCharArray();
        byte[] tmpBytes = new byte[tmpCharacters.length];
        for (int j = 0; j < tmpCharacters.length; j++) {
            tmpBytes[j] = (byte) tmpCharacters[j];
        }
        fileOutputStream.write(tmpBytes, 0, tmpBytes.length);
    }

    private static void unpackWithoutCompression(FileInputStream fileInputStream, FileOutputStream fileOutputStream, int BUFFER_SIZE) throws IOException{
        int sizeFile = getInfo(fileInputStream);
        int quantitySymbols;
        byte[] buffer = new byte[BUFFER_SIZE];

        if (BUFFER_SIZE > sizeFile){
            quantitySymbols = fileInputStream.read(buffer, 0, sizeFile);
            fileOutputStream.write(buffer, 0, quantitySymbols);
        }
        else {
            int counter = 0;
            while ((quantitySymbols = fileInputStream.read(buffer, 0, BUFFER_SIZE)) > 0) {
                fileOutputStream.write(buffer, 0, quantitySymbols);
                counter += quantitySymbols;
                if (counter + BUFFER_SIZE > sizeFile){
                    quantitySymbols = fileInputStream.read(buffer, 0, sizeFile - counter);
                    fileOutputStream.write(buffer, 0, quantitySymbols);
                    break;
                }
            }
        }
    }

    private static void unpackRepeat(FileInputStream fileInputStream, FileOutputStream fileOutputStream, int BUFFER_SIZE) throws IOException {
        int sizeFile = getInfo(fileInputStream);
        int quantitySymbols;
        byte[] buffer = new byte[BUFFER_SIZE];
        if ((quantitySymbols = fileInputStream.read(buffer, 0, 1)) == -1){
            throw new IOException("5/6Archive is bit");
        }
        for (int i = 0; i < sizeFile; i++)
            buffer[i] = buffer[0];
        fileOutputStream.write(buffer, 0, sizeFile);
    }
}
