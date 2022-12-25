package SharedState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Reward {
    private Position initialPosition;
    private Position finalPosition;
    private double value;

    public Reward(Position initPos, Position finalPos) {
        this.initialPosition = initPos;
        this.finalPosition = finalPos;
        this.value = this.calculateValue();
    }

    public Position getInitialPosition() {
        return this.initialPosition.clone();
    }

    public Position getFinalPosition() {
        return this.finalPosition.clone();
    }

    public double getValue() {
        return this.value;
    }

    public void setInitialPosition(Position pos) {
        this.initialPosition = pos;
    }

    public void setFinalPosition(Position pos) {
        this.finalPosition = pos;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private double calculateValue(){
        double distance = this.initialPosition.distanceTo(this.finalPosition);
        return distance * 1; // 1 euro / km ??
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(16);
        DataOutputStream os = new DataOutputStream(byteArray);

        os.write(this.initialPosition.serialize());
        os.write(this.finalPosition.serialize());

        return byteArray.toByteArray();
    }
}
