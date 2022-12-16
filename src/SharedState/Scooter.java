package SharedState;

import java.util.concurrent.locks.ReentrantLock;

public class Scooter {
    private Position position;
    private ReentrantLock lockScooter;

    public Scooter() {
        this.position = null;
        this.lockScooter = new ReentrantLock();
    }

    public Scooter(Position position) {
        this.position = position;
        this.lockScooter = new ReentrantLock();
    }

    public Scooter(Scooter other){
        this.position = other.position.clone();
        this.lockScooter = new ReentrantLock();
    }

    public Position getPosition() {
        return this.position.clone();
    }

    public void setPosition(Position pos) {
        this.position = position;
    }

    public Scooter clone(){
        return new Scooter(this);
    }
}
