package ru.mirea.data;


import java.io.File;

public interface Unpacker {
    int unpack(File inputFile, File outputDirectory) throws Exception;
}
