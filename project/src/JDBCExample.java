import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException; 

public class JDBCExample {

	// database information
    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1:3306/test";
	private static final String USER = "root";
	private static final String PASS = "123456";
	private static Statement stmt;
	private static Connection conn;
//	private static String sql = null;
	private static int state = 0;

	public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, SQLException {
        //Register JDBC driver
        Class.forName(dbClassName);
        //Database credentials
//        final String USER = "root";
        // your password
//        final String PASS = "123456";
        System.out.println("Connecting to database...");
		conn = DriverManager.getConnection(CONNECTION,USER,PASS);
		stmt = conn.createStatement();
   
//        Connection conn = null;
        
        try {
            //Establish connection
//            conn = DriverManager.getConnection(CONNECTION,USER,PASS);
            System.out.println("Successfully connected to MySQL!");
            File setup = new File("src/SETUP.sql");
            //System.out.println(setup.exists());
            //Execute a query
            System.out.println("Preparing a statement...");

            //Load .sql setup
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
              String data = set.next();
              stmt.execute(data.concat(";"));
            }
            set.close();
/*
			if(is_login){
            	System.out.println("Already Login, start your selection.");
			}else{
            	System.out.println("Please Login or register.");
            	System.out.println("Press 1 to Login, 2 to Register.");
				
			}*/
            
            //Scan Input
            Scanner sc = new Scanner(System.in);
            sc.useDelimiter("\n");

            while(sc.hasNext()) {
            	String input = sc.next();
        		// if(input.equals("a")) {
        		// 	sql = "SELECT * FROM Sailors;";
        		// 	ResultSet rs = stmt.executeQuery(sql);
		        //     //STEP 5: Extract data from result set
		        //     while(rs.next()){
		        //         //Retrieve by column name
		        //         int sid  = rs.getInt("sid");
		        //         String sname = rs.getString("sname");
		        //         int rating = rs.getInt("rating");
		        //         int age = rs.getInt("age");
		
		        //         //Display values
		        //         System.out.print("ID: " + sid);
		        //         System.out.print(", Name: " + sname);
		        //         System.out.print(", Rating: " + rating);
		        //         System.out.println(", Age: " + age);
		        //     }
		        //     rs.close();
        		// }
        		// else 
				if (input.equals("show")){ /*Show User Table*/
					show_database();
        			// sql = "SELECT * FROM User;";
        			// ResultSet rs = stmt.executeQuery(sql);
		            // //STEP 5: Extract data from result set
		            // while(rs.next()){
		            //     //Retrieve by column name
		            //     int sid  = rs.getInt("SIN");
		            //     String name = rs.getString("name");
		            //     String occupation = rs.getString("occupation");
		            //     int birth = rs.getInt("birth");
		            //     String username = rs.getString("username");
		            //     //Display values
		            //     System.out.print("SIN: " + sid);
		            //     System.out.print(", Name: " + name);
		            //     System.out.print(", Occupation: " + occupation);
		            //     System.out.print(", birth: " + birth);
					// 	System.out.println(", Username: " + username);
		            // }
		            // rs.close();
        		} /* Create User*/
        		else if (input.matches("Name:(.*), Password:(.*), Occupation:(.*), SIN:(.*), Birth:(.*), UserName:(.*)")){
					register(input);

					//
        			// String reg = "Name:(?<name>.*), Password:(?<password>.*), Occupation:(?<occupation>.*), SIN:(?<sin>.*), Birth:(?<birth>.*), UserName:(?<username>.*)";
        			// Pattern pattern = Pattern.compile(reg);
        			// Matcher matcher = pattern.matcher(input);
        			// if(matcher.find()) {
        			//    String name = matcher.group("name").toString();
        			//    String password = matcher.group("password").toString();
        			//    String occupation = matcher.group("occupation").toString();
        			//    String sin = matcher.group("sin").toString();
        			//    String birth = matcher.group("birth").toString();
					//    String username = matcher.group("username").toString();
        			//    sql = String.format("INSERT INTO User (SIN, name, password, birth, occupation, username) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",sin,name,password,birth,occupation, username);
        			//    System.out.print(sql.concat("\n"));
        			//    //stmt.execute(sql);
        			//    //stmt.execute(sql);
        			//    try {
        			//    		stmt.executeUpdate(sql);
        			//    }catch (SQLException e) {
        			// 	   // do nothing
        			//    }
        			   
        			// }else {
        			//    System.out.print("sad");
        			// }
        			
        		} /*Delete user*/
				else if (input.matches("UserName:(.*), Password:(.*), Delete!")){
//					delete_user(input);
        			// String reg = "UserName:(?<name>.*), Password:(?<password>.*), Delete";
        			// Pattern pattern = Pattern.compile(reg);
        			// Matcher matcher = pattern.matcher(input);
        			// if(matcher.find()) {
        			//    String name = matcher.group("name").toString();
        			//    String password = matcher.group("password").toString();
					   
        			//    //sql = String.format("DELETE FROM User where (SIN, name, password, birth, occupation) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",sin,name,password,birth,occupation);
        			//    System.out.print(sql.concat("\n"));
        			//    //stmt.execute(sql);
        			//    //stmt.execute(sql);
        			//    try {
        			//    		stmt.executeUpdate(sql);
        			//    }catch (SQLException e) {
        			// 	   // do nothing
        			//    }
        			// }else {
        			//    System.out.print("sad");
        			// }
        		}
        		else {
        			System.out.println("Invalid Input. Try Again: ");
        		}
	            
            }
           
        } catch (SQLException e) {
            System.err.println("Connection error occured!");
            conn.rollback();
        }
		// finally {
			System.out.println("Closing connection...");
            stmt.close();
            conn.close();
            System.out.println("Success!");
        //}
    }

	// register function, register a user to the database
	public static void register(String input) throws SQLException{
		try{
			String reg = "Name:(?<name>.*), Password:(?<password>.*), Occupation:(?<occupation>.*), SIN:(?<sin>.*), Birth:(?<birth>.*), UserName:(?<username>.*)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			if(matcher.find()) {
				String name = matcher.group("name").toString();
				String password = matcher.group("password").toString();
				String occupation = matcher.group("occupation").toString();
				String sin = matcher.group("sin").toString();
				String birth = matcher.group("birth").toString();
				String username = matcher.group("username").toString();
				String sql = String.format("INSERT INTO User (SIN, name, password, birth, occupation, username) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",sin,name,password,birth,occupation, username);
				System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
			}else {
				System.out.print("sad");
			}
		}catch(SQLException e){
			System.err.println("Something went wrong with register");
		}
	}

	// login function, check if the user exists in the database and if the password matches
	public static void login(String string) throws SQLException{
//		try{
//
//		}catch(SQLException e){
//			System.err.println("Something went wrong with login");
//		}
	}

	// delete function, after login, a use can choose to delete them self from database
