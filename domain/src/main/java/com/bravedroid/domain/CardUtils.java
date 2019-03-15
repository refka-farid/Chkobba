package com.bravedroid.domain;

import java.util.List;

public class CardUtils {
    public static int getSumValueCards(List<Card> eatenCardsListMultiple) {
        int eatenSumCards = 0;
        for (Card card : eatenCardsListMultiple) {
            eatenSumCards += card.getValue();
        }
        return eatenSumCards;
    }
}
