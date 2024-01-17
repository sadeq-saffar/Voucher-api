import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomLogFormatter extends Formatter {
    @Override
    public String format(LogRecord record) {
        // Customize the log message format here
        return "[" + record.getLevel() + "] " + record.getMessage() + "\n";
    }
}