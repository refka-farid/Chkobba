package com.bravedroid.application;

import com.bravedroid.application.players.BotPlayer;
import com.bravedroid.application.players.HumanPlayer;
import com.bravedroid.application.players.PlayAction;
import com.bravedroid.application.players.Player;
import com.bravedroid.domain.*;
import com.bravedroid.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.bravedroid.application.players.PlayAction.Action.THROW_CARD;
import static com.bravedroid.util.Helper.repeat;

public class Game implements Validator {
    private static Game instance;
    private Logger logger;

    private HumanPlayer humanPlayer;
    private BotPlayer botPlayer;
    private List<Player> listPlayer;

    private TableCards tableCards;
    private ScoreRecorder scoreRecorder;

    private Game() {
    }

    public static Game getInstance() {
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public TableCards getTableCards() {
        return tableCards;
    }

    public void initialize(HumanUI humanUI, Logger logger) {
        this.logger = logger;
        createHumanPlayer(humanUI);
        createBotPlayer();
        selectFirstPlayer();
        createScoreRecorder();
        createTableCards();
    }

    private void createHumanPlayer(HumanUI humanUI) {
        humanPlayer = new HumanPlayer(humanUI, this);
    }

    private void createBotPlayer() {
        botPlayer = new BotPlayer();
    }

    private void selectFirstPlayer() {
        Random rand = new Random();
        int randomInt = rand.nextInt(10);
        if (randomInt % 2 == 0) {
            listPlayer = Arrays.asList(humanPlayer, botPlayer);
            logger.log("first player is human ");
        } else {
            listPlayer = Arrays.asList(botPlayer, humanPlayer);
            logger.log("first player is bot ");
        }
    }

    private void createScoreRecorder() {
        scoreRecorder = new ScoreRecorder(listPlayer);
    }

    private void createTableCards() {
        tableCards = new TableCards();
    }

    public void start() {
        Cards.getInstance().shuffle();
        final boolean isFirstCardAccepted = askFirstPlayerToAcceptFirstCard();
        giveFirstRoundCards(isFirstCardAccepted);
        playRound();
        //giveNextRoundCards();
    }

    private void playRound() {
        repeat(() -> {
            for (Player player : listPlayer) {
                final PlayAction playAction = player.play(tableCards);
                if (isValid(playAction)) {
                    updateTableCards(playAction);
                    updateScore(player, playAction);
                }
            }
        }, 3);
    }

    private void updateTableCards(PlayAction action) {
        final List<Card> eatenCardsListPlayer = action.getEatenCardsList();
        final Card selectedCardFromPlayerHandCards = action.getSelectedCardFromHandCards();
        if (action.getAction() != THROW_CARD) {
            tableCards.getCardList().removeAll(eatenCardsListPlayer);
        } else {
            tableCards.getCardList().add(selectedCardFromPlayerHandCards);
        }
    }

    private void updateScore(Player player, PlayAction playAction) {
        if (playAction.getAction() != THROW_CARD) {
            scoreRecorder.updateScore(player, playAction, tableCards);
        }
    }

    private void giveFirstRoundCards(boolean isFirstCardAccepted) {
        Player firstPlayer = listPlayer.get(0);
        Player secondPlayer = listPlayer.get(1);
        List<Card> firstPlayerCardList = new ArrayList<>();
        List<Card> secondPlayerCardList;
        List<Card> tableCardList = new ArrayList<>();

        if (isFirstCardAccepted) {
            firstPlayerCardList.add(Cards.getInstance().getFirstCard());
            firstPlayerCardList.addAll(Cards.getInstance().getCardsRange(1, 2));
            logger.log("first player cards after accept first card " + firstPlayerCardList);

            tableCardList = Cards.getInstance().getCardsRange(3, 4);
            tableCards.takeCards(tableCardList);
            logger.log("table cards  " + tableCardList);
        } else {
            tableCardList.add(Cards.getInstance().getFirstCard());
            tableCardList.addAll(Cards.getInstance().getCardsRange(1, 3));
            tableCards.takeCards(tableCardList);
            logger.log("table cards " + tableCardList);

            firstPlayerCardList.addAll(Cards.getInstance().getCardsRange(4, 3));
            firstPlayer.takeHandCards(new HandCards(firstPlayerCardList));
            logger.log("first player cards after reject first card " + firstPlayerCardList);
        }
        secondPlayerCardList = Cards.getInstance().getCardsRange(7, 3);
        secondPlayer.takeHandCards(new HandCards(secondPlayerCardList));
        logger.log("second player cards " + secondPlayerCardList);
    }

    //private void giveNextRoundCards() {
    //    final ArrayList<Card> cardList = new ArrayList<>();
    //    int round = 0;
    //    int startIndex = 7;
    //    final int count = 3;
    //    while (round < 5) {
    //        cardList.addAll(Cards.getInstance().getCardsRange(startIndex, count));
    //        firstPlayer.takeHandCards(new HandCards(cardList));
    //        startIndex += 3;
    //        cardList.addAll(Cards.getInstance().getCardsRange(startIndex, count));
    //        secondPlayer.takeHandCards(new HandCards(cardList));
    //        round++;
    //    }
    //}

    private boolean askFirstPlayerToAcceptFirstCard() {
        Player firstPlayer = listPlayer.get(0);
        return firstPlayer.acceptFirstCard(Cards.getInstance().getFirstCard());
    }

    @Override
    public boolean isValid(PlayAction playAction) {
        Card selectedCardFromHandCards;
        List<Card> eatenCardsList;
        switch (playAction.getAction()) {
            case ONE_TO_ONE_EAT:
                selectedCardFromHandCards = playAction.getSelectedCardFromHandCards();
                eatenCardsList = playAction.getEatenCardsList();

                return (eatenCardsList.get(0).getValue() == selectedCardFromHandCards.getValue());
            case ONE_TO_MULTIPLE_EAT:
                selectedCardFromHandCards = playAction.getSelectedCardFromHandCards();
                eatenCardsList = playAction.getEatenCardsList();
                int sumValueCards = CardUtils.getSumValueCards(eatenCardsList);

                return !canEatOneToOne(tableCards, selectedCardFromHandCards.getValue())
                        && sumValueCards == selectedCardFromHandCards.getValue();
            case THROW_CARD:
                return tableCards.getCardList().isEmpty()
                        || !canEatCards(tableCards.getCardList(), playAction.getSelectedCardFromHandCards());
        }
        throw new IllegalArgumentException();
    }

    private boolean canEatOneToOne(TableCards tableCards, int cardValue) {
        boolean canEatOneToOne = false;
        for (Card element : tableCards.getCardList()) {
            canEatOneToOne = element.getValue() == cardValue;
        }
        return canEatOneToOne;
    }

    private boolean canEatCards(List<Card> cardList, Card thrownCard) {
        final List<Integer> integerList = mapFromListOfCardToIntegerList(cardList);
        return hasSum(integerList, thrownCard.getValue());
    }

    private List<Integer> mapFromListOfCardToIntegerList(List<Card> tableList) {
        List<Integer> integerList = new ArrayList<>();
        for (Card item : tableList) {
            integerList.add(item.getValue());
        }
        return integerList;
    }

    private boolean hasSum(List<Integer> intList, int x) {
        if (intList.isEmpty()) return false;
        if (intList.size() == 1) return intList.get(0) == x;
        for (int element : intList) {
            if (element == x) return true;
        }
        switch (x) {
            case 2:
                return
                        hasSumByTuple2(intList, x);
            case 3:
                return
                        hasSumByTuple2(intList, x) || hasSumByTuple3(intList, x);
            case 4:
                return
                        hasSumByTuple2(intList, x) || hasSumByTuple3(intList, x)
                                || hasSumByTuple4(intList, x);
            case 5:
                return
                        hasSumByTuple2(intList, x) || hasSumByTuple3(intList, x)
                                || hasSumByTuple4(intList, x) || hasSumByTuple5(intList, x);
            case 6:
                return
                        hasSumByTuple2(intList, x) || hasSumByTuple3(intList, x) || hasSumByTuple4(intList, x)
                                || hasSumByTuple5(intList, x) || hasSumByTuple6(intList, x);
            case 7:
            case 8:
            case 9:
            case 10:
                return
                        hasSumByTuple2(intList, x) || hasSumByTuple3(intList, x) || hasSumByTuple4(intList, x)
                                || hasSumByTuple5(intList, x) || hasSumByTuple6(intList, x) || hasSumByTuple7(intList, x);
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean hasSumByTuple7(List<Integer> intList, int x) {
        for (int index1 = 0; index1 <= intList.size() - 7; index1++) {
            for (int index2 = index1 + 1; index2 <= intList.size() - 6; index2++) {
                for (int index3 = index2 + 1; index3 <= intList.size() - 5; index3++) {
                    for (int index4 = index3 + 1; index4 <= intList.size() - 4; index4++) {
                        for (int index5 = index4 + 1; index5 <= intList.size() - 3; index5++) {
                            for (int index6 = index5 + 1; index6 <= intList.size() - 2; index6++) {
                                for (int index7 = index6 + 1; index7 <= intList.size() - 1; index7++) {
                                    int tuple7 = intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4) + intList.get(index5) + intList.get(index6) + intList.get(index7);
                                    logger.log(intList.get(index1));
                                    logger.log(intList.get(index2));
                                    logger.log(intList.get(index3));
                                    logger.log(intList.get(index4));
                                    if (tuple7 == x) {
                                        logger.log("x = " + intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4) + intList.get(index5));
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSumByTuple6(List<Integer> intList, int x) {
        for (int index1 = 0; index1 <= intList.size() - 6; index1++) {
            for (int index2 = index1 + 1; index2 <= intList.size() - 5; index2++) {
                for (int index3 = index2 + 1; index3 <= intList.size() - 4; index3++) {
                    for (int index4 = index3 + 1; index4 <= intList.size() - 3; index4++) {
                        for (int index5 = index4 + 1; index5 <= intList.size() - 2; index5++) {
                            for (int index6 = index5 + 1; index6 <= intList.size() - 1; index6++) {
                                int fifthSum = intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4) + intList.get(index5) + intList.get(index6);
                                logger.log(intList.get(index1));
                                logger.log(intList.get(index2));
                                logger.log(intList.get(index3));
                                logger.log(intList.get(index4));
                                if (fifthSum == x) {
                                    logger.log("x = " + intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4) + intList.get(index5) + intList.get(index6));
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSumByTuple5(List<Integer> intList, int x) {
        for (int index1 = 0; index1 <= intList.size() - 5; index1++) {
            for (int index2 = index1 + 1; index2 <= intList.size() - 4; index2++) {
                for (int index3 = index2 + 1; index3 <= intList.size() - 3; index3++) {
                    for (int index4 = index3 + 1; index4 <= intList.size() - 2; index4++) {
                        for (int index5 = index4 + 1; index5 <= intList.size() - 1; index5++) {
                            int fifthSum = intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4) + intList.get(index5);
                            logger.log(intList.get(index1));
                            logger.log(intList.get(index2));
                            logger.log(intList.get(index3));
                            logger.log(intList.get(index4));
                            if (fifthSum == x) {
                                logger.log("x = " + intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4) + intList.get(index5));
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSumByTuple4(List<Integer> intList, int x) {
        for (int index1 = 0; index1 <= intList.size() - 4; index1++) {
            for (int index2 = index1 + 1; index2 <= intList.size() - 3; index2++) {
                for (int index3 = index2 + 1; index3 <= intList.size() - 2; index3++) {
                    for (int index4 = index3 + 1; index4 <= intList.size() - 1; index4++) {
                        int forthSum = intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4);
                        logger.log(intList.get(index1));
                        logger.log(intList.get(index2));
                        logger.log(intList.get(index3));
                        logger.log(intList.get(index4));
                        if (forthSum == x) {
                            logger.log("x = " + intList.get(index1) + intList.get(index2) + intList.get(index3) + intList.get(index4));
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSumByTuple3(List<Integer> intList, int x) {
        for (int index1 = 0; index1 <= intList.size() - 3; index1++) {
            for (int index2 = index1 + 1; index2 < intList.size() - 2; index2++) {
                for (int index3 = index2 + 1; index3 <= intList.size() - 1; index3++) {
                    int tripleSum = intList.get(index1) + intList.get(index2) + intList.get(index3);
                    logger.log(intList.get(index1));
                    logger.log(intList.get(index2));
                    logger.log(intList.get(index3));
                    if (tripleSum == x) {
                        logger.log("x = " + intList.get(index1) + intList.get(index2) + intList.get(index3));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasSumByTuple2(List<Integer> intList, int x) {
        for (int index1 = 0; index1 <= intList.size() - 2; index1++) {
            for (int index2 = index1 + 1; index2 <= intList.size() - 1; index2++) {
                int coupleSum = intList.get(index1) + intList.get(index2);
                logger.log(intList.get(index1));
                logger.log(intList.get(index2));
                if (coupleSum == x) {
                    logger.log("x = " + intList.get(index1) + intList.get(index2));
                    return true;
                }
            }
        }
        return false;
    }
}
