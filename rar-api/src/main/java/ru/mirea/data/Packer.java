package ru.mirea.data;

import java.io.File;

public interface Packer {
    int pack(File inputFiles, File outputFile, boolean isCompression) throws Exception;
}
