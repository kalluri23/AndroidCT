package com.verizon.contenttransfer.utils.UtilsFromApacheLib;

import java.io.Serializable;
import java.io.Writer;


public class CTStringBuilderWriter extends Writer implements Serializable {
    private final StringBuilder builder;

    public CTStringBuilderWriter() {
        this.builder = new StringBuilder();
    }

    public CTStringBuilderWriter(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    public CTStringBuilderWriter(StringBuilder builder) {
        this.builder = builder != null ? builder : new StringBuilder();
    }

    public Writer append(char value) {
        this.builder.append(value);
        return this;
    }

    public Writer append(CharSequence value) {
        this.builder.append(value);
        return this;
    }

    public Writer append(CharSequence value, int start, int end) {
        this.builder.append(value, start, end);
        return this;
    }

    public void close() {
    }

    public void flush() {
    }

    public void write(String value) {
        if (value != null) {
            this.builder.append(value);
        }

    }

    public void write(char[] value, int offset, int length) {
        if (value != null) {
            this.builder.append(value, offset, length);
        }

    }

    public StringBuilder getBuilder() {
        return this.builder;
    }

    public String toString() {
        return this.builder.toString();
    }
}

