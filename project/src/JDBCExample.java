import java.sql.*;
import java.util.Scanner;

public class JDBCExample {

    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1:3306/test";

    public static void main(String[] args) throws ClassNotFoundException {
        //Register JDBC driver
        Class.forName(dbClassName);
        //Database credentials
        final String USER = "root";
        // your password
        final String PASS = "123456";
        System.out.println("Connecting to database...");

        try {
        	Scanner sc = new Scanner(System.in);
        	String sql = null;
            //Establish connection
            Connection conn = DriverManager.getConnection(CONNECTION,USER,PASS);
            System.out.println("Successfully connected to MySQL!");

            //Execute a query
            System.out.println("Preparing a statement...");
            Statement stmt = conn.createStatement();
            while(sc.hasNext()) {
        		if(sc.next().equals("a")) {
        			sql = "SELECT * FROM Sailors;";
        			ResultSet rs = stmt.executeQuery(sql);
		            //STEP 5: Extract data from result set
		            while(rs.next()){
		                //Retrieve by column name
		                int sid  = rs.getInt("sid");
		                String sname = rs.getString("sname");
		                int rating = rs.getInt("rating");
		                int age = rs.getInt("age");
		
		                //Display values
		                System.out.print("ID: " + sid);
		                System.out.print(", Name: " + sname);
		                System.out.print(", Rating: " + rating);
		                System.out.println(", Age: " + age);
		            }
		            rs.close();
        		}else {
        			System.out.println("Invalid Input. Try Again: ");
        		}
	            
            }
            System.out.println("Closing connection...");
            stmt.close();
            conn.close();
            System.out.println("Success!");
        } catch (SQLException e) {
            System.err.println("Connection error occured!");
        }
    }

}