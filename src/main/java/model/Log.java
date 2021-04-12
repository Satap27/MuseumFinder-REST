package model;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {

    public static String getStringStackTrace(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
