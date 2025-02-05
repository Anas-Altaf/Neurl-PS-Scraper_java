import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static java.lang.Math.abs;

public class Scraper {
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
            System.out.println("=======Neurl-PS Papers Scraper=======");
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
                        startYear = Integer.parseInt(scanner.next());
                        System.out.println("Enter the end year (max : "+extractor.getLastYear()+"), enter 0 to skip:");
                        scanner.reset();
                        endYear = Integer.parseInt(scanner.next());
                        endYear = endYear == 0 ? extractor.getLastYear() : endYear;
                        startYear = startYear == 0 ? extractor.getStartYear() : startYear;


                        System.out.println("Specify the path to save the papers (default: src/main/resources/downloads/) enter 'n' to skip:");
                        scanner.reset();
                        savePath = scanner.nextLine().trim();
                        if (savePath.isEmpty() || savePath.equalsIgnoreCase("n")) {
                            savePath = "src/main/resources/downloads/";
                        }
                        extractor.fetchPDFsYearly(startYear, endYear, savePath);
                        break;
                    case 2:
                        System.out.println("Enter the paper site link:");
                        String paperLink = scanner.next();
                        try {
                            String pdfLink = extractor.getPdfFileLink(paperLink.trim());
                            if (pdfLink == null) {
                                System.err.println("Error: No PDF link found for the provided paper link.");
                                continue;
                            }
                            fileDownloader.downloadFile(pdfLink, savePath, "none","pdf", "");
                        } catch (Exception e) {
                            System.err.println("Error downloading the paper: " + e.getMessage());
                        }
                        break;
                    case 3:
                        System.out.println("Enter the paper site link:");
                        String paperLinkInfo = scanner.next();
                        try {
                            System.out.println(extractor.getPaperInfo(paperLinkInfo.trim()));
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
        }
    }
    static class FileDownloader {
        private static final Logger logger = Logger.getLogger(Scraper.FileDownloader.class.getName());
        static String fileName = "";

        private static void displayProgress(int fileSize, AtomicLong progress, String msg) throws InterruptedException {
            while (progress.get() < fileSize) {
                double percentage = (double) progress.get() / fileSize * 100;
                String bar = generateProgressBar(percentage, fileName);
                System.out.print("\r" + bar + " " + String.format("%.2f", percentage) + "%");
                Thread.sleep(100);
            }
            String finalBar = generateProgressBar(100, fileName);
            System.out.print("\r" + finalBar + " 100.00%\n");
            System.out.println("✔ | " + msg );
        }
        private static String generateProgressBar(double percentage, String url_info) {
            StringBuilder bar = new StringBuilder(url_info + " | [");
            int progressLength = 50;

            int filledLength = (int) (percentage / 100 * progressLength);
            bar.append("\u001B[32m#".repeat(Math.max(0, filledLength)));
            bar.append("\u001B[31m-".repeat(Math.max(0, progressLength - filledLength)));
            bar.append("\u001B[0m]");
            return bar.toString();
        }
        public String downloadFile(String fileUrl, String downloadDir, String fileName, String fileType, String msg) throws IOException {
            if (fileName.trim().equals("none") || fileName.isEmpty()) {

                fileName = Paths.get(new URL(fileUrl).getPath()).getFileName().toString();
                Scraper.FileDownloader.fileName = fileName;
            }else {
                Scraper.FileDownloader.fileName = fileName + "." + fileType;
            }
            ((Runnable) () -> {
                try {
                    Path dirPath = Paths.get(downloadDir);
                    if (!Files.exists(dirPath)) {
                        Files.createDirectories(dirPath);
                    }
                    Path filePath = dirPath.resolve(Scraper.FileDownloader.fileName );
                    URL url = new URL(fileUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    int responseCode = connection.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("Server returned HTTP " + responseCode);
                    }
                    int fileSize = connection.getContentLength();
                    try (InputStream inputStream = connection.getInputStream();
                         FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        long bytesDownloaded = 0;
                        AtomicLong progress = new AtomicLong(0);
                        Thread td =  new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    displayProgress(fileSize, progress, msg);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } );
                        td.start();

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            bytesDownloaded += bytesRead;
                            progress.set(bytesDownloaded);
                        }
                        progress.set(fileSize);

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
    }
    static class LinkExtractor {
        public static final int START_YEAR = 1987;
        public static final String WEB_URL = "https://papers.nips.cc";
        private static final Logger logger = Logger.getLogger(Scraper.LinkExtractor.class.getName());
        private static final int TIMEOUT = 60 * 1000;
        private static final String BASE_URL = WEB_URL + "/paper_files/paper/";
        public final String User_Agent = " Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/37.0.2062.94 Chrome/37.0.2062.94 Safari/537.36 ";
        private final FileDownloader fileDownloader = new FileDownloader();
        private final String tempPath = "";
        public List<String> getSubLinksByYear(int year) throws IOException {
            List<String> links = new ArrayList<>();
            Document document = Jsoup.connect(BASE_URL + year)
                    .timeout(TIMEOUT)
                    .userAgent(User_Agent)
                    .get();
            Elements liElements = document.select("body > div.container-fluid > div > ul > li");
            int count = 0;
            for (Element li : liElements) {
                Element link = li.selectFirst("a");
                if (link != null) {
//                System.out.println("Link " + (++count) + ": " + link.text());
                    links.add(link.attr("href"));
                } else {
                    logger.warning("No link found in the <li> element");
                }
            }
            return links;
        }
        public int getLastYear() throws IOException {
            Document document = Jsoup.connect(BASE_URL)
                    .timeout(TIMEOUT)
                    .userAgent(User_Agent)
                    .get();
            Element listOfYearlyLinks = document.selectFirst("body > div.container-fluid > div.col-sm > ul");
            int count = -1;
            if (listOfYearlyLinks != null) {
                for (Element li : listOfYearlyLinks.children()) {
                    count++;
                }
            } else {
                logger.severe("No Papers found");
                return count;
            }
            return count + START_YEAR;
        }
        public int getStartYear(){
            return START_YEAR;
        }
        public String  getPdfFileLink(String subLink) throws IOException {
            String fullLink = WEB_URL + subLink;
            Document document = Jsoup.connect(fullLink)
                    .timeout(TIMEOUT)
                    .userAgent(User_Agent)
                    .get();
            Element div = document.selectFirst("body > div.container-fluid > div > div");
            if (div == null) {
                logger.warning("No Anchor Buttons element found in the page");
                return null;
            }
            for (Element anchor : div.children()) {
                if ("Paper".equals(anchor.text().trim())) {
                    return anchor.attr("href");
                }
            }
            return null;
        }
        public String getPaperInfo(String subLink) throws IOException {
            if (!subLink.startsWith("http")){
                subLink = WEB_URL + subLink;
            }
            Document document = Jsoup.connect( subLink)
                    .timeout(TIMEOUT)
                    .userAgent(User_Agent)
                    .get();
            Element anchors = document.selectFirst("body > div.container-fluid > div > div");
            if (anchors == null) {
                logger.warning("No Anchor Buttons element found in the page");
                return null;
            }
            for (Element anchor : anchors.children()) {
                if ("Bibtex".equals(anchor.text().trim())) {
                    String bibtexLink = WEB_URL + anchor.attr("href");

                    byte[] fileBytes = Files.readAllBytes(Paths.get(fileDownloader.downloadFile(bibtexLink, tempPath, "bibtex", "bib", "")));
                    return new String( fileBytes).replace("}", "");

                }
            }
            return null;
        }
        public void fetchPDFsYearly(int startYear, int endYear, String pathToStore) throws IOException {
            System.out.println("Downloading "+ abs(endYear - startYear) +" Years' Papers from Year " + startYear + " to " + endYear);
            if(startYear > endYear) {
                logger.warning("Starting Year Cannot be Greater then Ending Year.");
                return;
            }
            if(startYear < getStartYear() || endYear > getLastYear()){
                logger.warning("Years are out of range, Reference : Start : "+getStartYear() + ", End : "+ getLastYear());
                return;
            }

            while(startYear <= endYear){
                try {
                    List<String> allSubLinks = getSubLinksByYear(startYear);
                    int count = 1;
                    String msg = "\nYear : "+startYear+" | Processed : " + count + "/" + allSubLinks.size();
                    for (String subLink : allSubLinks) {

                        String pdfLink = getPdfFileLink(subLink);
                        if (pdfLink != null) {

                            String res = fileDownloader.downloadFile(Scraper.LinkExtractor.WEB_URL + pdfLink, pathToStore,"none" , "pdf", msg );
                            if (res != null ) {
                                count++;
                                msg = "\nYear : "+startYear+" | Processed : " + count + "/" + allSubLinks.size();
                            }
                        }
                    }
                    System.out.println("Year " + startYear + " | ✅ Done");
                } catch (IOException e) {
                    System.err.println("Error: " + e.getMessage());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                startYear++;
            }
        }
    }
}