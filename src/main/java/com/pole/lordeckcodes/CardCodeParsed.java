package com.pole.lordeckcodes;

/*
* Made by Pole (2019)
*/

public class CardCodeParsed {

    int setNumber;
    String factionName;
    int cardNumber;

    CardCodeParsed(String cardCode) {
        setNumber = parseSetNumber(cardCode);
        factionName = parseFaction(cardCode);
        cardNumber = parseCardNumber(cardCode);
    }

    static int parseSetNumber(String cardCode) {
        return Integer.parseInt(cardCode.substring(0, 2));
    }

    public static String parseFaction(String cardCode) {
        return cardCode.substring(2, 4);
    }

    static int parseCardNumber(String cardCode) {
        return Integer.parseInt(cardCode.substring(4, 7));
    }
}
