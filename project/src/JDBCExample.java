import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException; 

public class JDBCExample {

    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1:3306/test";

    
    
    
    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, SQLException {
        //Register JDBC driver
        Class.forName(dbClassName);
        //Database credentials
        final String USER = "root";
        // your password
        final String PASS = "123456";
        System.out.println("Connecting to database...");
   
        Connection conn = null;
        Statement stmt = null;
        
        try {
        	
            //Establish connection
            conn = DriverManager.getConnection(CONNECTION,USER,PASS);
            
            System.out.println("Successfully connected to MySQL!");
 
            File setup = new File("src/SETUP.sql");
            //System.out.println(setup.exists());

            //Execute a query
            System.out.println("Preparing a statement...");
            stmt = conn.createStatement();
           
            //Load .sql setup
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
              String data = set.next();
              stmt.execute(data.concat(";"));
            }
            set.close();
            
            //Scan Input
            Scanner sc = new Scanner(System.in);
            sc.useDelimiter("\n");
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
        		else if (input.equals("show users")){ /*Show User Table*/
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
        		} /* Input pattern match*/
        		else if (input.matches("UserName:(.*), Password:(.*), Occupation:(.*), SIN:(.*), Birth:(.*)")){
        			String reg = "UserName:(?<name>.*), Password:(?<password>.*), Occupation:(?<occupation>.*), SIN:(?<sin>.*), Birth:(?<birth>.*)";
        			Pattern pattern = Pattern.compile(reg);
        			Matcher matcher = pattern.matcher(input);
        			if(matcher.find()) {
        			   String name = matcher.group("name").toString();
        			   String password = matcher.group("password").toString();
        			   String occupation = matcher.group("occupation").toString();
        			   String sin = matcher.group("sin").toString();
        			   String birth = matcher.group("birth").toString();
        			   sql = String.format("INSERT INTO User (SIN, name, password, birth, occupation) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",sin,name,password,birth,occupation);
        			   System.out.print(sql.concat("\n"));
        			   //stmt.execute(sql);
        			   //stmt.execute(sql);
        			   stmt.executeUpdate(sql);
        			   
        			}else {
        			   System.out.print("sad");
        			}
        			
        		}
        		else {
        			System.out.println("Invalid Input. Try Again: ");
        		}
	            
            }
           
        } catch (SQLException e) {
            System.err.println("Connection error occured!");
            conn.rollback();
        }finally {
        	 System.out.println("Closing connection...");
            stmt.close();
            conn.close();
            System.out.println("Success!");
        }
    }

}
