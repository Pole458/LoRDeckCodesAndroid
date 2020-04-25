package com.pole.lordeckcodes;

/*
* Made by Pole (2020)
*/

import java.util.*;

public class LoRDeckEncoder {

    private final static int MAX_KNOWN_VERSION = 2;

    private final static int CARD_CODE_LENGTH = 7;

    // Public methods
    public static List<CardCodeAndCount> getDeckFromCode(String code) {

        List<CardCodeAndCount> result = new ArrayList<>();

        Byte[] bytes;
        try {
            bytes = Base32.decode(code);
        } catch (Base32.DecodingException e) {
            throw new IllegalArgumentException("Invalid deck code");
        }

        LinkedList<Byte> byteList = new LinkedList<>(Arrays.asList(bytes));

        // Grab format and version
        int version = bytes[0] & 0xF;
        byteList.remove(0);

        if (version > MAX_KNOWN_VERSION) {
            throw new IllegalArgumentException("The provided code requires a higher version of this library, please update.");
        }

        for (int i = 3; i > 0; i--) {

            int numGroupOfs = VarintTranslator.popVarint(byteList);

            for (int j = 0; j < numGroupOfs; j++) {

                int numOfsInThisGroup = VarintTranslator.popVarint(byteList);
                int set = VarintTranslator.popVarint(byteList);
                int faction = VarintTranslator.popVarint(byteList);

                for (int k = 0; k < numOfsInThisGroup; k++) {

                    int card = VarintTranslator.popVarint(byteList);

                    String setString = padLeft(set, 2);
                    String factionString = intIdentifierToFactionCode(faction);
                    String cardString = padLeft(card, 3);

                    CardCodeAndCount newEntry = new CardCodeAndCount(setString + factionString + cardString, i);
                    result.add(newEntry);
                }
            }
        }

        //the remainder of the deck code is comprised of entries for cards with counts >= 4
        //this will only happen in Limited and special game modes.
        //the encoding is simply [count] [cardcode]
        while (byteList.size() > 0) {

            int fourPlusCount = VarintTranslator.popVarint(byteList);
            int fourPlusSet = VarintTranslator.popVarint(byteList);
            int fourPlusFaction = VarintTranslator.popVarint(byteList);
            int fourPlusNumber = VarintTranslator.popVarint(byteList);

            String fourPlusSetString = padLeft(fourPlusSet, 2);
            String fourPlusFactionString = intIdentifierToFactionCode(fourPlusFaction);
            String fourPlusNumberString = padLeft(fourPlusNumber, 3);

            CardCodeAndCount newEntry = new CardCodeAndCount(fourPlusSetString + fourPlusFactionString + fourPlusNumberString, fourPlusCount);
            result.add(newEntry);
        }

        return result;
    }

    public static String getCodeFromDeck(List<CardCodeAndCount> deck) {
        return Base32.encode(getDeckCodeBytes(deck));
    }

    // Private methods
    private static int factionCodeToIntIdentifier(String factionCode) {
        switch (factionCode) {
            case "DE":
                return 0;
            case "FR":
                return 1;
            case "IO":
                return 2;
            case "NX":
                return 3;
            case "PZ":
                return 4;
            case "SI":
                return 5;
            case "BW":
                return 6;
        }
        throw new IllegalArgumentException("No factionName code for this value");
    }

    private static String intIdentifierToFactionCode(int identifier) {
        switch (identifier) {
            case 0:
                return "DE";
            case 1:
                return "FR";
            case 2:
                return "IO";
            case 3:
                return "NX";
            case 4:
                return "PZ";
            case 5:
                return "SI";
            case 6:
                return "BW";
        }
        throw new IllegalArgumentException("No factionName name for this factionName code");
    }

    private static String padLeft(int s, int n) {
        return String.format("%0" + n + "d", s);
    }

    private static Byte[] getDeckCodeBytes(List<CardCodeAndCount> deck) {

        if (!validCardCodesAndCounts(deck))
            throw new IllegalArgumentException("The provided deck contains invalid card codes.");

        Byte[] formatAndVersion = new Byte[] { 17 }; //i.e. 00010001
        final List<Byte> result = new ArrayList<>(Arrays.asList(formatAndVersion));

        final LinkedList<CardCodeAndCount> of3 = new LinkedList<>();
        final LinkedList<CardCodeAndCount> of2 = new LinkedList<>();
        final LinkedList<CardCodeAndCount> of1 = new LinkedList<>();
        final LinkedList<CardCodeAndCount> ofN = new LinkedList<>();

        for(CardCodeAndCount ccc : deck) {
            if (ccc.count == 3)
                of3.add(ccc);
            else if (ccc.count == 2)
                of2.add(ccc);
            else if (ccc.count == 1)
                of1.add(ccc);
            else if (ccc.count < 1)
                throw new IllegalArgumentException("Invalid count of " + ccc.count + " for card " + ccc.cardCode);
            else
                ofN.add(ccc);
        }

        //build the lists of setNumber and factionName combinations within the groups of similar card counts
        List<List<CardCodeAndCount>> groupedOf3s = getGroupedOfs(of3);
        List<List<CardCodeAndCount>> groupedOf2s = getGroupedOfs(of2);
        List<List<CardCodeAndCount>> groupedOf1s = getGroupedOfs(of1);

        //to ensure that the same decklist in any order produces the same code, do some sorting
        sortGroupOf(groupedOf3s);
        sortGroupOf(groupedOf2s);
        sortGroupOf(groupedOf1s);

        //Nofs (since rare) are simply sorted by the card code - there's no optimiziation based upon the card count
        sortByCardCodes(ofN);

        //Encode
        encodeGroupOf(result, groupedOf3s);
        encodeGroupOf(result, groupedOf2s);
        encodeGroupOf(result, groupedOf1s);

        //Cards with 4+ counts are handled differently: simply [count] [card code] for each
        encodeNOfs(result, ofN);

        return result.toArray(new Byte[0]);
    }

