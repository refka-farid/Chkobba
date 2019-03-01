package com.bravedroid.application.players;

import com.bravedroid.application.HumanUI;
import com.bravedroid.application.Validator;
import com.bravedroid.domain.Card;
import com.bravedroid.domain.TableCards;

public class HumanPlayer extends Player {
    private final Validator validator;
    private HumanUI humanUI;

    public HumanPlayer(HumanUI humanUI, Validator validator) {
        this.humanUI = humanUI;
        this.validator = validator;
    }

    @Override
    public boolean acceptFirstCard(Card firstCard) {
        return humanUI.acceptFirstCard(firstCard);
    }

    @Override
    public PlayAction play(TableCards tableCards) {
        PlayAction playAction = humanUI.play(tableCards, getHandCards());
        while (!isValid(playAction)) {
            playAction = humanUI.play(tableCards, getHandCards());
        }
        updateHandCards(playAction.getSelectedCardFromHandCards());
        return playAction;
    }

    private boolean isValid(PlayAction playAction) {
        return validator.isValid(playAction);
    }
}
