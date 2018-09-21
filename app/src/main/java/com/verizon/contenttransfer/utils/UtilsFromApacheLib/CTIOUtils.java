package com.verizon.contenttransfer.utils.UtilsFromApacheLib;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

public class CTIOUtils {

    public static String toString(InputStream input, String encoding) throws IOException {
        return toString(input, CTCharsets.toCharset(encoding));
    }

    public static String toString(InputStream input, Charset encoding) throws IOException {
        CTStringBuilderWriter sw = new CTStringBuilderWriter();
        copy((InputStream)input, (Writer)sw, (Charset)encoding);
        return sw.toString();
    }

    public static void copy(InputStream input, Writer output, Charset encoding) throws IOException {
        InputStreamReader in = new InputStreamReader(input, CTCharsets.toCharset(encoding));
        copy((Reader)in, (Writer)output);
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int)count;
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        return copyLarge(input, output, new char[4096]);
    }

    public static long copyLarge(Reader input, Writer output, char[] buffer) throws IOException {
        long count = 0L;

        int n;
        for(boolean var5 = false; -1 != (n = input.read(buffer)); count += (long)n) {
            output.write(buffer, 0, n);
        }

        return count;
    }

    public static void closeQuietly(InputStream input) {
        closeQuietly((Closeable)input);
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException var2) {
            ;
        }

    }
}
