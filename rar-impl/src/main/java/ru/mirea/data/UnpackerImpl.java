package ru.mirea.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UnpackerImpl implements Unpacker {

    @Override
    public int unpack(File inputFile, File outputDirectory) throws Exception {
        FileOutputStream fileOutputStream = null;
        int BUFFER_SIZE_UNPACK = 64000;

        String path = inputFile.getAbsolutePath();
        final FileInputStream fileInputStream = new FileInputStream(path);

        char tmp = (char)fileInputStream.read();
        while (fileInputStream.available() != 0) {
            if (tmp == '0') {
                fileOutputStream = createFile(fileOutputStream, fileInputStream, outputDirectory);
                if (fileOutputStream == null) {
                    fileInputStream.close();
                    return -13;
                }
                int error = unpackWithoutCompression(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                if (error != 0) {
                    closeFiles(fileInputStream, fileOutputStream);
                    return error;
                }
            } else if (tmp == '1'){
                fileOutputStream = createFile(fileOutputStream, fileInputStream, outputDirectory);
                if (fileOutputStream == null) {
                    fileInputStream.close();
                    return -13;
                }
                int error = unpackWithCompression(fileInputStream, fileOutputStream);
                if (error != 0) {
                    closeFiles(fileInputStream, fileOutputStream);
                    return error;
                }
            } else if (tmp == '2'){
                int error = unpackWithCompression(fileInputStream, fileOutputStream);
                if (error != 0) {
                    closeFiles(fileInputStream, fileOutputStream);
                    return error;
                }
            } else if (tmp == '3'){
                int error = unpackWithoutCompression(fileInputStream, fileOutputStream, BUFFER_SIZE_UNPACK);
                if (error != 0) {
                    closeFiles(fileInputStream, fileOutputStream);
                    return error;
                }
            } else if (tmp == '4') {
                fileOutputStream = createFile(fileOutputStream, fileInputStream, outputDirectory);
                if (fileOutputStream == null) {
                    fileInputStream.close();
                    return -13;
                }
                int error = unpackRepeat(fileInputStream, fileOutputStream);
                if (error != 0) {
                    closeFiles(fileInputStream, fileOutputStream);
                    return error;
                }
            } else if (tmp == '5'){
                int error = unpackRepeat(fileInputStream, fileOutputStream);
                if (error != 0) {
                    closeFiles(fileInputStream, fileOutputStream);
                    return error;
                }
            } else {
                fileInputStream.close();
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return -3;
            }
            tmp = (char)fileInputStream.read();
        }
        closeFiles(fileInputStream, fileOutputStream);
        if (inputFile.getName().endsWith(".afkdec"))
            if (!inputFile.delete())
                return -8;
        return 0;
    }

    private void closeFiles(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        if (fileOutputStream != null)
            fileOutputStream.close();
        fileInputStream.close();
    }

    private int getInfo(FileInputStream fileInputStream, String[] strData, int[] intData) throws IOException {
        intData[0] = getMetaInt(fileInputStream);
        if (intData[0] == -1)
            return -1;
        strData[1] = getMetaString(fileInputStream, intData[0]);
        intData[1] = getMetaInt(fileInputStream);
        if (intData[1] == -1)
            return -1;
        strData[2] = getMetaString(fileInputStream, intData[1]);
        intData[2] = getMetaInt(fileInputStream);
        if (intData[2] == -1)
            return -1;
        return 0;
    }

    private int getMetaInt(FileInputStream fileInputStream) throws IOException {
        StringBuilder tmp = new StringBuilder();
        int b = fileInputStream.read();
        while (b != ':') {
            if (!Character.isDigit((char)b)) {
                fileInputStream.close();
                return -1;
            }
            tmp.append((char)b);
            b = fileInputStream.read();
        }
        return Integer.parseInt(tmp.toString());
    }

    private String getMetaString(FileInputStream fileInputStream, int length) throws IOException {
        StringBuilder tmp = new StringBuilder();
        int b = fileInputStream.read();
        for (int i = 0; i < length; i++) {
            tmp.append((char) b);
            b = fileInputStream.read();
        }
        return tmp.toString();
    }

    private FileOutputStream createFile(FileOutputStream fileOutputStream,final FileInputStream fileInputStream, File outputDirectory) throws IOException {
        String path;
        StringBuilder fileName = new StringBuilder();
        if (fileOutputStream != null)
            fileOutputStream.close();

        int b = fileInputStream.read();
        while (b != ':') {
            fileName.append((char)b);
            b = fileInputStream.read();
        }

        path = outputDirectory.getAbsolutePath();
        if (!Files.exists(Paths.get(path))) {
            return null;
        }
        path += "\\" + fileName.toString();
        fileOutputStream = new FileOutputStream(path);
        return fileOutputStream;
    }

    private int unpackWithCompression(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws Exception {
        Decompressor decompressor = new DecompressorImpl();
        String[] strData = new String[4];
        int[] intData = new int[4];
        int error = getInfo(fileInputStream, strData, intData);
        if (error == -1)
            return -6;
        byte[] bytes = new byte[intData[2]];
        int quantitySymbols = fileInputStream.read(bytes, 0, intData[2]);
        if (quantitySymbols != intData[2])
            return -7;
        char[] characters = new char[bytes.length];
        for (int j = 0; j < characters.length; j++) {
            characters[j] = (char) bytes[j];
        }

        String result = new String(characters);
        StringBuilder tmpStr = new StringBuilder(result);
        result = decompressor.decompression(strData, tmpStr);

        if (result.equals("-1")){
            fileInputStream.close();
            fileOutputStream.close();
            return -1;
        }
        if (result.equals("-2")){
            fileInputStream.close();
            fileOutputStream.close();
            return -2;
        }

        char[] tmpCharacters = result.toCharArray();
        byte[] tmpBytes = new byte[tmpCharacters.length];
        for (int j = 0; j < tmpCharacters.length; j++) {
            tmpBytes[j] = (byte) tmpCharacters[j];
        }
        fileOutputStream.write(tmpBytes, 0, tmpBytes.length);
        return 0;
    }

    private int unpackWithoutCompression(FileInputStream fileInputStream, FileOutputStream fileOutputStream, int BUFFER_SIZE) throws IOException{
        int sizeFile = getMetaInt(fileInputStream);
        if (sizeFile == -1){
            return -6;
        }
        int controlSum = 0;
        int quantitySymbols;
        byte[] buffer = new byte[BUFFER_SIZE];

        if (BUFFER_SIZE > sizeFile){
            quantitySymbols = fileInputStream.read(buffer, 0, sizeFile);
            controlSum += quantitySymbols;
            fileOutputStream.write(buffer, 0, quantitySymbols);
            if (controlSum != sizeFile){
                fileInputStream.close();
                fileOutputStream.close();
                return -4;
            }
        }
        else {
            int counter = 0;
            while ((quantitySymbols = fileInputStream.read(buffer, 0, BUFFER_SIZE)) > 0) {
                fileOutputStream.write(buffer, 0, quantitySymbols);
                counter += quantitySymbols;
                controlSum += quantitySymbols;
                if (counter + BUFFER_SIZE > sizeFile){
                    quantitySymbols = fileInputStream.read(buffer, 0, sizeFile - counter);
                    fileOutputStream.write(buffer, 0, quantitySymbols);
                    controlSum += quantitySymbols;
                    if (controlSum != sizeFile){
                        fileInputStream.close();
                        fileOutputStream.close();
                        return -4;
                    }
                    break;
                }
            }
        }
        return 0;
    }

    private int unpackRepeat(FileInputStream fileInputStream, FileOutputStream fileOutputStream) throws IOException {
        int sizeFile = getMetaInt(fileInputStream);
        if (sizeFile == -1){
            return -6;
        }
        byte[] buffer = new byte[sizeFile];
        if ((fileInputStream.read(buffer, 0, 1)) == -1){
            fileInputStream.close();
            fileOutputStream.close();
            return -5;
        }
        for (int i = 0; i < sizeFile; i++)
            buffer[i] = buffer[0];
        fileOutputStream.write(buffer, 0, sizeFile);
        return 0;
    }
}
