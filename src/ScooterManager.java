import SharedState.Position;
import SharedState.Reservation;

import java.io.IOException;
import java.util.List;

public interface ScooterManager {

    public void close() throws IOException;

    public boolean register(String username, String password);

    public boolean login(String username, String password);

    public List<Position> listFreeScooters(Position p);

    public List<List<Position>> listRewards(Position p);

    public Reservation activateScooter(Position p);

    public double parkScooter(Position p, int codReservation);

    public void turnOnOffNotifications(boolean onOff, Position p);

    public void waitForNotifications();
}
