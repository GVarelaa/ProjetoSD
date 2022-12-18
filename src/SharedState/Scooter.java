package SharedState;

import java.util.concurrent.locks.ReentrantLock;

public class Scooter {
    private Position position;
    private ReentrantLock lockScooter;
    private boolean isFree;

    public Scooter() {
        this.position = null;
        this.lockScooter = new ReentrantLock();
        this.isFree = true;
    }

    public Scooter(Position position) {
        this.position = position;
        this.lockScooter = new ReentrantLock();
        this.isFree = true; // mudar
    }

    public Scooter(Scooter other){
        this.position = other.position.clone();
        this.lockScooter = new ReentrantLock();
        this.isFree = true; // mudar
    }

    public Position getPosition() {
        return this.position.clone();
    }

    public void setPosition(Position pos) {
        this.position = position;
    }

    public boolean getIsFree() { return this.isFree; }

    public void setIsFree(boolean b) { this.isFree = b; }

    public Scooter clone(){
        return new Scooter(this);
    }
}
