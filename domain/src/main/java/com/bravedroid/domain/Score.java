package com.bravedroid.domain;

public class Score {
    public int dinaryCount;
    public int bermilaCount;
    public int cartaCount;
    public boolean hasSabaHaya;
    public int chkobbaCount;
    public int total;

    /**
     * calculateTotalScore() for iteration round 6 should be called
     */
    public void calculateTotalScore() {
        if (dinaryCount > 5) total += 1;
        if (bermilaCount > 2) total += 1;
        if (cartaCount > 20) total += 1;
        if (hasSabaHaya) total += 1;
        total += chkobbaCount;
    }
}
