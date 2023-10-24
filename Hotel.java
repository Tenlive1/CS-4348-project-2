//acquire is wait
//release is signal


import java.util.concurrent.Semaphore;
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
    static Semaphore GuestLeftfront;

    //mutex
    static Semaphore Guest_mutex;
    static Semaphore completed_mutex;
    static Semaphore front_mutex;
    static Semaphore bell_mutex;
    static Semaphore A;
    static Semaphore testing;
    static Semaphore Bellhop_got_tips;



    //queue
    static Queue<Guest> FrontDeskWait;// Queue for the guest to enter when the go to the front desk this is how the front desk people will get the guest info
    static Queue<Guest> BellHopWait;// Queue for the guest to enter so this way the bellhop can get the information of the guest bags and it is in order
    static Queue<Guest> Guest_retire;// queue for the guest to enter when they are done so this way main can join the the thread.

   

    Hotel(){// makinng a thread for the hotel to run
        Thread hotelThread = new Thread(this);
        hotelThread.start();
    }

    public void run(){
        //initilizing the semaphore
        front_available = new Semaphore(2,true);
        bellhop_available = new Semaphore(2,true);

        Guest_rdy = new Semaphore(0,true);
        front_done = new Semaphore(0,true);
        Givesbagstobell = new Semaphore(0,true);
        receivebags = new Semaphore(0,true);
        EnterRoom = new Semaphore(0,true);
        deliver = new Semaphore(0,true);
        givingtips = new Semaphore(0,true);
        bellhop_done = new Semaphore(0,true);
        GuestLeftfront = new Semaphore(0,true);
        Bellhop_got_tips=new Semaphore(0,true);
        

        A = new Semaphore(0,true);
        //initilizing the mutex
        Guest_mutex = new Semaphore(1,true);
        front_mutex = new Semaphore(1,true);
        bell_mutex = new Semaphore(1,true);
        completed_mutex = new Semaphore(1, true);
        testing = new Semaphore(1, true);
        // making the queue
        FrontDeskWait = new LinkedList<>();
        BellHopWait = new LinkedList<>();
        Guest_retire = new LinkedList<>();



    }
    
    public static void main(String args[]){


        Hotel sim = new Hotel();
        Front_desk front_employee[] = new Front_desk[MaxEmployee];
        Bellhop bell_employee[] = new Bellhop[MaxEmployee];
        Guest customer[] = new Guest[MaxGuest];

        int counter =0;


        try {
            
            testing.release();
        } catch (Exception e) {
            System.out.println("testing");
        }
        for(int x=0; x<MaxEmployee; x++){
            front_employee[x] = new Front_desk(x, sim);// this is how the front desk thread will start
        }

        for(int x=0; x<MaxEmployee; x++){
            bell_employee[x] = new Bellhop(x, sim);
        }
         for(int x=0; x<MaxGuest; x++){
            customer[x] = new Guest(x, sim);
        }


        while(true){
            System.out.println(counter);
            try {
                
                if(counter == 25){
                    break;
                }
                A.acquire();
                Guest g = Hotel.Guest_retire.remove();
                g.GuestThreads.join();
                System.out.println(g.Role + g.GuestID + " joined");
                counter++;
            } catch (Exception e) {
            }
            
        }

        
        System.out.println("exit");
        System.exit(0);
        

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
                    Hotel.bell_mutex.acquireUninterruptibly();

                    Guest g = Hotel.BellHopWait.remove();
                    g.Bellhop_ID = EmployeeID;
                
                    Hotel.bell_mutex.release();

                    System.out.println(Role + EmployeeID + " receives bags from "+ g.Role + g.GuestID);

                    Hotel.receivebags.release();
                    Hotel.EnterRoom.acquire();
                    System.out.println(Role + EmployeeID + " delivers bags to " + g.Role + g.GuestID);
                    Hotel.deliver.release();
                    Hotel.bellhop_done.release();
                    Hotel.bellhop_available.release();

                }
            } catch (Exception e) {
                System.out.println("caught something in bellhop");
            }

        }
    }

    static class Front_desk implements Runnable{
        
        int EmployeeID;
        Hotel sim;
        Thread FrontEmployeeThread;
        String Role ="Front desk employee ";
        static int assignroomnumber = 0;

        Front_desk(int ID, Hotel sim){
            EmployeeID = ID;
            FrontEmployeeThread = new Thread(this); // creating the thread

            System.out.println(Role + EmployeeID + " created");
            FrontEmployeeThread.start();//starting the thread and it will go to the public void run
        }

        public void run() {
            while(true){
                try {
                    Hotel.Guest_rdy.acquire();
                    Hotel.front_mutex.acquire();
                    Guest g = Hotel.FrontDeskWait.remove();
                    g.front_ID = EmployeeID;
                    assignroomnumber++;

                    g.Roomnumber = assignroomnumber; 
                    
                    Hotel.front_mutex.release();
                    System.out.println(Role + EmployeeID + " registers " + g.Role + g.GuestID + " and assigns room " + g.Roomnumber);

                    Hotel.front_done.release();

                    Hotel.GuestLeftfront.acquire();
                    Hotel.front_available.release();
                } catch (Exception e) {
                    System.out.println("caught front");
                }
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
            Hotel.Guest_mutex.acquire();
            Hotel.FrontDeskWait.add(this); // adding the guest into the queue for the front
            EnterHotel();
            Hotel.Guest_mutex.release();


            Hotel.front_available.acquire();// seeing if the front is available
            Hotel.Guest_rdy.release(); // guest is ready once the fron is ready
            System.out.println(Role + GuestID + " receives room key for room " + Roomnumber + " from front desk employee " + front_ID);
            Hotel.front_done.acquire(); // front is done
            Hotel.GuestLeftfront.release();


            if(bags > 2){
                Hotel.testing.acquire();
                Hotel.BellHopWait.add(this);
                System.out.println(Role + GuestID + " request help with bags");
                Hotel.testing.release();
                Hotel.bellhop_available.acquire();
                Hotel.Givesbagstobell.release();
                Hotel.receivebags.acquire();
                System.out.println(Role + GuestID + " enter room " + Roomnumber);
                Hotel.EnterRoom.release();
                Hotel.deliver.acquire();
                //wait for bell to get tips
                System.out.println(Role + GuestID + " receives bags from Bellhop " + Bellhop_ID + " and gives tip");
                Hotel.givingtips.release();
                Hotel.bellhop_done.acquire();
            }else{
                System.out.println(Role + GuestID + " enter room " + Roomnumber);
            }
            System.out.println(Role + GuestID + " retires for the evening");

            
           } catch (Exception e) {
            System.out.println("caught guest");
           }

           try {
            Hotel.completed_mutex.acquire();
            Hotel.Guest_retire.add(this);
            Hotel.A.release();
            Hotel.completed_mutex.release();
           } catch (Exception e) {
            System.out.println("caught guest going to main queue");

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


// make a variable that is that say guest is ready to join so this way main can look at it