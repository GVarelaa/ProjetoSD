package Tests;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistentUsernameException;
import Exceptions.UsernameAlreadyExistsException;
import SharedState.Position;
import SharedState.Reservation;
import SharedState.ScooterManager;

public class Test1 {
    public static void main(String[] args) throws UsernameAlreadyExistsException, NonExistentUsernameException, NoScootersAvailableException {
        ScooterManager sm = new ScooterManager(20, 20, 2);
        sm.register("miguel", "mike");
        sm.login("miguel", "mike");

        for (int i=0; i<3; i++){
            Reservation r = sm.activateScooter(new Position(1,1), "miguel");
            if (r != null) {
                System.out.println(r.toString());
            }
        }
    }

}
