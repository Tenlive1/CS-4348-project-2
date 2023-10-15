//acquire is wait
//release is signal


import java.util.concurrent.Semaphore;
import java.util.Random;

public class Hotel
{
 
    private static Semaphore front_available = new Semaphore( 2, true ); // semaphore
    private static Semaphore bellhop_available = new Semaphore( 2, true );
    private static Semaphore register = new Semaphore( 0, true );
    private static Semaphore assign_room = new Semaphore( 0, true );
    private static Semaphore assistance = new Semaphore( 0, true );
    private static Semaphore enter = new Semaphore( 0, true );
    private static Semaphore gives = new Semaphore( 0, true );
    private static Semaphore tips = new Semaphore( 0, true );
    private static Semaphore test = new Semaphore( 0);

    // void Guest(int num){
    //     Random random = new Random();
    //     this.num = num;
    //     this.role = "Guest";
    //     int bag = random.nextInt(5)+1;
    // }
    // void front_desk(int num){
    //     this.num = num;
    //     this.role = "Front desk employee";
    // }
    
    public static void main(String args[]){

        final int GuestThreads = 25;
        final int HotelStaffThreads = 10;

        Hotel gt[] = new Hotel[GuestThreads];
        Thread guestThread[] = new Thread[GuestThreads];

        Hotel front[] = new Hotel[HotelStaffThreads];
        Thread frontThread[] = new Thread[HotelStaffThreads];

        Bellhop bellhop[] = new Bellhop[HotelStaffThreads];
        Thread bellThread[] = new Thread[HotelStaffThreads];

        for (int i = 0; i<HotelStaffThreads; i++){
            bellhop[i] = new Bellhop(i);
            bellThread[i] = new Thread(bellhop[i]);
            bellThread[i].start();
        }

        for(int i =0; i < HotelStaffThreads; i++){
            System.out.println("bla");
            test.release();
        }

        


        for(int i =0; i < HotelStaffThreads; i++){
            try
            {
                bellThread[i].join();
            }
            catch (InterruptedException e)
            {
            }
        }

    }




    static class Bellhop implements Runnable {
        private int num;
        private String role;
        
        Bellhop(int num){
            this.num = num;
            role = "Bellhop";
        };

        public void run() {
            System.out.println(role + " " + num + " created");
            try {
                test.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println( "Thread " + num + " resuming" );
        }
    }


}
