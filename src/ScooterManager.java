import SharedState.Position;
import SharedState.Reservation;

import java.io.IOException;
import java.util.List;

public interface ScooterManager {

    public void start() throws IOException;

    public void close() throws IOException;

    public boolean register(String username, String password) throws IOException, InterruptedException;

    public boolean login(String username, String password) throws IOException, InterruptedException;

    public List<Position> listFreeScooters(Position p) throws IOException, InterruptedException;

    public List<List<Position>> listRewards(Position p) throws IOException, InterruptedException;

    public Reservation activateScooter(Position p) throws IOException, InterruptedException;

    public double parkScooter(Position p, int codReservation) throws IOException, InterruptedException;

    public void turnOnOffNotifications(boolean onOff, Position p);

    public void waitForNotifications();
}
