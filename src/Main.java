import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main {
    Logger logger;
    static String loginName ;
    static String password;
    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger(Main.class.getName());
        FileHandler fileHandler = new FileHandler("myLog.log", false);
        fileHandler.setEncoding("UTF-8");
        fileHandler.setFormatter(new CustomLogFormatter());

        logger.addHandler(fileHandler);

        loginName = args[0];
        password = args[1];

//        System.out.println("Connected to : "+args[0]);
        logger.info("Connected to : "+args[0]);


        try {
            VoucherServer server = new VoucherServer(logger);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

//        Runnable invoiceServerRunnable = new Runnable() {
//            public void run() {
//                try {
//                    VoucherServer invoiceServer = new VoucherServer(logger);
//                } catch (IOException e) {
//                    logger.severe(e.getMessage());
//                    throw new RuntimeException(e);
//                } catch (TimeoutException e) {
//                    logger.severe(e.getMessage());
//                    throw new RuntimeException(e);
//                }
//            }
//        };

//        try {
//            Thread invoiceThread = new Thread(invoiceServerRunnable);
//            invoiceThread.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }
    static class CustomLogFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            // Customize the log message format here
            return "[" + record.getLevel() + "] " + record.getMessage() + "\n";
        }
    }

}
