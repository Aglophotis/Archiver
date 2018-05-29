package ru.mirea.data;

public interface Decompressor {
    StringBuilder decompression(String[] strData, StringBuilder codedBlock) throws Exception;
}
