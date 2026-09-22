package communication;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class LogWriter extends PrintWriter {
    public LogWriter(OutputStream outputStream, boolean autoFlush) {
        super(outputStream,  autoFlush);
    }
    @Override
    public void println(String text) {
        System.out.println("-> " + text);
        super.println(text);
    }
}
