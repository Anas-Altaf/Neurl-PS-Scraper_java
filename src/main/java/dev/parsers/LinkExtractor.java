package dev.parsers;

import dev.parsers.dto.PaperDTO;
import dev.parsers.utils.parser.BibTexParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static dev.parsers.utils.web.WebUtils.getRandomUserAgent;

public class LinkExtractor {
    private static final Logger logger = Logger.getLogger(LinkExtractor.class.getName());
    public static final int START_YEAR = 1987;
    private static final int TIMEOUT = 10 * 1000;  // 10 seconds timeout
    public static final String WEB_URL = "https://papers.nips.cc";  // URL to scrape
    private static final String BASE_URL = WEB_URL + "/paper_files/paper/";  // URL to scrape
    private final BibTexParser bibTexParser = new BibTexParser();
    private final FileDownloader fileDownloader = new FileDownloader();
    private final String tempPath = "";

    // Main method to connect and get all links
    public List<String> getSubLinksByYear(int year) throws IOException {
        List<String> links = new ArrayList<>();

        // Connect to the URL and get the HTML document
        Document document = Jsoup.connect(BASE_URL + year)
                .timeout(TIMEOUT)
                .userAgent(getRandomUserAgent())
                .get();

        // Select all <li> elements inside the <ul> matching the CSS selector
        Elements liElements = document.select("body > div.container-fluid > div > ul > li");

        // Iterate through each <li> element and extract <a> tag links
        int count = 0;
        for (Element li : liElements) {
            // Find the <a> tag inside the <li>
            Element link = li.selectFirst("a");
            if (link != null) {
                // Print the text of the <a> tag
//                System.out.println("Link " + (++count) + ": " + link.text());
                // If you want to get the href attribute (URL)
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
                .userAgent(getRandomUserAgent())
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
                .userAgent(getRandomUserAgent())
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
    public PaperDTO getPaperInfo(String subLink) throws IOException {
        PaperDTO paperDTO ;
        if (!subLink.startsWith("http")){
            subLink = WEB_URL + subLink;
        }
        Document document = Jsoup.connect( subLink)
                .timeout(TIMEOUT)
                .userAgent(getRandomUserAgent())
                .get();

        Element anchors = document.selectFirst("body > div.container-fluid > div > div");
        if (anchors == null) {
            logger.warning("No Anchor Buttons element found in the page");
            return null;
        }

        for (Element anchor : anchors.children()) {
            if ("Bibtex".equals(anchor.text().trim())) {
                String bibtexLink = WEB_URL + anchor.attr("href");

                paperDTO = bibTexParser.parseBibTeXFile( fileDownloader.downloadFile(bibtexLink, tempPath, "bibtex", "bib", ""));
                return paperDTO;
            }
        }
        return null;
    }
    public boolean fetchPDFsYearly(int startYear, int endYear, String pathToStore) throws IOException {
        // Validation
        if(startYear > endYear) {
            logger.warning("Starting Year Cannot be Greater then Ending Year.");
            return false;
        }
        if(startYear < getStartYear() || endYear > getLastYear()){
            logger.warning("Years are out of range, Reference : Start : "+getStartYear() + ", End : "+ getLastYear());
return false;
        }
        if (!Character.isDigit(startYear) || !Character.isDigit(endYear) ){
            logger.warning("You entered invalid Years , Please try again");
            return false;
        }
        while(startYear <= endYear){
            try {
                // Get all links
                List<String> allSubLinks = getSubLinksByYear(startYear);

                // Get Pdf file link for each sub link
                int count = 1;
                String msg = "\nYear : "+startYear+" | Processed : " + count + "/" + allSubLinks.size();
                for (String subLink : allSubLinks) {

                    String pdfLink = getPdfFileLink(subLink);
                    if (pdfLink != null) {
                        // Download files
                        String res = fileDownloader.downloadFile(LinkExtractor.WEB_URL + pdfLink, pathToStore,"none" , "pdf", msg );
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
        return true;
    }

    public static void main( String[] args ) {
        LinkExtractor extractor = new LinkExtractor();

        try {
            System.out.println(extractor.getPaperInfo("https://papers.nips.cc/paper_files/paper/2009/hash/39059724f73a9969845dfe4146c5660e-Abstract.html").toString());
            // Get all links
//            List<String> allSubLinks = extractor.getSubLinksByYear(2023);
//            // Get Paper Info
//            System.out.println( extractor.getPaperInfo(allSubLinks.get(0)));

        } catch (Exception e) {
            logger.severe("Error in LinkExtractor " + e.getMessage());
        }
    }
}
