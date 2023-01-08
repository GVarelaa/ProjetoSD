package Tests;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistentUsernameException;
import Exceptions.UsernameAlreadyExistsException;
import SharedState.Position;
import SharedState.Reservation;
import SharedState.ScooterManager;

public class Test1 {
    public static void main(String[] args) throws UsernameAlreadyExistsException, NonExistentUsernameException, InterruptedException {
        ScooterManager sm = new ScooterManager(20, 20, 10);
        sm.register("miguel", "mike");
        sm.login("miguel", "mike");

        int threadNum = 10;
        Thread t[] = new Thread[threadNum];

        for (int i=0; i<threadNum; i++){
            t[i] = new Thread(() -> {
                Reservation r=null;
                try{
                    r = sm.activateScooter(new Position(1,1), "miguel");
                } catch(Exception e){
                    System.out.println(e.getMessage());
                }
                if (r != null) System.out.println(r.toString());
            });
            t[i].start();
        }

        for(int i=0; i<threadNum; i++){
            t[i].join();
        }

        System.out.println("Terminei");


    }

}
