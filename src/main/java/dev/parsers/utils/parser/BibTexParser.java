package dev.parsers.utils.parser;

import org.jbibtex.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import dev.parsers.dto.PaperDTO;

public class BibTexParser {

    // Function that takes file path as input and returns a PaperDTO
    public PaperDTO parseBibTeXFile(String filePath) {
        PaperDTO paperDTO = new PaperDTO();
        try {
            // Initialize a BibTeX parser
            File bibFile = new File(filePath);
            BibTeXParser parser = new BibTeXParser();
            BibTeXDatabase database = parser.parse(new FileReader(bibFile));

            // Process the first entry in the BibTeX database (assuming single entry)
            for (Map.Entry<Key, BibTeXEntry> entry : database.getEntries().entrySet()) {
                Key citationKey = entry.getKey();
                BibTeXEntry bibTeXEntry = entry.getValue();

                // Extract fields and set them in the DTO
                paperDTO.setCitationKey(citationKey.toString());
                paperDTO.setAuthor(getField(bibTeXEntry, "author"));
                paperDTO.setTitle(getField(bibTeXEntry, "title"));
                paperDTO.setBookTitle(getField(bibTeXEntry, "booktitle"));
                paperDTO.setEditor(getField(bibTeXEntry, "editor"));
                paperDTO.setPages(getField(bibTeXEntry, "pages"));
                paperDTO.setPublisher(getField(bibTeXEntry, "publisher"));
                paperDTO.setUrl(getField(bibTeXEntry, "url"));
                paperDTO.setVolume(getField(bibTeXEntry, "volume"));
                paperDTO.setYear(getField(bibTeXEntry, "year"));
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (ParseException | TokenMgrException e) {
            System.err.println("Error parsing BibTeX file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        return paperDTO; // Return the populated DTO
    }

    // Helper method to get the field value from BibTeXEntry
    private String getField(BibTeXEntry entry, String field) {
        StringValue value = (StringValue) entry.getField(new Key(field));
        return value != null ? value.toUserString() : "";
    }
    public static void main(String[] args) {
        // Instantiate the parser
        BibTexParser bibTexParser = new BibTexParser();

        // Provide the file path to your .bib file
        String filePath = "C:\\Users\\_\\Downloads\\NeurIPS-2021-beyond-value-function-gaps-improved-instance-dependent-regret-bounds-for-episodic-reinforcement-learning-Bibtex.bib";

        // Get the PaperDTO by calling the parseBibTeXFile method
        PaperDTO paperDTO = bibTexParser.parseBibTeXFile(filePath);

        // Print the PaperDTO object
        System.out.println(paperDTO.toString());
    }
}