    private static void encodeNOfs(List<Byte> bytes, List<CardCodeAndCount> nOfs) {
        for(CardCodeAndCount ccc : nOfs) {
            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(ccc.count)));

            CardCodeParsed ccp = new CardCodeParsed(ccc.cardCode);

            int factionNumber = factionCodeToIntIdentifier(ccp.factionName);

            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(ccp.setNumber)));
            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(factionNumber)));
            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(ccp.cardNumber)));
        }
    }

    //The sorting convention of this encoding scheme is
    //First by the cardNumber of setNumber/factionName combinations in each top-level list
    //Second by the alphanumeric order of the card codes within those lists.
    private static void sortGroupOf(final List<List<CardCodeAndCount>> groupOf) {

        //noinspection ComparatorCombinators
        Collections.sort(groupOf, (ccc1, ccc2) -> Integer.compare(ccc1.size(), ccc2.size()));

        for (List<CardCodeAndCount> cardCodeAndCounts : groupOf) {

            sortByCardCodes(cardCodeAndCounts);

        }
    }

    private static void sortByCardCodes(final List<CardCodeAndCount> cardCodeAndCounts) {
        //noinspection ComparatorCombinators
        Collections.sort(cardCodeAndCounts, (ccc1, ccc2) -> ccc1.cardCode.compareTo(ccc2.cardCode));
    }

    private static List<List<CardCodeAndCount>> getGroupedOfs(final LinkedList<CardCodeAndCount> list) {

        List<List<CardCodeAndCount>> result = new ArrayList<>();
        while (list.size() > 0) {
            List<CardCodeAndCount> currentSet = new ArrayList<>();

            //get info from first
            String firstCardCode = list.get(0).cardCode;
            CardCodeParsed ccp = new CardCodeParsed(firstCardCode);

            //now add that to our new list, remove from old
            currentSet.add(list.get(0));
            list.remove(0);

            //sweep through rest of list and grab entries that should live with our first one.
            //matching means same setNumber and factionName - we are already assured the count matches from previous grouping.
            for (int i = list.size() - 1; i >= 0; i--) {
                String currentCardCode = list.get(i).cardCode;
                int currentSetNumber = CardCodeParsed.parseSetNumber(currentCardCode);
                String currentFactionCode = CardCodeParsed.parseFaction(currentCardCode);

                if (currentSetNumber == ccp.setNumber && currentFactionCode.equals(ccp.factionName)) {
                    currentSet.add(list.get(i));
                    list.remove(i);
                }
            }
            result.add(currentSet);
        }
        return result;
    }

    private static void encodeGroupOf(final List<Byte> bytes, List<List<CardCodeAndCount>> groupOf) {

        bytes.addAll(Arrays.asList(VarintTranslator.getVarint(groupOf.size())));

        for(List<CardCodeAndCount> currentList : groupOf) {
            //how many cards in current group?
            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(currentList.size())));

            //what is this group, as identified by a setNumber and factionName pair
            String currentCardCode = currentList.get(0).cardCode;

            CardCodeParsed ccp = new CardCodeParsed(currentCardCode);

            int currentFactionNumber = factionCodeToIntIdentifier(ccp.factionName);
            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(ccp.setNumber)));
            bytes.addAll(Arrays.asList(VarintTranslator.getVarint(currentFactionNumber)));

            //what are the cards, as identified by the third section of card code only now, within this group?
            for (CardCodeAndCount cd : currentList) {
                String code = cd.cardCode;
                int sequenceNumber = CardCodeParsed.parseCardNumber(code);
                bytes.addAll(Arrays.asList(VarintTranslator.getVarint(sequenceNumber)));
            }
        }
    }

    private static boolean validCardCodesAndCounts(List<CardCodeAndCount> deck) {
        for (CardCodeAndCount ccc : deck) {

            if (ccc.cardCode.length() != CARD_CODE_LENGTH)
                return false;

            CardCodeParsed ccp;

            try {
                ccp = new CardCodeParsed(ccc.cardCode);
            } catch (NumberFormatException e) {
                return false;
            }

            try {
                factionCodeToIntIdentifier(ccp.factionName);
            } catch (IllegalArgumentException e) {
                return false;
            }

            if (ccc.count < 1)
                return false;
        }
        return true;
    }
}
