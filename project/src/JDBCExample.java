import java.sql.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException; 

public class JDBCExample {

    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1:3306/test";

    
    
    
    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException {
        //Register JDBC driver
        Class.forName(dbClassName);
        //Database credentials
        final String USER = "root";
        // your password
        final String PASS = "123456";
        System.out.println("Connecting to database...");
   
        try {
        	
            //Establish connection
            Connection conn = DriverManager.getConnection(CONNECTION,USER,PASS);
            System.out.println("Successfully connected to MySQL!");
 
            File setup = new File("src/SETUP.sql");
            //System.out.println(setup.exists());

            //Execute a query
            System.out.println("Preparing a statement...");
            Statement stmt = conn.createStatement();
           
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
              String data = set.next();
              //System.out.println(data.concat(";"));
              stmt.execute(data.concat(";"));
              //System.out.println(rs.toString());
              //rs.close();
              //System.out.println(set.hasNext());
            }
            set.close();
            Scanner sc = new Scanner(System.in);
        	String sql = null;
            while(sc.hasNext()) {
            	String input = sc.next();
        		if(input.equals("a")) {
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
        		}
        		else if (input.equals("showusers")){ /*Show User Table*/
        			sql = "SELECT * FROM User;";
        			ResultSet rs = stmt.executeQuery(sql);
		            //STEP 5: Extract data from result set
		            while(rs.next()){
		                //Retrieve by column name
		                int sid  = rs.getInt("SIN");
		                String name = rs.getString("name");
		                String occupation = rs.getString("occupation");
		                int birth = rs.getInt("birth");
		
		                //Display values
		                System.out.print("SIN: " + sid);
		                System.out.print(", Name: " + name);
		                System.out.print(", Occupation: " + occupation);
		                System.out.println(", birth: " + birth);
		            }
		            rs.close();
        		} /* Input pattern match
        		else if (input.matches()  ){ 
        			sql = "SELECT * FROM User;";
        			ResultSet rs = stmt.executeQuery(sql);
		            //STEP 5: Extract data from result set
		            while(rs.next()){
		                //Retrieve by column name
		                int sid  = rs.getInt("SIN");
		                String name = rs.getString("name");
		                String occupation = rs.getString("occupation");
		                int birth = rs.getInt("birth");
		
		                //Display values
		                System.out.print("SIN: " + sid);
		                System.out.print(", Name: " + name);
		                System.out.print(", Occupation: " + occupation);
		                System.out.println(", birth: " + birth);
		            }
		            rs.close();
        		}*/
        		else {
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
