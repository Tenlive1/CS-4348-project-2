//acquire is wait
//release is signal


import java.util.concurrent.Semaphore;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Hotel implements Runnable
{
    //amount of guest,bellhop and front desk
    final static int MaxGuest = 25;
    final static int MaxEmployee = 2;

    // semaphore
    static Semaphore front_available;
    static Semaphore bellhop_available; 
    static Semaphore register;
    static Semaphore assign_room;
    static Semaphore assistance; 
    static Semaphore enter;
    static Semaphore gives;
    static Semaphore tips;

    //mutex
    static Semaphore forguest;
    static Semaphore forfront;
    static Semaphore forbell;


    //queue
    static Queue<Guest> FrontDeskWait;
    static Queue<Guest> BellHopWait;

    //array
    static int [] front_Identifier; // this will tell the guest which front they enteracted
    static int [] bellhop_Identifier; // this will tell the guest which bellhop they enteracted

    Hotel(){
        Thread hotelThread = new Thread(this);
        hotelThread.start();
    }

    public void run(){
        front_available = new Semaphore(2);
        bellhop_available = new Semaphore(2);
        register = new Semaphore(0);
        assign_room = new Semaphore(0);
        assistance = new Semaphore(0);
        enter = new Semaphore(0);
        gives = new Semaphore(0);
        tips = new Semaphore(0);
        forguest = new Semaphore(1);

        FrontDeskWait = new LinkedList<>();
        BellHopWait = new LinkedList<>();

        front_Identifier = new int[MaxGuest];
        bellhop_Identifier = new int[MaxGuest];
        for(int x=0; x<MaxGuest; x++){

            front_Identifier[x] = 0;
            bellhop_Identifier[x] = 0;
        }


    }
    
    public static void main(String args[]){

        

        Hotel sim = new Hotel();
        Front_desk front_employee[] = new Front_desk[MaxEmployee];
        Bellhop bell_employee[] = new Bellhop[MaxEmployee];
        Guest customer[] = new Guest[MaxGuest];


        for(int x=0; x<MaxEmployee; x++){
            front_employee[x] = new Front_desk(x, sim);// this is how the front desk thread will start
        }

        for(int x=0; x<MaxEmployee; x++){
            bell_employee[x] = new Bellhop(x, sim);
        }
         for(int x=0; x<MaxGuest; x++){
            customer[x] = new Guest(x, sim);
        }


        
        

    }



    static class Bellhop implements Runnable {

        int EmployeeID;
        Hotel sim;
        Thread BellHopThreads;
        String Role = "BellHop ";

        Bellhop(int ID, Hotel sim){
            EmployeeID = ID;

            BellHopThreads = new Thread(this);
            System.out.println(Role + EmployeeID + " created");
            BellHopThreads.start();
        }
      
        public void run() {
        }
    }

    static class Front_desk implements Runnable{
        
        int EmployeeID;
        Hotel sim;
        Thread FrontEmployeeThread;
        static int RoomNum = 0;
        String Role ="Front desk employee ";

        Front_desk(int ID, Hotel sim){
            EmployeeID = ID;
            FrontEmployeeThread = new Thread(this); // creating the thread

            System.out.println(Role + EmployeeID + " created");
            FrontEmployeeThread.start();//starting the thread and it will go to the public void run
        }

        public void run() {
            try {
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    static class Guest implements Runnable{
        
        int GuestID;
        Random Ran;
        int bags;
        Thread GuestThreads;
        String Role = "Guest ";

        Guest(int ID, Hotel sim){
            Ran = new Random();
            GuestID = ID;
            bags = Ran.nextInt(5) +1; // range is from 1-5
            GuestThreads = new Thread(this);
            System.out.println(Role + GuestID + " created");
            GuestThreads.start();
        }

        public void run(){
           try {
            EnterHotel();
            Hotel.forguest.acquire();
            Hotel.FrontDeskWait.add(this); // adding the guest into the queue for the front
            Hotel.forguest.release();
           } catch (Exception e) {
            // TODO: handle exception
           }
        }
       public void EnterHotel(){
        if(bags <= 1){
            System.out.println(Role + GuestID + " enter hotel with " + bags + " bag");
        }else{
            System.out.println(Role + GuestID + " enter hotel with " + bags + " bags");
        }
       }
    }


}
