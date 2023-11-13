import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Restaurant{
    static int cCount = 0;
    static int customerID = 0;
    
    public static class Table{
        String tableName;
        String type;
        static ArrayList <Integer> seats;
        ArrayList <Integer> queue;
        
        Waiter waiter;
        
        public Table(String name, String type){
            tableName = name;
            this.type = type;
            seats = new ArrayList <>();
            queue = new ArrayList <>();
        }
        
        public void addWaiter(Waiter w){
            waiter = w;
        }
        
        public void addCustomer(int x){
            Restaurant.customerID = x;
        }
        
    }
    
    public static class Shared{
        static Semaphore printCustomer = new Semaphore(1);
        static Semaphore s = new Semaphore(1);
        static Semaphore q = new Semaphore(1);
        static Semaphore x = new Semaphore(1);
        static Semaphore c = new Semaphore(1);
        static Semaphore signalWaiter1 = new Semaphore(0);
        static Semaphore signalWaiter2 = new Semaphore(0);
    }
    
    public static void main(String[] args) throws InterruptedException{
        Random rand = new Random();
        
        //Create Tables x3
        Table[] tables = new Table[3];
        tables[0] = new Table("A", "Seafood");
        tables[1] = new Table("B", "Steak");
        tables[2] = new Table("C", "Pasta");
        
        //Create Waiters x3
        Waiter[] waiters = new Waiter[3];
        for(int i = 0; i < 3; i++){
            //Give each waiter a table
            waiters[i] = new Waiter(i, tables[i]);
            tables[i].addWaiter(waiters[i]);
        }
        
        for(int i = 0; i < 3; i++){
            //Start waiter threads
            waiters[i].start();
            Thread.sleep(40);
        }
        
        //Create Customers x40
        Customer[] customers = new Customer[40];
        for(int i = 0; i < 40; i++){
            Restaurant.cCount++;
            //Customers choose a table and backup table
            customers[i] = new Customer(i, tables[rand.nextInt(3)], tables[rand.nextInt(3)], rand.nextInt(2));
            //Start customer threads
            customers[i].start();
        }
    }
}
