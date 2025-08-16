package com.eslamdev.islamic.model;

public class TafseerAya {
    private String ayaText;
    private String tafseerText;
    private int ayaNumber;

    public TafseerAya(String ayaText, String tafseerText, int ayaNumber) {
        this.ayaText = ayaText;
        this.tafseerText = tafseerText;
        this.ayaNumber = ayaNumber;
    }

    public String getAyaText() {
        return ayaText;
    }

    public String getTafseerText() {
        return tafseerText;
    }

    public int getAyaNumber() {
        return ayaNumber;
    }
}