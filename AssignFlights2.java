import java.sql.*;
public class AssignFlights2 {
    public static void main(String[] args) {
        assignFlight();
    }

    static void assignFlight() {
        Connection conn = null;
        ResultSet rset = null;
        ResultSet rset2 = null;
        ResultSet rset3 = null;
        Statement stmt;
        Statement stmt2;
        String preparedSQL;
        int cruise;
        int lowest;
        int aid = 0;
        int eid = 0;
        Flight flight;
        
        try {
            conn = DriverManager.getConnection("jdbc:oracle:thin:@dbs3.cs.umb.edu:1521:dbs3", "rwilson", "rwilson");
            //ALGORITHM STARTS HERE
            stmt = conn.createStatement();
            rset = stmt.executeQuery("select * from flights1 where is_processed= 0 order by departs");
            while(rset.next()) {
                flight = new Flight(rset.getInt(1), rset.getString(2), rset.getString(3), rset.getInt(4), rset.getTimestamp(5), rset.getTimestamp(6), rset.getDouble(7), rset.getInt(8));

                //Set the lowest to the highest cruising range
                stmt2 = conn.createStatement();
                rset2 = stmt2.executeQuery("select MAX(cruisingrange) from aircraft1");
                rset2.next();
                lowest = rset2.getInt(1);
                rset2.close();
                stmt2.close();
                //System.out.println(lowest);

                preparedSQL = "select a.aid, a.cruisingrange from aircraft1 a where ? <= a.cruisingrange";
                PreparedStatement ps = conn.prepareStatement(preparedSQL);
                ps.setString(1, Integer.toString(flight.getDistance()));
                rset3 = ps.executeQuery();
                preparedSQL = "select c.aid, c.eid from certified1 c where c.aid = ?";
                ps = conn.prepareStatement(preparedSQL);
                //cycle through every result while updating the lowest cruise
                while(rset3.next()) {
                    ResultSet rset4;
                    //PreparedStatement ps2 = conn.prepareStatement(preparedSQL);
                    ps.setString(1, Integer.toString(rset3.getInt(1)));
                    rset4 = ps.executeQuery();
                    ResultSet rset5 = rset4;
                    rset4.next();
                    ResultSet rset6 = rset4;
                    cruise = rset3.getInt(2);
                    preparedSQL = "select eid from flight_assignments where eid = ?";
                    PreparedStatement ps3 = conn.prepareStatement(preparedSQL);
                    ps3.setString(1, Integer.toString(rset4.getInt(2)));
                    rset4 = ps3.executeQuery();
                    if (rset4.next()) {//if there is one row
                        //do nothing: the pilot has been assigned
                        //System.out.println("inside" + rset3.getInt(1));
                    }
                    else if (cruise <= lowest ){//&& (rset3.getInt(1) != null) {
                        lowest = cruise;
                        aid = rset6.getInt(1);
                        eid = rset6.getInt(2);
                      }
                    rset6.close();
                    rset5.close();
                    rset4.close();
                    //ps2.close();
                    ps3.close();
                }
                //outside rset3 while
                rset3.close();
                ps.close();
                //BEGIN INSERTS AND ADDING 1'S
                preparedSQL = "update flights1 set is_processed = 1 where flno = ?";
                ResultSet rset11;
                PreparedStatement ps11 = conn.prepareStatement(preparedSQL);
                ps11.setString(1, Integer.toString(flight.getFlno()));
                rset11 = ps11.executeQuery();
                rset11.close();
                ps11.close();

                preparedSQL = "select eid from flight_assignments where eid = ?";
                PreparedStatement ps12 = conn.prepareStatement(preparedSQL);
                ps12.setString(1, Integer.toString(eid));
                ResultSet rset12;
                rset12 = ps12.executeQuery();
                if (!rset12.next() && (eid != 0) ){//if there is nothing matching in there
                    ResultSet rset14;
                    preparedSQL = "insert into flight_assignments (flno, aid, eid) values(?, ?, ?)";
                    PreparedStatement ps14 = conn.prepareStatement(preparedSQL);
                    ps14.setString(1, Integer.toString(flight.getFlno()));
                    ps14.setString(2, Integer.toString(aid));
                    ps14.setString(3, Integer.toString(eid));
                    rset14 = ps14.executeQuery();
                    rset14.close();
                    ps14.close();
                } else {
                    //insert delay
                    ResultSet rset15;
                    preparedSQL = "insert into delayed_flights (flno) values(?)";
                    PreparedStatement ps15 = conn.prepareStatement(preparedSQL);
                    ps15.setString(1, Integer.toString(flight.getFlno()));
                    rset15 = ps15.executeQuery();
                    rset15.close();
                    ps15.close();
                }
                rset12.close();
                ps12.close();
            }
            rset.close();

        } catch (SQLException e) {
            System.out.println("Problem with JDBC Connection\n");
            printSQLException(e);
            System.exit(4);
        } finally {
            // Close the connection, if it was obtained, no matter what happens
            // above or within called methods
            if (conn != null) {
                try {
                    conn.close(); // this also closes the Statement and
                    // ResultSet, if any
                    //System.out.println("You're disconnected!");
                } catch (SQLException e) {
                    System.out
                        .println("Problem with closing JDBC Connection\n");
                    printSQLException(e);
                    System.exit(5);
                }
            }
        }
    }

    //TAKEN FROM JDBCCHECKUP
    // print out all exceptions connected to e by nextException or getCause
    static void printSQLException(SQLException e) {
        // SQLExceptions can be delivered in lists (e.getNextException)
        // Each such exception can have a cause (e.getCause, from Throwable)
        while (e != null) {
            System.out.println("SQLException Message:" + e.getMessage());
            Throwable t = e.getCause();
            while (t != null) {
                System.out.println("SQLException Cause:" + t);
                t = t.getCause();
            }
            e = e.getNextException();
        }
    }

}

