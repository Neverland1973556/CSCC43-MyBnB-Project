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
	private static int bookid = 20;
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
            
            create_calendar();
            while(sc.hasNext()) {
            	String input = sc.next();
        	
				if (input.equals("show users")){ /*Show User Table*/
					show_users();
        		}
				else if (input.equals("show renters")){ /*Show User Table*/
					show_renters();
        		}
        		else if (input.equals("show hosts")){ /*Show User Table*/
					show_hosts();
        		} 
        		else if (input.equals("show listings")){ /*Show User Table*/
					show_listings();
        		}
        		else if (input.equals("show books")){ /*Show User Table*/
					show_books();
        		}
        		else if (input.equals("show book")){ /*Show User Table*/
					show_book();
        		}
        		else if (input.equals("show availables")){ /*Show User Table*/
					show_availables();
        		}
        		else if (input.equals("show owns")){ /*Show User Table*/
					show_owns();
        		}
        		else if (input.matches("Name:(.*), Password:(.*), Occupation:(.*), SIN:(.*), Birth:(.*), UserName:(.*)")){
					register(input);
        			
        		} /*Delete user*/
				else if (input.matches("UserName:(.*), Password:(.*), Delete!")){
					delete_user(input);
        		}
				else if (input.matches("UserName:(.*), Password:(.*), Login!")){
					login(input);
        		}
				else if(input.matches("UserName:(?<username>.*), Lon:(?<lon>.*), Lat:(?<lat>.*), Type:(?<type>.*)")){
					create_listing(input);
				}
				else if(input.matches("Price:(?<price>.*), Month:(?<month>.*), Day:(?<day>.*), Lid:(?<lid>.*)")){
					create_available(input);
				}
				else if(input.matches("Username:(?<username>.*), Payment:(?<payment>.*), Month:(?<month>.*), Day:(?<day>.*), Lid:(?<lid>.*)")){
					create_book(input);
				}
				else if(input.matches("BID:(.*), Username:(?<username>.*)")){
					book_cancellation(input);
				}
				else if(input.matches("Username:(?<username>.*), Books!")){
					show_user_books(input);
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
	public static boolean register(String input) throws SQLException{
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
				sql = String.format("INSERT INTO Host (username) VALUES (\"%s\");",username);
				System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				sql = String.format("INSERT INTO Renter (username) VALUES (\"%s\");",username);
				System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				return true;
			}else {
				System.out.println("Invalid register.");
				return false;
			}
		}catch(SQLException e){
			System.err.println("Something went wrong with register");
			return false;
		}
	}

	public static boolean create_calendar() throws SQLException{
		try{
				String month = "1";
				String day = "1";
				
				for(int j = 1; j < 13; j++) {
					month = String.format("%d", j);
				for(int i = 1; i < 32; i++) {
					if( !(i == 23 && j == 3)) { //Avoid deleting the available related
					day = String.format("%d", i);
					String sql = String.format("INSERT INTO Calendar (month, day) VALUES (\"%s\", \"%s\");" ,month, day);
					//System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					}
				}
				}
				String sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"2", "31");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"2", "30");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
			    sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"2", "29");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"4", "31");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"6", "31");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"9", "31");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";" ,"11", "31");
				//System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				System.out.print("Calendar created!");
				return true;

		}catch(SQLException e){
			System.err.println("Something went wrong with calendar");
			return false;
		}
	}
	
	public static boolean create_listing(String input) throws SQLException{
		try{
			String reg = "UserName:(?<username>.*), Lon:(?<lon>.*), Lat:(?<lat>.*), Type:(?<type>.*)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			if(matcher.find()) {
				String lat = matcher.group("lat").toString();
				String lon = matcher.group("lon").toString();
				String type = matcher.group("type").toString();
				String username = matcher.group("username").toString();
				String sql = String.format("INSERT INTO Listing (lon, lat, type) VALUES ( \"%s\", \"%s\", \"%s\");",lon,lat,type);
				System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				
				sql = String.format("SELECT * FROM Listing where lon = \"%s\" and lat = \"%s\";", lon, lat);
				//System.out.print(sql.concat("\n"));
				//stmt.executeUpdate(sql);
				ResultSet rs = stmt.executeQuery(sql);
				if(rs.next()) {
					String lid = rs.getString("lid").toString();
					sql = String.format("INSERT INTO Owns (username, lid) VALUES (\"%s\", \"%s\");",username, lid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					return true;
				}
				return false;
			}else {
				System.out.println("Invalid Listing.");
				return false;
			}
		}catch(SQLException e){
			System.err.println("Something went wrong with Listing");
			return false;
		}
	}
	
	public static boolean create_available(String input) throws SQLException{
		try{
			String reg = "Price:(?<price>.*), Month:(?<month>.*), Day:(?<day>.*), Lid:(?<lid>.*)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			if(matcher.find()) {
				String price = matcher.group("price").toString();
				String month = matcher.group("month").toString();
				String day = matcher.group("day").toString();
				String lid = matcher.group("lid").toString();
				String sql = String.format("INSERT INTO Available (price, month, day, lid) VALUES ( \"%s\", \"%s\", \"%s\", \"%s\");",price, month, day,lid);
				System.out.print(sql.concat("\n"));
				stmt.executeUpdate(sql);
				return true;
			}else {
				System.out.println("Invalid Listing.");
				return false;
			}
		}catch(SQLException e){
			System.err.println("Something went wrong with creating Available");
			return false;
		}
	}
	
	public static boolean create_book(String input) throws SQLException{
		try{
			String reg = "Username:(?<username>.*), Payment:(?<payment>.*), Month:(?<month>.*), Day:(?<day>.*), Lid:(?<lid>.*)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			if(matcher.find()) {
				String username = matcher.group("username").toString();
				String payment = matcher.group("payment").toString();
				String month = matcher.group("month").toString();
				String day = matcher.group("day").toString();
				String lid = matcher.group("lid").toString();
				String sql = String.format("SELECT * FROM Available where month = \"%s\" and day = \"%s\" and lid = \"%s\";", month, day, lid);
				//System.out.print(sql.concat("\n"));
				//stmt.executeUpdate(sql);
				ResultSet rs = stmt.executeQuery(sql);
				if(rs.next()) {
					String price = rs.getString("price");
					sql = String.format("DELETE FROM Available where month = \"%s\" and day = \"%s\" and lid = \"%s\";", month, day, lid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					
					bookid++;
					sql = String.format("INSERT INTO Book (payment, BID) VALUES (\"%s\", \"%s\");",payment, bookid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					
					sql = String.format("INSERT INTO Available (price, month, day, lid, BID) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");",price, month, day, lid, bookid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					
					sql = String.format("INSERT INTO Books (BID, username) VALUES (\"%s\", \"%s\");", bookid, username);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					return true;
				}
				System.out.println("Invalid Book.");
				return false;
			}else {
				System.out.println("Invalid Listing.");
				return false;
			}
		}catch(SQLException e){
			System.err.println("Something went wrong with creating Available");
			return false;
		}
	}

	public static boolean book_cancellation(String input) throws SQLException{
		try{
			String reg = "BID:(?<bid>.*), Username:(?<username>.*)";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			if(matcher.find()) {
				String username = matcher.group("username").toString();
				String bid = matcher.group("bid").toString();
				String sql = String.format("SELECT * FROM Books where username = \"%s\" and BID = \"%s\";", username, bid);
				ResultSet rs = stmt.executeQuery(sql);
				if(rs.next()) {
					
					
					sql = String.format("SELECT * FROM Available where BID = \"%s\";", bid);
					System.out.print(sql.concat("\n"));
					rs = stmt.executeQuery(sql);
					rs.next();
					String price = rs.getString("price");
					String month = rs.getString("month");
					String day = rs.getString("day");
					String lid = rs.getString("lid");
					
					sql = String.format("DELETE FROM Available where BID = \"%s\";", bid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					
					sql = String.format("INSERT INTO Available (price, month, day, lid) VALUES (\"%s\", \"%s\", \"%s\", \"%s\");",price, month, day, lid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					
					sql = String.format("SELECT * FROM Book where BID = \"%s\";", bid);
					System.out.print(sql.concat("\n"));
					rs = stmt.executeQuery(sql);
					rs.next();
					String payment = rs.getString("payment");
					String credit_card = rs.getString("credit_card");
					String cancellation = "1";
					
					sql = String.format("DELETE FROM Book where BID = \"%s\";", bid);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					
					if(credit_card != null) {
						sql = String.format("INSERT INTO Book (payment, BID, credit_card, cancellation) VALUES (\"%s\", \"%s\", \"%s\", \"%s\");",payment, bid, credit_card, cancellation);
						System.out.print(sql.concat("\n"));
						stmt.executeUpdate(sql);
					}else {
						sql = String.format("INSERT INTO Book (payment, BID, cancellation) VALUES (\"%s\", \"%s\", \"%s\");",payment, bid, cancellation);
						System.out.print(sql.concat("\n"));
						stmt.executeUpdate(sql);
					}
					
					return true;
				}
				System.out.println("Invalid Book.");
				return false;
			}else {
				System.out.println("Invalid Listing.");
				return false;
			}
		}catch(SQLException e){
			System.err.println("Something went wrong with creating Available");
			return false;
		}
	}
	// login function, check if the user exists in the database and if the password matches
	public static boolean login(String string) throws SQLException{
		try{
			String reg = "UserName:(?<username>.*), Password:(?<password>.*), Login!";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(string);
			if(matcher.find()) {
				String username = matcher.group("username").toString();
				String password = matcher.group("password").toString();
				String sql = String.format("SELECT * FROM User where username = \"%s\" and password = \"%s\";", username, password);
				ResultSet rs = stmt.executeQuery(sql);
				if(rs.next()) {
					String UserName = rs.getString("username");
					System.out.println("User: " + UserName + " Login succeed!");
					return true;
				}else {
					System.out.println("Login failed");
					return false;
				}
				/**/
				
			}else {
				System.out.print("sad");
				return false;
			}

		}catch(SQLException e){
			System.err.println("Something went wrong with user login");
			return false;
		}
	}

	 //delete function, after login, a use can choose to delete them self from database
	public static boolean delete_user(String input) throws SQLException{
		try{
			String reg = "UserName:(?<username>.*), Password:(?<password>.*), Delete!";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			if(matcher.find()) {
				String username = matcher.group("username").toString();
				String password = matcher.group("password").toString();
				String sql = String.format("SELECT * FROM User where username = \"%s\" and password = \"%s\";", username, password);
				//System.out.print(sql.concat("\n"));
				//stmt.executeUpdate(sql);
				ResultSet rs = stmt.executeQuery(sql);
				if(rs.next()) {
					String UserName = rs.getString("username");
					System.out.println("User: " + UserName + " deleting");
					sql = String.format("DELETE FROM User where username = \"%s\";", username);
					System.out.print(sql.concat("\n"));
					stmt.executeUpdate(sql);
					System.out.println("User: Jonathan deleted!");
					return true;
				}else {
					System.out.println("User Invalid");
					return false;
				}
				/**/
				
			}else {
				System.out.print("sad");
				return false;
			}

		}catch(SQLException e){
			System.err.println("Something went wrong with delete user");
			return false;
		}
	}
	

	// helper function to show all users in the database
	public static void show_users() throws SQLException{
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
	
	// helper function to show all users in the database
	public static void show_renters() throws SQLException{
		try{
			String sql = "SELECT * FROM Renter;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				String username = rs.getString("username");
				System.out.println("Username: " + username);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	// helper function to show all users in the database
	public static void show_hosts() throws SQLException{
		try{
			String sql = "SELECT * FROM Host;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				String username = rs.getString("username");
				System.out.println("Username: " + username);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	// helper function to show all users in the database
	public static void show_listings() throws SQLException{
		try{
			String sql = "SELECT * FROM Listing;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				int lid = rs.getInt("lid");
				System.out.print("Lid: " + lid);
				double lon = rs.getDouble("lon");
				System.out.print(", Lon: " + lon);
				double lat = rs.getDouble("lat");
				System.out.print(", Lat: " + lat);
				String type = rs.getString("type");
		        System.out.println(", Type: " + type);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}

	public static void show_owns() throws SQLException{
		try{
			String sql = "SELECT * FROM Owns;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				int lid = rs.getInt("lid");
				System.out.print("Lid: " + lid);
				String username = rs.getString("username");
		        System.out.println(", HostName: " + username);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	public static void show_user_owns(String username) throws SQLException{
		try{//SELECT * FROM Owns Natural Join Listing where Owns.username = "Jonathan"
			String sql = String.format("SELECT * FROM Owns Natural Join Listing where Owns.username  = \"%s\";", username);
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				int lid = rs.getInt("lid");
				System.out.println("Lid: " + lid);
				//String username = rs.getString("username");
		        //System.out.println(", HostName: " + username);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	
	public static void show_books() throws SQLException{
		try{
			String sql = "SELECT * FROM Books;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				String username = rs.getString("username");
		        System.out.print("RenterName: " + username);
				int bid = rs.getInt("BID");
				System.out.println(", BID: " + bid);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	
	public static void show_user_books(String input) throws SQLException{
		try{

			String reg = "Username:(?<username>.*), Books!";
			Pattern pattern = Pattern.compile(reg);
			Matcher matcher = pattern.matcher(input);
			String username = "1";
			if(matcher.find()) {
				username = matcher.group("username").toString();
			}
			String sql = String.format("SELECT * FROM Books Natural Join Available Natural Join Listing where username = '%s';", username);
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				
				String bid = rs.getString("BID");
				System.out.print("BID: " + bid);
				String lid = rs.getString("lid");
		        System.out.print(", ListingID: " + lid);
		        String lon = rs.getString("lon");
		        System.out.print(", Longitude: " + lon);
		        String lat = rs.getString("lat");
		        System.out.print(", Latitude: " + lat);
		        String type = rs.getString("type");
		        System.out.print(", Type: " + type);
		        String month = rs.getString("month");
		        System.out.print(", Month: " + month);
		        String day = rs.getString("day");
		        System.out.print(", Day: " + day);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	
	public static void show_book() throws SQLException{
		try{
			String sql = "SELECT * FROM Book;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				
				int bid = rs.getInt("BID");
				System.out.print("BID: " + bid);
				float payment = rs.getFloat("payment");
		        System.out.print(", Payment: " + payment);
		        int cancellation = rs.getInt("cancellation");
		        System.out.println(", Cancelled: " + cancellation);

			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
	public static void show_availables() throws SQLException{
		try{
			String sql = "SELECT * FROM Available;";
			ResultSet rs = stmt.executeQuery(sql);
			//STEP 5: Extract data from result set
			while(rs.next()){
				//Retrieve by column name
				
				String bid = rs.getString("BID");
				System.out.print("BID: " + bid);
				float price = rs.getFloat("price");
		        System.out.print(", Price: " + price);
		        int month = rs.getInt("month");
		        System.out.print(", Month: " + month);
		        int day = rs.getInt("day");
		        System.out.print(", Day: " + day);
		        String lid = rs.getString("lid");
		        System.out.println(", Lid: " + lid);
			}
			rs.close();
		}catch(SQLException e){
			System.err.println("Something went wrong with the show database");
		}
	}
}
