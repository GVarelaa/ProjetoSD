package SharedState;

public class Reward {
    private Position initialPosition;
    private Position finalPosition;
    private int value;

    public Reward(Position initPos, Position finalPos, int value) {
        this.initialPosition = initPos;
        this.finalPosition = finalPos;
        this.value = value;
    }

    public Position getInitialPosition() {
        return this.initialPosition.clone();
    }

    public Position getFinalPosition() {
        return this.finalPosition.clone();
    }

    public int getValue() {
        return this.value;
    }

    public void setInitialPosition(Position pos) {
        this.initialPosition = pos;
    }

    public void setFinalPosition(Position pos) {
        this.finalPosition = pos;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
