package ru.mirea.data;

public interface Decompressor {
    String decompression(String[] strData, StringBuilder codedBlock) throws Exception;
}
