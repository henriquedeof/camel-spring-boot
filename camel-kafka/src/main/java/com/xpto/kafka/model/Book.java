package com.xpto.kafka.model;

public class Book {

    // {"name":"HENRIQUE BOOK", "author":"HENRIQUE FERNANDES", "available":"true"}

    private String name;
    private String author;
    private boolean available;

    public Book() {}

    public Book(String name, String author, boolean available) {
        this.name = name;
        this.author = author;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
