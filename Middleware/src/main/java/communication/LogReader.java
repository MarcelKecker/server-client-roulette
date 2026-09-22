package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LogReader extends BufferedReader {
    public LogReader(Reader reader) {
        super(reader);
    }
    @Override
    public String readLine() throws IOException {
        String text = super.readLine();
        System.out.println("<- " + text);
        return text;
    }
}
