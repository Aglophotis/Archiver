package ru.mirea.data;

import java.io.IOException;

public interface Compressor {
    String compression(String block) throws IOException, InterruptedException;
}
