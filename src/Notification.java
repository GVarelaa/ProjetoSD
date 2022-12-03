public class Notification {
    private boolean notificationsOn;
    private Position position;

    public Notification(boolean notificationsOn, Position pos) {
        this.notificationsOn = notificationsOn;
        this.position = pos;
    }

    public boolean isNotificationsOn() {
        return this.notificationsOn;
    }

    public Position getPosition() {
        return this.position.clone();
    }

    public void setNotificationsOn(boolean notificationsOn) {
        this.notificationsOn = notificationsOn;
    }

    public void setPosition(Position pos) {
        this.position = pos;
    }
}
