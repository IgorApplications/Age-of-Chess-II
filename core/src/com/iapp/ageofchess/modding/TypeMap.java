package com.iapp.ageofchess.modding;

public enum TypeMap {
    TWO_D,
    ISOMETRIC;

    @Override
    public String toString() {
        if (this == TWO_D) return "2d";
        return "isometric";
    }
}
