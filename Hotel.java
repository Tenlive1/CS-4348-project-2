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
    static Semaphore Guest_rdy;
    static Semaphore front_done;
    static Semaphore Givesbagstobell;
    static Semaphore receivebags;
    static Semaphore EnterRoom;
    static Semaphore deliver;
    static Semaphore givingtips;
    static Semaphore bellhop_done;

    //mutex
    static Semaphore Guest_mutex;
    static Semaphore front_mutex;
    static Semaphore bell_mutex;


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

        Guest_rdy = new Semaphore(0);
        front_done = new Semaphore(0);
        Givesbagstobell = new Semaphore(0);
        receivebags = new Semaphore(0);
        EnterRoom = new Semaphore(0);
        deliver = new Semaphore(0);
        givingtips = new Semaphore(0);
        bellhop_done = new Semaphore(0);


        Guest_mutex = new Semaphore(1);
        front_mutex = new Semaphore(1);
        bell_mutex = new Semaphore(1);

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

            try {
                while(true){
                    Hotel.Givesbagstobell.acquire();
                    Hotel.bell_mutex.acquire();

                    Guest g = Hotel.BellHopWait.remove();
                    g.Bellhop_ID = EmployeeID;
                
                    Hotel.bell_mutex.release();

                    System.out.println(Role + EmployeeID + " receives bags from "+ g.Role + g.GuestID);

                    Hotel.receivebags.release();
                    Hotel.EnterRoom.acquire();
                    System.out.println(Role + EmployeeID + " delivers bags to " + g.Role + g.GuestID);
                    Hotel.deliver.release();
                    Hotel.givingtips.acquire();
                    System.out.println(g.Role + g.GuestID + " receives bags from " + Role + EmployeeID + " and gives tip");
                    Hotel.bellhop_done.release();
                    Hotel.bellhop_available.release();

                }
            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    static class Front_desk implements Runnable{
        
        int EmployeeID;
        Hotel sim;
        Thread FrontEmployeeThread;
        static int RoomNum = 1;
        String Role ="Front desk employee ";
        static int assignroomnumber = 0;

        Front_desk(int ID, Hotel sim){
            EmployeeID = ID;
            FrontEmployeeThread = new Thread(this); // creating the thread

            System.out.println(Role + EmployeeID + " created");
            FrontEmployeeThread.start();//starting the thread and it will go to the public void run
        }

        public void run() {
            try {
                while(true){
                    Hotel.Guest_rdy.acquire();
                    Hotel.front_mutex.acquire();
                    Guest g = Hotel.FrontDeskWait.remove();
                    g.front_ID = EmployeeID;
                    g.Roomnumber = assignroomnumber;
                    assignroomnumber++;
                    Hotel.front_mutex.release();

                    System.out.println(Role + EmployeeID + " registers " + g.Role + g.GuestID + " and assigns room " + assignroomnumber);

                    Hotel.front_done.release();
                    Hotel.front_available.release();

                }


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
        int front_ID;
        int Bellhop_ID;
        int Roomnumber;


        Guest(int ID, Hotel sim){
            Ran = new Random();
            GuestID = ID;
            bags = Ran.nextInt(6); // range is from 0-5
            GuestThreads = new Thread(this);
            System.out.println(Role + GuestID + " created");
            GuestThreads.start();
        }

        public void run(){
           try {
            EnterHotel();
            Hotel.Guest_mutex.acquire();
            Hotel.FrontDeskWait.add(this); // adding the guest into the queue for the front
            Hotel.Guest_mutex.release();


            Hotel.front_available.acquire();// seeing if the front is available
            Hotel.Guest_rdy.release(); // guest is ready once the fron is ready
            Hotel.front_done.acquire(); // front is done
            System.out.println(Role + GuestID + " receives room key for room " + Roomnumber + " from front desk employee " + front_ID);


            if(bags > 1){
                Hotel.bellhop_available.acquire();
                Hotel.Guest_mutex.acquire();
                Hotel.BellHopWait.add(this);
                System.out.println(Role + GuestID + " request help with bags");
                Hotel.Guest_mutex.release();
                Hotel.Givesbagstobell.release();
                Hotel.receivebags.acquire();
                System.out.println(Role + GuestID + " enter room " + Roomnumber);
                Hotel.EnterRoom.release();
                Hotel.deliver.acquire();
                Hotel.givingtips.acquire();
                Hotel.bellhop_done.acquire();
            }else{
                System.out.println(Role + GuestID + " enter room " + Roomnumber);
            }
            System.out.println(Role + GuestID + " retires for the evening");

            
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
