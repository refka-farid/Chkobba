package com.bravedroid.domain;

import static com.bravedroid.domain.Card.CardType.CARREAU;

public class Rules {
    public static boolean isSabaaHaya(Card card) {
        return (card.equals(Card.Factory.create(CARREAU, 7)));
    }

    public static boolean isDinery(Card card) {
        return (card.getType() == CARREAU);
    }

    public static boolean isBermila(Card card) {
        return (card.getValue() == 7);
    }
}
