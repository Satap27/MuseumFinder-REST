package model;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {

    /** Returns a printable stack trace, given the caught exception. This string could eventually be logged.
     *
     * @param   e   the caught exception
     * @return      the exception stack trace as a string
     */
    public static String getStringStackTrace(Exception e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
