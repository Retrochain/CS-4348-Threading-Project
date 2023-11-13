import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class Waiter extends Thread{
    int waiterID;
    Restaurant.Table table;
    static Semaphore qAccess;
    static Semaphore sAccess;
    
    //Unique Waiter id
    public Waiter(int wid, Restaurant.Table t){
        waiterID = ++ wid;
        table = t;
        System.out.println("Waiter " + waiterID + " has table " + this.table.tableName + " which is serving " + this.table.type);
        qAccess = Restaurant.Shared.q;
        sAccess = Restaurant.Shared.x;
    }
    
    //Waiter goes to the kitchen to enter the customer's order
    public void kitchenFirst(int c, int w) throws InterruptedException{
        Random ran = new Random();
        int r = ran.nextInt(500 - 100);
        int result = (int) r + 100;
        Thread.sleep(result);
        System.out.println("Waiter " + w + " is sending order for Customer " + c + " to the kitchen");
    }
    
    //Waiter waits until order is ready
    public void kitchenWait(int w) throws InterruptedException{
        Random ran = new Random();
        int r = ran.nextInt(1000 - 300);
        int result = (int) r + 300;
        Thread.sleep(result);
        System.out.println("Waiter " + w + " is waiting outside the kitchen");
    }
    
    //Waiter sends order to the customer
    public void kitchenLast(int c, int w) throws InterruptedException{
        Random ran = new Random();
        int r = ran.nextInt(500 - 100);
        int result = (int) r + 100;
        Thread.sleep(result);
        System.out.println("Waiter " + w + " has gotten the order for Customer " + c);
    }
    
    public void run(){
        try{
            while(true){
                Restaurant.Shared.signalWaiter1.acquire();
                int cid = Restaurant.customerID;
                System.out.println("Waiter " + waiterID + " has been requested by Customer " + cid);
                
                sAccess.acquire();
                kitchenFirst(Restaurant.customerID, waiterID);
                kitchenWait(waiterID);
                kitchenLast(Restaurant.customerID, waiterID);
                Restaurant.cCount--;
                sAccess.release();
                Restaurant.Shared.signalWaiter2.release();
                
                //If last customer, exit
                if(Restaurant.cCount == 0){
                    System.out.println("Waiter is cleaning table " + table.tableName);
                    System.out.println("Waiter has left the restaurant");
                    break;
                }
            }
        } catch(Exception e){
            System.err.println("Error in Thread " + waiterID + ": " + e);
        }
        
    }
}
