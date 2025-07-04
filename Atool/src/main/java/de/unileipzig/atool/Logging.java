package de.unileipzig.atool;

import java.util.logging.*;

public class Logging {
    private static final Logger LOGGER = Logger.getLogger("Logger");

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        handler.setFormatter(new CustomFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
    }

    public static void log(Level level, String ClassName, String message) {
        LOGGER.log(level, "[" + ClassName + ", " + level + "] " +  message);
    }

    /**
     * Formatter for Logger
     *
     * @author meni1999
     */
    public static class CustomFormatter extends Formatter {

        String stageName;

        public CustomFormatter() {
            super();
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        @Override
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }

    }
}
