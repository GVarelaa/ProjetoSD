package Tests;

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
                try{
                    sm.register(Integer.toString(finalI), "pass");
                    boolean validLogin = sm.login(Integer.toString(finalI), "pass");
                    if (validLogin == true){
                        System.out.println(Thread.currentThread().getName() + " could register and login!");
                    }
                    else {
                        System.out.println("Fail in thread " + Thread.currentThread().getName());
                    }
                } catch(Exception e){
                    System.out.println(e.getMessage());
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
