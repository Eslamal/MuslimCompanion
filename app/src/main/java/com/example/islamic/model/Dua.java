package com.example.islamic.model;

public class Dua {
    private String title;
    private String duaText;
    private String category;

    public Dua(String title, String duaText, String category) {
        this.title = title;
        this.duaText = duaText;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getDuaText() {
        return duaText;
    }

    public String getCategory() {
        return category;
    }
}