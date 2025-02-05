package dev.parsers;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class FileDownloader {
    private static final Logger logger = Logger.getLogger(FileDownloader.class.getName());
    public static String fileName = "";

    // Method to download a file from a given URL and save it to the specified directory
    public String downloadFile(String fileUrl, String downloadDir, String fileName, String fileType, String msg) throws IOException {
        if (fileName.trim().equals("none") || fileName.isEmpty()) {
            // Extract file name from URL
                 fileName = Paths.get(new URL(fileUrl).getPath()).getFileName().toString();
                FileDownloader.fileName = fileName;
        }else {
            FileDownloader.fileName = fileName + "." + fileType;
        }
        ((Runnable) () -> {
            try {
                // Create the directory if it does not exist
                Path dirPath = Paths.get(downloadDir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }

                // Set the full path for the file
                Path filePath = dirPath.resolve(FileDownloader.fileName );

                URL url = new URL(fileUrl);  // Create a URL object from the direct link
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Get file size
                int fileSize = connection.getContentLength();

                // Open input and output streams
                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                     long bytesDownloaded = 0;

                    // Progress bar logic
                    AtomicLong progress = new AtomicLong(0);
                  Thread td =  new Thread(new Runnable() {
                        @Override
                        public void run() {
                            displayProgress(fileSize, progress, msg);
                        }
                    } ); // Start progress bar in a separate thread
                    td.start();
                    // Read bytes from the input stream and write to the output file stream
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        bytesDownloaded += bytesRead;
                        progress.set(bytesDownloaded);
                    }

                    // Ensure 100% progress is displayed when done
                    progress.set(fileSize);  // Explicitly set to file size to show 100% progress

                } catch (IOException e) {
                    logger.severe("Error downloading the file: " + e.getMessage());
                    throw e;
                }
            } catch (Exception e) {
                logger.severe("Error downloading the file: " + e.getMessage());
            }
        }).run();
       return FileDownloader.fileName ;
    }

    // Method to display the progress bar with color
    private static void displayProgress(int fileSize, AtomicLong progress, String msg) {
        while (progress.get() < fileSize) {
            double percentage = (double) progress.get() / fileSize * 100;
            String bar = generateProgressBar(percentage, fileName);
            System.out.print("\r" + bar + " " + String.format("%.2f", percentage) + "%");

        }

        // Final progress update to 100%
        String finalBar = generateProgressBar(100, fileName);
        System.out.print("\r" + finalBar + " 100.00%\n");
       System.out.println("✔ | " + msg );
    }

    // Generate a colorful progress bar string
    private static String generateProgressBar(double percentage, String url_info) {
        StringBuilder bar = new StringBuilder(url_info + " | [");
        int progressLength = 50;  // Length of the progress bar

        int filledLength = (int) (percentage / 100 * progressLength);
        bar.append("\u001B[32m#".repeat(Math.max(0, filledLength)));  // Green for the filled portion
        bar.append("\u001B[31m-".repeat(Math.max(0, progressLength - filledLength)));  // Red for the unfilled portion
        bar.append("\u001B[0m]");  // Reset the color
        return bar.toString();
    }

    public static void main(String[] args) {
        FileDownloader downloader = new FileDownloader();
        String fileUrl = "https://papers.nips.cc/paper_files/paper/19673-/bibtex";  // Replace with your file URL
        String downloadDir = "src/main/resources/downloads";  // Specify your desired download directory

        try {
            downloader.downloadFile(fileUrl, downloadDir, "test", "bib", "");
        } catch (IOException e) {
            logger.severe("Error downloading the file: " + e.getMessage());
        }
    }
}
