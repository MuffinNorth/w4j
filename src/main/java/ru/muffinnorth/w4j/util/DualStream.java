package ru.muffinnorth.w4j.util;

import java.io.PrintStream;

public class DualStream extends PrintStream {

    PrintStream out;

    public DualStream(PrintStream out, PrintStream out2) {
        super(out);
        this.out = out2;
    }


    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        out.write(buf, off, len);
    }

    @Override
    public void flush() {
        super.flush();
        out.flush();
    }
}
