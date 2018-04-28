package ru.mirea;

import java.io.*;
import java.util.ArrayList;
import java.util.IllegalFormatException;

public class Archivator {
    public static void checkCommandLine(String[] args, boolean[] flags, ArrayList<String> files){
        int flagsQuantity = 0;
        while (args[flagsQuantity].charAt(0) == '-') {
            switch (args[flagsQuantity]){
                case "-pack":
                    flags[0] = true;
                    break;
                case "-unpack":
                    flags[0] = false;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid flag: " + args[flagsQuantity]);
            }
            flagsQuantity++;
        }

        if (flagsQuantity == 0) {
            throw new IllegalArgumentException("incorrect number of flags");
        }

        if ((flags[0] && args.length-flagsQuantity-1 > 0) || (!flags[0] && args.length-flagsQuantity > 0)) {
            for (int i = flagsQuantity; i < args.length; i++) {
                files.add(args[i]);
            }
        }
        else{
            throw new IllegalArgumentException("incorrect number of files");
        }
    }

    private static void writeFile(String fileName, RandomAccessFile inputFile) throws IOException {
        String path = new File(".").getCanonicalPath() + "\\original\\" + fileName;
        RandomAccessFile file = new RandomAccessFile(path, "r");

        int b = file.read();
        while (b != -1){
            inputFile.write(b);
            b = file.read();
        }
    }

    private static String getInfo(ArrayList<String> files) throws IOException {
        String metaInfo = "";
        for (int i = 0; i < files.size()-1; i++) {
            String path = new File(".").getCanonicalPath() + "\\original\\" + files.get(i);
            if (!(new File(path).exists()))
                throw new IllegalArgumentException("Unknown name of file");
            long bytes = new RandomAccessFile(path, "r").length();
            metaInfo += bytes + ":" + files.get(i) + ":";
        }
        metaInfo += '\0';
        return metaInfo;
    }

    public static void pack(ArrayList<String> files) throws IOException {
        String path = new File(".").getCanonicalPath() + "\\original\\" + files.get(files.size()-1) + ".afk";

        RandomAccessFile inputFile = new RandomAccessFile(path, "rw");
        inputFile.write(getInfo(files).getBytes());

        for (int i = 0; i < files.size()-1; i++) {
            writeFile(files.get(i),inputFile);
        }
        inputFile.close();
    }

    private static String getInfo(RandomAccessFile outputFile) throws IOException {
        String meta = "";
        int b = outputFile.read();
        while (b != '\0'){
            meta += (char)b;
            b = outputFile.read();
        }
        return meta;
    }

    public static void unpack(ArrayList<String> files) throws IOException {
        for (String file: files) {
            String path = new File(".").getCanonicalPath() + "\\original\\" + file;

            RandomAccessFile outputFile = new RandomAccessFile(path, "rw");
            if (outputFile.length() == 0)
                throw new IllegalArgumentException("file is empty");
            String meta = getInfo(outputFile);
            String[] fileProperties = meta.split(":");

            long sizeArchive = 0;
            for (int i = 0; i < fileProperties.length/2; ++i){
                sizeArchive += Integer.parseInt(fileProperties[2*i]);
            }
            if (sizeArchive + meta.length()+1 != outputFile.length()){
                outputFile.close();
                throw new IllegalArgumentException("Archive is broken");
            }

            for (int i = 0; i < fileProperties.length/2; ++i){
                path = new File(".").getCanonicalPath() + "\\results\\" + fileProperties[2*i + 1];
                RandomAccessFile tmpFile = new RandomAccessFile(path, "rw");
                tmpFile.setLength(0);
                for (int j = 0; j < Integer.parseInt(fileProperties[2*i]); j++){
                    int b = outputFile.read();
                    tmpFile.write(b);
                }
                tmpFile.close();
            }
            outputFile.close();
        }
    }
}
