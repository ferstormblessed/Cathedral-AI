package ai;

import de.fhkiel.ki.cathedral.game.Placement;

import java.util.List;

// class that links a possible placemet with the score of making the move
public class PlacementScore {
    Placement p;
    int score;

    public PlacementScore(Placement p, int score) {
        this.score = score;
        this.p = p;
    }

    public int getScore() {
        return score;
    }

    public Placement getPlacement() {
        return p;
    }
}
