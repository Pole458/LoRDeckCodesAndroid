package com.pole.lordeckcodes;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoRDeckEncoderTest {

    //Tests the encoding of a set of hard coded decks in DeckCodesTestData.txt
    @Test
    public void EncodeDecodeRecommendedDecks() throws IOException {
        List<String> codes = new ArrayList<>();
        List<List<CardCodeAndCount>> decks = new ArrayList<>();

        //Load the test data from file.

        String line;
        // Both methods works
        BufferedReader myReader = new BufferedReader(new FileReader("src/test/resources/DeckCodesTestData.txt"));
        // BufferedReader myReader = new BufferedReader(new FileReader("DeckCodesTestData.txt"));

        while((line = myReader.readLine()) != null) {
            codes.add(line);
            List<CardCodeAndCount> newDeck = new ArrayList<>();
            for(line = myReader.readLine(); line != null && !line.isEmpty(); line = myReader.readLine()) {
                String[] parts = line.split(":");
                newDeck.add(new CardCodeAndCount(parts[1], Integer.parseInt(parts[0]))); // { Count = Integer.parse(parts[0]), CardCode = parts[1] });
            }
            decks.add(newDeck);
        }

        //Encode each test deck and ensure it's equal to the correct String.
        //Then decode and ensure the deck is unchanged.
        for (int i = 0; i < decks.size(); i++) {
            String encoded = LoRDeckEncoder.getCodeFromDeck(decks.get(i));
            Assert.assertEquals(codes.get(i), encoded);

            List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(encoded);

            Assert.assertTrue(VerifyRehydration(decks.get(i), decoded));
        }
    }

    @Test
    public void SmallDeck() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 1 )); // { CardCode = "01DE002", Count = 1 });

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }


    @Test
    public void LargeDeck() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        for(int i = 2; i < 22; i++) {
            deck.add(new CardCodeAndCount("01DE0" + (i < 10 ? "0" + i : i), 3));
        }

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void DeckWithCountsMoreThan3Small() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 4));

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void DeckWithCountsMoreThan3Large() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        for(int i = 2; i < 6; i++) {
            deck.add(new CardCodeAndCount("01DE00" + i, 3));
        }

        deck.add(new CardCodeAndCount("01DE006", 4));
        deck.add(new CardCodeAndCount("01DE007", 5));
        deck.add(new CardCodeAndCount("01DE008", 6));
        deck.add(new CardCodeAndCount("01DE009", 7));
        deck.add(new CardCodeAndCount("01DE010", 8));
        deck.add(new CardCodeAndCount("01DE011", 9));
        deck.add(new CardCodeAndCount("01DE011", 9));

        for(int i = 12; i < 22; i++) {
            deck.add(new CardCodeAndCount("01DE0" + i, 3));
        }

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void SingleCard40Times() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 40));

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void WorstCaseLength() {
        List<CardCodeAndCount> deck = new ArrayList<>();

        for(int i = 2; i < 7; i++) {
            deck.add(new CardCodeAndCount("01DE00" + i, 4));
        }

        deck.add(new CardCodeAndCount("01DE007", 5));
        deck.add(new CardCodeAndCount("01DE008", 6));
        deck.add(new CardCodeAndCount("01DE009", 7));
        deck.add(new CardCodeAndCount("01DE010", 8));
        deck.add(new CardCodeAndCount("01DE011", 9));
        deck.add(new CardCodeAndCount("01DE011", 9));

        for(int i = 12; i < 22; i++) {
            deck.add(new CardCodeAndCount("01DE0" + i, 4));
        }

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void OrderShouldNotMatter1() {
        List<CardCodeAndCount> deck1 = new ArrayList<>();
        deck1.add(new CardCodeAndCount("01DE002", 1)); //{ CardCode = "", Count = 1 });
        deck1.add(new CardCodeAndCount("01DE003", 2)); //{ CardCode = "", Count = 2 });
        deck1.add(new CardCodeAndCount("02DE003", 3)); //{ CardCode = "", Count = 3 });

        List<CardCodeAndCount> deck2 = new ArrayList<>();
        deck2.add(new CardCodeAndCount("01DE003", 2)); //{ CardCode = "01DE003", Count = 2 });
        deck2.add(new CardCodeAndCount("02DE003", 3)); //{ CardCode = "02DE003", Count = 3 });
        deck2.add(new CardCodeAndCount("01DE002", 1)); // { CardCode = "01DE002", Count = 1 });

        String code1 = LoRDeckEncoder.getCodeFromDeck(deck1);
        String code2 = LoRDeckEncoder.getCodeFromDeck(deck2);

        Assert.assertEquals(code1, code2);

        List<CardCodeAndCount> deck3 = new ArrayList<>();
        deck3.add(new CardCodeAndCount("01DE002", 4)); // { CardCode = "01DE002", Count = 4 });
        deck3.add(new CardCodeAndCount("01DE003", 2)); // { CardCode = "01DE003", Count = 2 });
        deck3.add(new CardCodeAndCount("02DE003", 3)); // { CardCode = "02DE003", Count = 3 });

        List<CardCodeAndCount> deck4 = new ArrayList<>();
        deck4.add(new CardCodeAndCount("01DE003", 2)); //  { CardCode = "01DE003", Count = 2 });
        deck4.add(new CardCodeAndCount("02DE003", 3)); // { CardCode = "02DE003", Count = 3 });
        deck4.add(new CardCodeAndCount("01DE002", 4)); //  { CardCode = "01DE002", Count = 4 });

        String code3 = LoRDeckEncoder.getCodeFromDeck(deck3);
        String code4 = LoRDeckEncoder.getCodeFromDeck(deck4);

        Assert.assertEquals(code3, code4);
    }

    @Test
    public void OrderShouldNotMatter2() {
        //importantly this order test includes more than 1 card with counts >3, which are sorted by card code and appending to the <=3 encodings.
        List<CardCodeAndCount> deck1 = new ArrayList<>();
        deck1.add(new CardCodeAndCount("01DE002", 4)); //  { CardCode = "01DE002", Count = 4 });
        deck1.add(new CardCodeAndCount("01DE003", 2)); //  { CardCode = "01DE003", Count = 2 });
        deck1.add(new CardCodeAndCount("02DE003", 3)); //  { CardCode = "02DE003", Count = 3 });
        deck1.add(new CardCodeAndCount("01DE004", 5)); //  { CardCode = "01DE004", Count = 5 });

        List<CardCodeAndCount> deck2 = new ArrayList<>();
        deck2.add(new CardCodeAndCount("01DE004", 5)); //  { CardCode = "01DE004", Count = 5 });
        deck2.add(new CardCodeAndCount("01DE003", 2)); // { CardCode = "01DE003", Count = 2 });
        deck2.add(new CardCodeAndCount("02DE003", 3)); //  { CardCode = "02DE003", Count = 3 });
        deck2.add(new CardCodeAndCount("01DE002", 4)); //{ CardCode = "01DE002", Count = 4 });

        String code1 = LoRDeckEncoder.getCodeFromDeck(deck1);
        String code2 = LoRDeckEncoder.getCodeFromDeck(deck2);

        Assert.assertEquals(code1, code2);
    }

    @Test
    public void BilgewaterSet() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 4)); // { CardCode = "01DE002", Count = 4 });
        deck.add(new CardCodeAndCount("02BW003", 2)); // { CardCode = "02BW003", Count = 2 });
        deck.add(new CardCodeAndCount("02BW010", 3)); // { CardCode = "02BW010", Count = 3 });
        deck.add(new CardCodeAndCount("01DE004", 5)); // { CardCode = "01DE004", Count = 5 });

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void MtTargonSet() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 4)); // { CardCode = "01DE002", Count = 4 });
        deck.add(new CardCodeAndCount("03MT003", 2)); // { CardCode = "03MT003", Count = 2 });
        deck.add(new CardCodeAndCount("03MT010", 3)); // { CardCode = "03MT010", Count = 3 });
        deck.add(new CardCodeAndCount("02BW004", 5)); // { CardCode = "02BW004", Count = 5 });

        String code = LoRDeckEncoder.getCodeFromDeck(deck);
        List<CardCodeAndCount> decoded = LoRDeckEncoder.getDeckFromCode(code);
        Assert.assertTrue(VerifyRehydration(deck, decoded));
    }

    @Test
    public void BadVersion() throws Base32.DecodingException {
        // make sure that a deck with an invalid version fails

        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 4)); // { CardCode = "01DE002", Count = 4 });
        deck.add(new CardCodeAndCount("01DE003", 2)); // { CardCode = "01DE003", Count = 2 });
        deck.add(new CardCodeAndCount("02DE003", 3)); // { CardCode = "02DE003", Count = 3 });
        deck.add(new CardCodeAndCount("01DE004", 5)); // { CardCode = "01DE004", Count = 5 });

        List<Byte> bytesFromDeck = new ArrayList<>(Arrays.asList(Base32.decode(LoRDeckEncoder.getCodeFromDeck(deck))));

        Byte[] formatAndVersion = new Byte[] { 88 }; // arbitrary invalid format/version
        List<Byte> result = new ArrayList<>(Arrays.asList(formatAndVersion));

        bytesFromDeck.remove(0); // remove the actual format/version
        result.addAll(bytesFromDeck); // replace with invalid one (not needed)

        try {
            Byte[] a = new Byte[result.size()];
            result.toArray(a);
            String badVersionDeckCode = Base32.encode(a);
            List<CardCodeAndCount> deckBad = LoRDeckEncoder.getDeckFromCode(badVersionDeckCode);
        } catch (IllegalArgumentException e) {
            String expectedErrorMessage = "The provided code requires a higher version of this library; please update.";
            Assert.assertEquals(expectedErrorMessage, e.getMessage());
        }
    }

    @Test
    public void BadCardCodes() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE02", 1)); // { CardCode = "01DE02", Count = 1 });

        try {
            String code = LoRDeckEncoder.getCodeFromDeck(deck);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }

        deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01XX002", 1)); // { CardCode = "01XX002", Count = 1 });

        try {
            String code = LoRDeckEncoder.getCodeFromDeck(deck);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }

        deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 0)); // { CardCode = "01DE002", Count = 0 });

        try {
            String code = LoRDeckEncoder.getCodeFromDeck(deck);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }
    }

    @Test
    public void BadCount() {
        List<CardCodeAndCount> deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", 0)); // { CardCode = "01DE002", Count = 0 });
        try {
            LoRDeckEncoder.getCodeFromDeck(deck);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }

        deck = new ArrayList<>();
        deck.add(new CardCodeAndCount("01DE002", -1)); // { CardCode = "01DE002", Count = -1 });
        try {
            LoRDeckEncoder.getCodeFromDeck(deck);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }
    }


    @Test
    public void GarbageDecoding() {
        String badEncodingNotBase32 = "I'm no card code!";
        String badEncoding32 = "ABCDEFG";
        String badEncodingEmpty = "";

        try {
            List<CardCodeAndCount> deck = LoRDeckEncoder.getDeckFromCode(badEncodingNotBase32);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }

        try {
            List<CardCodeAndCount> deck = LoRDeckEncoder.getDeckFromCode(badEncoding32);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {

        } catch (Exception ignore) {
            Assert.fail();
        }

        try {
            List<CardCodeAndCount> deck = LoRDeckEncoder.getDeckFromCode(badEncodingEmpty);
            Assert.fail();
        } catch (Exception ignore) {

        }

    }

    public boolean VerifyRehydration(List<CardCodeAndCount> d, List<CardCodeAndCount> rehydratedList) {
        if (d.size() != rehydratedList.size())
            return false;

        for(CardCodeAndCount cd : rehydratedList) {
            boolean found = false;
            for (CardCodeAndCount cc : d) {
                if (cc.cardCode.equals(cd.cardCode) && cc.count == cd.count) {
                    found = true;
                    break;
                }
            }
            if (!found)
                return false;

        }
        return true;
    }
}