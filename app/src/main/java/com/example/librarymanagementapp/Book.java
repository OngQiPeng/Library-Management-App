package com.example.librarymanagementapp;

public class Book {

    private String book_id;
    private String book_name;
    private String book_type;
    private String book_genre;
    private String borrowedBy;

    public Book(String book_id, String book_name, String book_type, String book_genre, String borrowedBy) {
        this.book_id = book_id;
        this.book_name = book_name;
        this.book_type = book_type;
        this.book_genre = book_genre;
        this.borrowedBy = borrowedBy;
    }

    public String getBook_id() {
        return book_id;
    }

    public void setBook_id(String book_id) {
        this.book_id = book_id;
    }

    public String getBook_name() {
        return book_name;
    }

    public void setBook_name(String book_name) {
        this.book_name = book_name;
    }

    public String getBook_type() {
        return book_type;
    }

    public void setBook_type(String book_type) {
        this.book_type = book_type;
    }

    public String getBook_genre() { return book_genre; }

    public void setBook_genre(String book_genre) { this.book_genre = book_genre; }

    public String getBorrowedBy() {
        return borrowedBy;
    }

    public void setBorrowedBy(String borrowedBy) {
        this.borrowedBy = borrowedBy;
    }
}
