package com.miumg.wtcenter.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum PipesText {
    INCH_1_8("1/8 Pulgadas"),
    INCH_1_4("1/4 Pulgadas"),
    INCH_3_8("3/8 Pulgadas"),
    INCH_1_2("1/2 Pulgadas"),
    INCH_3_4("3/4 Pulgadas"),
    INCH_1("1 Pulgadas"),
    INCH_2("2 Pulgadas"),
    INCH_2_5("2.5 Pulgadas"),
    INCH_3("3 Pulgadas"),
    INCH_4("4 Pulgadas"),
    INCH_6("6 Pulgadas"),
    INCH_8("8 Pulgadas"),
    INCH_10("10 Pulgadas"),
    INCH_12("12 Pulgadas");

    private String text;

    PipesText(String text) {
        this.text = text;
    }

    // Método para convertir a Map<String, String>
    public static List<PipesTextDto> toMap() {
        return Arrays.stream(PipesText.values())
                .map(e -> new PipesTextDto(e.name(),e.text)).toList();
    }

    public record PipesTextDto(String key, String value){
    }
}
