package SharedState;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Notification {
    private boolean notificationsOn;
    private Position position;
    private ReentrantLock lockNotification;
    private Condition waitingForRewards;
    private List<Reward> allRewards;

    public Notification(Position pos, List<Reward> allRewards) {
        this.allRewards = allRewards; // Pointer to the collection
        this.notificationsOn = false;
        this.position = pos;
        this.lockNotification = new ReentrantLock();
        this.waitingForRewards = lockNotification.newCondition();
    }

    public Position getPosition() {
        return this.position.clone();
    }

    public void setPosition(Position pos) {
        this.position = pos;
    }

    public void setNotificationsOn(Boolean onOff){
        if (this.notificationsOn == false && onOff == true){
            this.notificationsOn = true; // Start waiting for rewards
        }
        else if (this.notificationsOn == true && onOff == false){
            this.notificationsOn = false; // Signal event
        }
        // else ignore
    }

    /**
     * Signals an event that may unblock waitingForRewards condition
     */
    public void signal(){
        this.waitingForRewards.signal(); // To be called when a reward is added
    }
}
