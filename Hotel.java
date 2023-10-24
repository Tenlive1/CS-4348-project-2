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
    static Semaphore finish;
    //mutex
    static Semaphore Guest_mutex;
    static Semaphore completed_mutex;
    static Semaphore front_mutex;
    static Semaphore bell_mutex;
    



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
        finish = new Semaphore(0,true);
        //initilizing the mutex
        Guest_mutex = new Semaphore(1,true);
        front_mutex = new Semaphore(1,true);
        bell_mutex = new Semaphore(1,true);
        completed_mutex = new Semaphore(1, true);
        // making the queue
        FrontDeskWait = new LinkedList<>();
        BellHopWait = new LinkedList<>();
        Guest_retire = new LinkedList<>();



    }
    
    public static void main(String args[]){

        System.out.println("simulation start");

        Hotel sim = new Hotel();// starting the thread hotel
        Front_desk front_employee[] = new Front_desk[MaxEmployee];
        Bellhop bell_employee[] = new Bellhop[MaxEmployee];
        Guest customer[] = new Guest[MaxGuest];

        int counter =0;


        for(int x=0; x<MaxEmployee; x++){
            front_employee[x] = new Front_desk(x, sim);// this is how the front desk thread will start
        }

        for(int x=0; x<MaxEmployee; x++){
            bell_employee[x] = new Bellhop(x, sim);// this is how the bellhop will start
        }
         for(int x=0; x<MaxGuest; x++){
            customer[x] = new Guest(x, sim);// this is how the guest will start
        }


        while(true){// basicall this section of the code is how the guest will join up together
            try {
                
                if(counter == 25){
                    break;
                }
                finish.acquire();
                Guest g = Hotel.Guest_retire.remove();
                g.GuestThreads.join();
                System.out.println(g.Role + g.GuestID + " joined");
                counter++;
            } catch (Exception e) {
                System.out.println("something is wrong with main");
            }
            
        }

        System.out.println("simulation exit");
        System.exit(0);
        

    }



    static class Bellhop implements Runnable {

        int EmployeeID;// bell hop id
        Hotel sim; // this is how the bellhop get access to the global semphore
        Thread BellHopThreads;// making bellhoip threads
        String Role = "BellHop ";

        Bellhop(int ID, Hotel sim){
            EmployeeID = ID;// getting bellhop id

            BellHopThreads = new Thread(this);//making a thread
            System.out.println(Role + EmployeeID + " created");
            BellHopThreads.start();// starting the thread
        }
      
        public void run() {

            try {
                while(true){
                    Hotel.Givesbagstobell.acquire();// waiting for guest to give bag to bellhop
                    Hotel.bell_mutex.acquireUninterruptibly(); // this is where the bellhop know the guest id and guest info (but one at a time)

                    Guest g = Hotel.BellHopWait.remove();// getting the guest that request it
                    g.Bellhop_ID = EmployeeID;// letting the guest know the bellhop id
                
                    Hotel.bell_mutex.release();

                    System.out.println(Role + EmployeeID + " receives bags from "+ g.Role + g.GuestID + " ");

                    Hotel.receivebags.release();// this will let guest know that it have recieve the bag
                    Hotel.EnterRoom.acquire();// waiting for guest to enter the room
                    System.out.println(Role + EmployeeID + " delivers bags to " + g.Role + g.GuestID + " ");
                    Hotel.deliver.release();// bell hop have deliver the bag
                    Hotel.givingtips.acquire();// waiting for guest to give tips
                    Hotel.bellhop_done.release();//bellhop is done
                    Hotel.bellhop_available.release();// bellhop is now available

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
            EmployeeID = ID;// getting front desk id
            FrontEmployeeThread = new Thread(this); // creating the thread

            System.out.println(Role + EmployeeID + " created");
            FrontEmployeeThread.start();//starting the thread and it will go to the public void run
        }

        public void run() {
           
            try {
                while(true){
                    Hotel.Guest_rdy.acquire();// waiting for guest to be ready
                    Hotel.front_mutex.acquire(); // this will get the guest thread and assign the room to that guest
                    Guest g = Hotel.FrontDeskWait.remove();
                    g.front_ID = EmployeeID;// letting the guest know the front id/ who is serving them
                    assignroomnumber++; // increament the room
                    g.Roomnumber = assignroomnumber; //assigning it to guest
                    
                    Hotel.front_mutex.release();
                    System.out.println(Role + EmployeeID + " registers " + g.Role + g.GuestID + " and assigns room " + g.Roomnumber + " ");

                    Hotel.front_done.release();// front is done with it's jobs

                    Hotel.GuestLeftfront.acquire();// guest have left the front
                    Hotel.front_available.release();// front is now available
                }
            } catch (Exception e) {
                
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
            GuestID = ID;// getting guest id
            bags = Ran.nextInt(6); // range is from 0-5  also this is guest bags
            GuestThreads = new Thread(this);// creating guest threads
            System.out.println(Role + GuestID + " created");
            GuestThreads.start();// starting the threads
        }

        public void run(){
           try {
            EnterHotel();
            Hotel.Guest_mutex.acquire();
            Hotel.FrontDeskWait.add(this); // adding the guest into the queue for the front
            Hotel.Guest_mutex.release();

            Hotel.front_available.acquire();// seeing if the front is available
            Hotel.Guest_rdy.release(); // guest is ready once the fron is ready
            Hotel.front_done.acquire(); // waiting on front to be done   
            System.out.println(Role + GuestID + " receives room key for room " + Roomnumber + " from front desk employee " + front_ID);
            
            Hotel.GuestLeftfront.release();// guest is done with front


            if(bags > 2){// bell hop is needed when guest have more then 2 bags else guest will go to the room
                Hotel.bellhop_available.acquire();// waiting on bellhop being available
                Hotel.BellHopWait.add(this);
                System.out.println(Role + GuestID + " request help with bags");
                Hotel.Givesbagstobell.release();// giving bag to bellhop
                Hotel.receivebags.acquire();// bell hop have reviece the bag from guest
                System.out.println(Role + GuestID + " enter room " + Roomnumber); 
                Hotel.EnterRoom.release(); // guest have enter their room 
                Hotel.deliver.acquire();// waiting on bellhop to deliver the bag
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

           try {// this is how i signal main that this thread is done
            Hotel.completed_mutex.acquire();
            Hotel.Guest_retire.add(this);
            Hotel.finish.release();
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