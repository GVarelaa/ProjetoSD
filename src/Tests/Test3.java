package Tests;

import Exceptions.NonExistentUsernameException;
import Exceptions.UsernameAlreadyExistsException;
import Exceptions.WrongPasswordException;
import SharedState.Position;
import SharedState.Reservation;
import SharedState.ScooterManagerImpl;

public class Test3 {
    public static void main(String[] args) throws InterruptedException {
        ScooterManagerImpl sm = new ScooterManagerImpl(20, 20, 10);

        int threadNum = 1000;
        Thread t[] = new Thread[threadNum];

        for (int i=0; i<threadNum; i++){
            int finalI = i;
            t[i] = new Thread(() -> {
                try {
                    sm.register(Integer.toString(finalI), "pass");
                    sm.login(Integer.toString(finalI), "pass");
                    System.out.println(Thread.currentThread().getName() + " could register and login!");

                } catch (Exception e) {
                    System.out.println("Fail in thread " + Thread.currentThread().getName());
                }
            });
            t[i].start();
            //Thread.sleep(10);
        }

        for(int i=0; i<threadNum; i++){
            t[i].join();
        }

        System.out.println("Terminei");
    }
}
