package com.bravedroid.application;

import com.bravedroid.application.players.PlayAction;
import com.bravedroid.application.players.Player;
import com.bravedroid.domain.Card;
import com.bravedroid.domain.Rules;
import com.bravedroid.domain.Score;
import com.bravedroid.domain.TableCards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreRecorder {
    private Map<Player, Score> scoreMap;

    public ScoreRecorder(List<Player> listPlayer) {
        scoreMap = new HashMap<>();
        scoreMap.put(listPlayer.get(0), new Score());
        scoreMap.put(listPlayer.get(1), new Score());
    }

    public void updateScore(Player player, PlayAction playAction, TableCards tableCards) {
        Score score = scoreMap.get(player);
        final Card selectedCardFromHandCards = playAction.getSelectedCardFromHandCards();
        final List<Card> eatenCardsList = playAction.getEatenCardsList();
        final List<Card> allEatenCardsList = new ArrayList<>();
        allEatenCardsList.add(selectedCardFromHandCards);
        allEatenCardsList.addAll(eatenCardsList);

        for (Card card : allEatenCardsList) {
            if (Rules.isDinery(card)) {
                score.dinaryCount += 1;
            }
            if (Rules.isBermila(card)) {
                score.bermilaCount += 1;
            }
            if (Rules.isSabaaHaya(card)) {
                score.hasSabaHaya = true;
            }
        }
        score.cartaCount += allEatenCardsList.size();
        if (tableCards.isEmpty()) {
            score.chkobbaCount += 1;
        }
    }
}
