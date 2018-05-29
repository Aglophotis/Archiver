package ru.mirea.data;

import java.io.IOException;

public interface Compressor {
    StringBuilder compression(StringBuilder block) throws IOException, InterruptedException;
}
