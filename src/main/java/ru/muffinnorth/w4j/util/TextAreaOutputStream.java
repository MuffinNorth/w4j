package ru.muffinnorth.w4j.util;

import javafx.scene.control.TextArea;

import java.io.ByteArrayOutputStream;

public class TextAreaOutputStream extends ByteArrayOutputStream {

    private TextArea textArea;

    public TextAreaOutputStream(TextArea textArea){
        super();
        this.textArea = textArea;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        super.write(b, off, len);
        textArea.clear();
        textArea.appendText(new String(buf));
    }
}