//	public static void delete_user(String input) throws SQLException{
//		try{
//			String reg = "UserName:(?<name>.*), Password:(?<password>.*), Delete";
//			Pattern pattern = Pattern.compile(reg);
//			Matcher matcher = pattern.matcher(input);
//			if(matcher.find()) {
//				String name = matcher.group("name").toString();
//				String password = matcher.group("password").toString();
//				String sql ;
//
////				String sql = String.format("DELETE FROM User where (SIN, name, password, birth, occupation) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",sin,name,password,birth,occupation);
////				System.out.print(sql.concat("\n"));
//				//stmt.execute(sql);
//				//stmt.execute(sql);
////					stmt.executeUpdate(sql);
//
//			}else {
//				System.out.print("sad");
//			}
//
//		}catch(SQLException e){
//			System.err.println("Something went wrong with delete user");
//		}
//	}
	

	// helper function to show all users in the database
	public static void show_database() throws SQLException{
		try{
			String sql = "SELECT * FROM User;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				int sid  = rs.getInt("SIN");
				String name = rs.getString("name");
				String occupation = rs.getString("occupation");
				int birth = rs.getInt("birth");
				String username = rs.getString("username");
				//Display values
				System.out.print("SIN: " + sid);
				System.out.print(", Name: " + name);
				System.out.print(", Occupation: " + occupation);
				System.out.print(", birth: " + birth);
				System.out.println(", Username: " + username);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	
}
