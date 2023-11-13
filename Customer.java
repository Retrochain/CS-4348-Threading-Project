import java.util.concurrent.Semaphore;
import java.util.Random;

public class Customer extends Thread{
    Random rand = new Random();
    
    static Semaphore door;
    static Semaphore qAccessC;
    static Semaphore sAccessC;
    static Semaphore oAccess;
    static Semaphore bill;
    int customerID;
    int r;
    Restaurant.Table table;
    Restaurant.Table tableIn;
    Restaurant.Table backup;
    Restaurant.Table backupIn;
    
    //Unique Customer id
    public Customer(int cid, Restaurant.Table tableS, Restaurant.Table backupS, int rand){
        customerID = ++ cid;
        door = new Semaphore(2);
        tableIn = tableS;
        backupIn = backupS;
        r = rand;
        qAccessC = Restaurant.Shared.q;
        sAccessC = Restaurant.Shared.s;
        oAccess = Restaurant.Shared.c;
        bill = new Semaphore(1);
    }
    
    //Customer enters through one of the 2 doors and gets serviced, before leaving
    public void entry(Restaurant.Table ts, Restaurant.Table b, int randDoor) throws InterruptedException{
        door.acquire();
        System.out.println("Customer " + customerID + " entered using door " + ++ randDoor);
        //Customer chooses table
        chooseTable(ts, b);
        //Customer either enters queue or sits at the table
        addCustomer(ts, b);
        //Customer calls the waiter
        signalWaiter();
        //Customer receives the order
        order();
        //Customer eats and leaves the table
        removeCustomer();
        //Customer pays the bill
        payBill();
        //Customer exits through the same door it came in through
        System.out.println("Customer " + customerID + " has left through door " + randDoor);
        door.release();
    }
    
    public void chooseTable(Restaurant.Table tableS, Restaurant.Table backupS) throws InterruptedException{
        table = tableIn;
        System.out.println("Customer " + customerID + " wants to eat " + this.table.type);
        int coin = rand.nextInt(2);
        if(coin == 1 && backupS != tableS){
            backup = backupIn;
        }
    }
    
    public void addCustomer(Restaurant.Table table, Restaurant.Table backup) throws InterruptedException{
        //If the queue size is less than 7, add the customer to the queue
        if(table.queue.size() < 7){
            qAccessC.acquire();
            table.queue.add(customerID);
            qAccessC.release();
            System.out.println("Customer " + customerID + " is standing in line for table " + this.table.tableName);
        } else if(backup != null){ //Otherwise if the main queue is 7 or more long, and the table has a backup
            table = backup;
            if(table.queue.size() < 7){ //If the backup table has a smaller line, add customer to this queue
                qAccessC.acquire();
                table.queue.add(customerID);
                qAccessC.release();
                System.out.println("Customer " + customerID + " switched queues to table " + this.table.tableName + " instead");
            }
        }
        
        //Add customer to the seats in the table
        if(Restaurant.Table.seats.size() < 4){
            sAccessC.acquire();
            Restaurant.Table.seats.add(customerID);
            sAccessC.release();
            
            qAccessC.acquire();
            table.queue.remove((Integer) customerID);
            qAccessC.release();
            
            sAccessC.acquire();
            int pos = Restaurant.Table.seats.indexOf(customerID);
            sAccessC.release();
            
            System.out.println("Customer " + customerID + " sat on seat " + ++ pos + " at table " + table.tableName);
        }
    }
    
    public void signalWaiter() throws InterruptedException{
        oAccess.acquire();
        table.addCustomer(customerID);
        Restaurant.Shared.signalWaiter1.release();
        Restaurant.Shared.signalWaiter2.acquire();
        oAccess.release();
    }
    
    public void order() throws InterruptedException{
        System.out.println("Customer " + customerID + " has received their order");
        Random ran = new Random();
        int r = ran.nextInt(1000 - 200);
        int result = (int) r + 200;
        Thread.sleep(result);
        System.out.println("Customer " + customerID + " has eaten their order");
    }
    
    public void removeCustomer() throws InterruptedException{
        Restaurant.Table.seats.remove((Integer) customerID);
        System.out.println("Customer " + customerID + " has left the table");
    }
    
    public void payBill() throws InterruptedException{
        bill.acquire();
        System.out.println("Customer " + customerID + " is paying the bill");
        bill.release();
    }
    
    public void run(){
        try{
            Restaurant.Shared.printCustomer.acquire();
            entry(tableIn, backupIn, r);
            Restaurant.Shared.printCustomer.release();
        } catch(Exception e){
            System.err.println("Error in Thread " + customerID + ": " + e);
        }
    }
}
