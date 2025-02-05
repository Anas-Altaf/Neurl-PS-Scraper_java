package dev.parsers.dto;

public class PaperDTO {
    private String citationKey;
    private String author;
    private String title;
    private String bookTitle;
    private String editor;
    private String pages;
    private String publisher;
    private String url;
    private String volume;
    private String year;
//Two constructors
    public PaperDTO() {
    }
    // With all fields
    public PaperDTO(String citationKey, String author, String title, String bookTitle, String editor, String pages, String publisher, String url, String volume, String year) {
        this.citationKey = citationKey;
        this.author = author;
        this.title = title;
        this.bookTitle = bookTitle;
        this.editor = editor;
        this.pages = pages;
        this.publisher = publisher;
        this.url = url;
        this.volume = volume;
        this.year = year;
    }
    // Getters and Setters
    public String getCitationKey() {
        return citationKey;
    }

    public void setCitationKey(String citationKey) {
        this.citationKey = citationKey;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getEditor() {
        return editor;
    }

    public void setEditor(String editor) {
        this.editor = editor;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "PaperDTO{"+
                "citationKey = " + citationKey + ",\n" +
                "author = " + author + ",\n" +
                "title = "+ title + ",\n" +
                "bookTitle = " + bookTitle + ",\n" +
                "editor = " + editor + ",\n" +
                "pages = " + pages + ",\n" +
                "publisher = " + publisher + ",\n" +
                "url = " + url + ",\n" +
                "volume = " + volume + ",\n" +
                "year = " + year + ",\n" +
                "}";
    }
}
