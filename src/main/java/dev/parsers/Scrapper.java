package dev.parsers;

import dev.parsers.dto.PaperDTO;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Scrapper {
    public static void main(String[] args) throws IOException {
        LinkExtractor extractor = new LinkExtractor();
        FileDownloader fileDownloader = new FileDownloader();
        int startYear = 0;
        int endYear = 0;
        int choice = 0;
        Scanner scanner = new Scanner(System.in);
        while (choice != 4) {
            String savePath = "src/main/resources/downloads/";
            System.out.println("=================================");
            System.out.println("=======Neurl-PS Papers Scrapper=======");
            System.out.println("=================================");
            System.out.println("MENU:");
            System.out.println("1 - Download all papers by year interval");
            System.out.println("2 - Download papers by Paper site Link");
            System.out.println("3 - Get paper info by Paper site Link");
            System.out.println("4 - Exit");
            System.out.println("=================================");
            try{
            System.out.println("Enter your choice:");
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Enter the start year (min : "+extractor.getStartYear()+"), enter 0 to skip:");
                    startYear = scanner.nextInt();
                    System.out.println("Enter the end year (max : "+extractor.getLastYear()+"), enter 0 to skip:");
                     endYear = scanner.nextInt();
                    if (startYear == 0) {
                        startYear = extractor.getStartYear();
                    }
                    if (endYear == 0) {
                        endYear = extractor.getLastYear();
                    }
                    // Path to download the papers
                    System.out.println("Specify the path to save the papers (default: src/main/resources/downloads/) enter 'n' to skip:");
                    savePath = scanner.next().trim().toLowerCase();
                    if (savePath.isEmpty() || savePath.isBlank() || savePath.equals("n")) {
                        savePath = "src/main/resources/downloads/";
                    }
                    extractor.fetchPDFsYearly(startYear, endYear, savePath);
                    break;
                case 2:
                    System.out.println("Enter the paper site link:");
                    String paperLink = scanner.next();
                    try {
                        String pdfLink = extractor.getPdfFileLink(paperLink.trim());
                        fileDownloader.downloadFile(pdfLink, savePath, "none","pdf", "");
                    } catch (Exception e) {
                        System.err.println("Error downloading the paper: " + e.getMessage());
                    }
                    break;
                case 3:
                    System.out.println("Enter the paper site link:");
                    String paperLinkInfo = scanner.next();
                    try {
                        PaperDTO paperDTO = extractor.getPaperInfo(paperLinkInfo.trim());
                        System.out.println(paperDTO.toString());
                    } catch (IOException e) {
                        System.err.println("Error getting paper info: " + e.getMessage());
                    }
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a valid choice.");
            }}
            catch(Exception e){
                System.err.println("Error: " + e.getMessage());
                break;
            }
        } // end

    }
}
