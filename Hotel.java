//acquire is wait
//release is signal


import java.util.concurrent.Semaphore;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Random;

public class Hotel
{
    // semaphore
    private static Semaphore front_available = new Semaphore( 2, true ); // semaphore
    private static Semaphore bellhop_available = new Semaphore( 2, true );
    private static Semaphore register = new Semaphore( 0, true );
    private static Semaphore assign_room = new Semaphore( 0, true );
    private static Semaphore assistance = new Semaphore( 0, true );
    private static Semaphore enter = new Semaphore( 0, true );
    private static Semaphore gives = new Semaphore( 0, true );
    private static Semaphore tips = new Semaphore( 0, true );


    
    public static void main(String args[]){

        final int GuestThreads = 25;
        final int HotelStaffThreads = 2;

        Thread guestThread[] = new Thread[GuestThreads];

        Thread frontThread[] = new Thread[HotelStaffThreads];

        
        Thread bellThread[] = new Thread[HotelStaffThreads];

        
        for (int i = 0; i<HotelStaffThreads; i++){ // this will make the front-desk
            Front_desk employee = new Front_desk(i);
            frontThread[i] = new Thread(employee);
            frontThread[i].start();
        }

        for (int i = 0; i<HotelStaffThreads; i++){ // this will make the front-desk
            Bellhop staffs = new Bellhop(i);
            bellThread[i] = new Thread(staffs);
            bellThread[i].start();
        }


        for(int i = 0; i<GuestThreads; i++){
            Guest customer = new Guest(i);
            guestThread[i] = new Thread(customer);
            guestThread[i].start();
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
            
        }
    }

    static class Front_desk implements Runnable{
        private int num;
        private String role;

        Front_desk(int num){
            this.num = num;
            role = "Front desk employee";
        }
        
        public void run() {
            System.out.println(role + " " + num + " created");
            try {
                register.acquire();

            } catch (Exception e) {
                // TODO: handle exception
            }

        }
    }

    static class Guest implements Runnable{
        private int num;
        private String role;
        private int bags;

        Guest(int num){
            this.num = num;
            role = "Guest ";
            bags = (int) (Math.floor(Math.random() *5)+1);
        }

        

        public void run(){
            System.out.println(role + " " + num + " created");

            try {

                enterhotel();
                front_available.acquire();
                register.release();








                front_available.release();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        public void enterhotel(){
            if(bags>1){
                System.out.println(role + num +" " + "enters hotel with " + bags + " bags" );
            }else{
                System.out.println(role + num +" " + "enters hotel with " + bags + " bag" );
            }
        }
    }


}
