package dev.parsers.utils.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class WebUtils {

    private static final String USER_AGENT_LIST_PATH = "src/main/resources/user-agents/user-agent-list.csv";
    private static final Random RANDOM = new Random();
    private static final Logger logger = Logger.getLogger(WebUtils.class.getName());


    public static void main(String[] args) {
        String userAgent = getRandomUserAgent();

        if (userAgent != null) {
            logger.info("User Agent: " + userAgent);
        }
    }

    public static String getRandomUserAgent() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(USER_AGENT_LIST_PATH));
            return lines.get(RANDOM.nextInt(lines.size())).trim();
        } catch (Exception e) {
            logger.severe("Error reading user agent list: " +  e.getMessage());
             return null;
        }
    }
}