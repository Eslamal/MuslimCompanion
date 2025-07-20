package com.example.islamic.model;

import java.io.Serializable;

public class Hadith implements Serializable {
    private String title;
    private String hadithText;
    private String description;

    public Hadith(String title, String hadithText, String description) {
        this.title = title;
        this.hadithText = hadithText;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getHadithText() {
        return hadithText;
    }

    public String getDescription() {
        return description;
    }
}