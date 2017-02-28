import java.util.*;
import java.sql.*;

public class Flight {
    private int flno;
    private String origin;
    private String destination;
    private int distance;
    private Timestamp departs;
    private Timestamp arrives;
    private double price;
    private int is_processed;

    public Flight(int flno, String origin, String destination, int distance, Timestamp departs, Timestamp arrives, double price,  int is_processed) {
        this.flno = flno;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.departs = departs;
        this.arrives = arrives;
        this.price = price;
        this.is_processed = is_processed;
    }

    public int getDistance(){
        return this.distance;
    }

    public int getFlno(){
        return this.flno;
    }
    
    public Timestamp  getArrives(){
        return this.arrives;
    }

    public String getDestination() {
        return this.destination;
    }
}
