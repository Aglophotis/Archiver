package ru.mirea;

import java.io.*;
import java.util.ArrayList;

public class Unpacker {
    private final static int BUFFER_SIZE_UNPACK = 64000;

    public static void unpack(ArrayList<String> files) throws IOException {
        FileOutputStream fileOutputStream = null;

        for (String file : files) {
            String path = new File(".").getCanonicalPath() + "\\original\\" + file;
            if (!(new File(path).exists()))
                throw new IllegalArgumentException("Unknown name of file");
            final FileInputStream fileInputStream = new FileInputStream(path);

            char tmp = (char)fileInputStream.read();
            while (fileInputStream.available() != 0) {
                if (tmp == '0') {
                    fileOutputStream = createFile(fileOutputStream, fileInputStream);
                    unpackWithoutCompression(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                } else if (tmp == '1'){
                    fileOutputStream = createFile(fileOutputStream, fileInputStream);
                    unpackWithCompression(fileInputStream, fileOutputStream);
                } else if (tmp == '2'){
                    unpackWithCompression(fileInputStream, fileOutputStream);
                } else if (tmp == '3'){
                    unpackWithoutCompression(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                } else if (tmp == '4') {
                    fileOutputStream = createFile(fileOutputStream, fileInputStream);
                    unpackRepeat(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                } else if (tmp == '5'){
                    unpackRepeat(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                }
                tmp = (char)fileInputStream.read();
            }
            fileInputStream.close();
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

    private static FileOutputStream createFile(FileOutputStream fileOutputStream, FileInputStream fileInputStream) throws IOException {
        String path;
        String fileName;
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
        return fileOutputStream;
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
        result = Decompressor.decompression(strData, tmpStr);

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
        byte[] buffer = new byte[BUFFER_SIZE];
        if ((fileInputStream.read(buffer, 0, 1)) == -1){
            throw new IOException("5/6Archive is bit");
        }
        for (int i = 0; i < sizeFile; i++)
            buffer[i] = buffer[0];
        fileOutputStream.write(buffer, 0, sizeFile);
    }
}
