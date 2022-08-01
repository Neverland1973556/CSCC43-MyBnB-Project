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

    // your username and password of mysql
    private static final String USER = "root";
    private static final String PASS = "Xzt1973556";

    // connection
    private static Statement stmt;
    private static Connection conn;

    // state of the while loop
    private static int is_login = 0;
    private static int is_owner = -1;

    // user data
    private static String username;

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, SQLException {
        Class.forName(dbClassName);
        // connection
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(CONNECTION, USER, PASS);
        stmt = conn.createStatement();
        try {
            // initialize the database
            System.out.println("Successfully connected to MySQL!");
            File setup = new File("src/SETUP.sql");
            assert (setup.exists());
            System.out.println("Preparing a statement...");
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
                String data = set.next();
                stmt.execute(data.concat(";"));
            }
            set.close();
            create_calendar();


            // scanner initialization
            Scanner sc = new Scanner(System.in);
            sc.useDelimiter("\n");

            // the while loop start
            label_whole:
            while (true) {
                // debug use
                System.out.println("is_login " + is_login + " is_owner " + is_owner);
                if (is_login == 0) {
                    // didn't login
                    System.out.println("Please Login or register.");
                    System.out.println("Press 1 to Login, 2 to Register.");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        // check if the input is empty
                        if (input.equals("")) {
                            System.out.println("LOGIN empty");
                        } else if (input.equals("1")) {
                            // need to log in
                            System.out.println("LOGIN - Please input your username.");
                            String username = sc.nextLine();
                            System.out.println("LOGIN - Please input your password.");
                            String password = sc.nextLine();
                            // debug use
                            System.out.println("Your input:" + username + " " + password);
                            String reg = "UserName:" + username + ", Password:" + password + ", Login!";
//                            boolean success = true;
                            boolean success = login(reg);
                            if (!success) {
                                // cannot login
                                System.out.println("Your login is not success, Please try again.");
                                System.out.println("Press 1 to Login, 2 to Register.");
                                // continue the loop
                            } else {
                                // success, can continue
                                JDBCExample.username = username;
                                is_login = 1;
                                break;
                            }
                        } else if (input.equals("2")) {
                            // need to register
                            System.out.println("REGISTER - Please input your username.");
                            String username = sc.nextLine();
                            System.out.println("REGISTER - Please input your password.");
                            String password = sc.nextLine();
                            System.out.println("REGISTER - Please input your real name.");
                            String real_name = sc.nextLine();
                            int birth_year_int;
                            String birth_year;
                            while (true) {
                                System.out.println("REGISTER - Please input your birth year.");
                                birth_year = sc.nextLine();
                                birth_year_int = Integer.parseInt(birth_year);
                                if (birth_year_int < 1822 || birth_year_int > 2022) {
                                    System.out.println("REGISTER - birth year is not valid.");
                                } else {
                                    break;
                                }
                            }
                            System.out.println("REGISTER - Please input your occupation.");
                            String occupation = sc.nextLine();
                            System.out.println("REGISTER - Please input your SIN.");
                            String sin = sc.nextLine();
                            String reg = "Name:" + real_name + ", Password:" + password + ", Occupation:" + occupation + ", SIN:" + sin + ", Birth:" + birth_year + ", UserName:" + username;
                            boolean success = register(reg);
                            if (!success) {
                                // cannot login
                                System.out.println("Your register is not valid, Please try again.");
                                System.out.println("Press 1 to Login, 2 to Register.");
                                // continue the loop
                            } else {
                                // success, can continue
                                JDBCExample.username = username;
                                is_login = 1;
                                break;
                            }
                        } else if (input.equals("break")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        } else {
                            // not valid
                            System.out.println("Please use valid input!");
                            System.out.println("Press 1 to Login, 2 to Register.");
                        }
                    }
                }

                // after logged in, username is set, check if the login is owner;

                if (is_owner == -1) {
                    System.out.println("Continue with Owner Account or Renter Account?");
                    System.out.println("Press 1: owner, 2: renter, 9: logout");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            // is owner account
                            is_owner = 1;
                            break;
                        } else if (input.equals("2")) {
                            is_owner = 0;
                            break;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            System.out.println("Logged out successfully");
                            continue label_whole;
                        } else {
                            // not valid
                            System.out.println("Please use valid input!");
                        }
                    }
                }

                if (is_owner == 1) {
                    // is owner account
                    System.out.println("Welcome to MyBnB!, Owner " + username + "!");
                    System.out.println("Press the Corresponding number to continue");
                    System.out.println("1: Update Information, 2: Manage My Listings, 3: Check Booking, 4: check booking history, 5: rating, 9: logout");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            System.out.println("Update Information");
                            // 1: check profile, 2: change profile
                            break;
                        } else if (input.equals("2")) {
                            System.out.println("Manage My Listings");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Check My Listings, 2: Add a Listing");
                            String listing_decide = sc.nextLine();
                            if (listing_decide.equals("1")) {
                                // get all my listings
                                // get_listings(username);
                            } else if (listing_decide.equals("2")) {
                                // add a listing to database
                                System.out.println("ADD A LISTING - Please input your listing latitude.");
                                String latitude = sc.nextLine();
                                System.out.println("ADD A LISTING - Please input your listing longitude.");
                                String longitude = sc.nextLine();
                                String listing_type;
                                int listing_type_int = 0;
                                while (true) {
                                    System.out.println("ADD A LISTING - Please input your listing type");
                                    System.out.println("              - 1: full house, 2: apartment, 3: room");
                                    listing_type = sc.nextLine();
                                    listing_type_int = Integer.parseInt(listing_type);
                                    if (listing_type_int == 1 || listing_type_int == 2 || listing_type_int == 3) {
                                        break;
                                    } else {
                                        System.out.println("ADD A LISTING - Please input valid listing type.");
                                    }
                                }
                                // all success
                                // add_listing(latitude, longitude, listing_type_int);
                            } else {
                                // not valid
                                System.out.println("Not Valid input in Managing my List!");
                                System.out.println("Press the Corresponding number to continue");
                                System.out.println("1: Update Information, 2: Manage My Listings, 3: Check Booking, 4: check booking history, 5: rating, 9: logout");

                            }
                            break;
                        } else if (input.equals("3")) {
                            System.out.println("Check Booking");
                            break;
                        } else if (input.equals("break")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            System.out.println("Try to logout");
                            continue label_whole;
                        } else {
                            // not valid
                            System.out.println("Not Valid input!");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Update Information, 2: Manage My Listings, 3: Check Booking, 4: check booking history, 5: rating, 9: logout");

                        }
                    }
                } else {
                    // is_owner == 0
                    // is renter account
                    System.out.println("Welcome to MyBnB!, Renter " + username + "!");
                    System.out.println("Press the Corresponding number to continue");
                    System.out.println("1: Update Information, 2: Book a listing, 3: cancel a booking, 4: check booking history, 5: rating, 9: logout");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            System.out.println("Update Information");
                            // 1: check profile, 2: change profile
                            break;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            System.out.println("Try to logout");
                            continue label_whole;
                        } else {
                            // not valid
                            System.out.println("Not Valid input!");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Update Information, 2: Book a listing, 3: cancel a booking, 4: check booking history, 5: rating, 9: logout");
                        }
                    }
                }

            }
        } catch (SQLException e) {
            System.err.println("Connection error occurred!");
            conn.rollback();
        }
        System.out.println("Closing connection...");
        stmt.close();
        conn.close();
        System.out.println("Closing success!");
    }

    public static boolean create_calendar() throws SQLException {
        try {
            String month = "1";
            String day = "1";

            for (int j = 1; j < 13; j++) {
                month = String.format("%d", j);
                for (int i = 1; i < 32; i++) {
                    if (!(i == 23 && j == 3)) { //Avoid deleting the available related
                        day = String.format("%d", i);
                        String sql = String.format("INSERT INTO Calendar (month, day) VALUES (\"%s\", \"%s\");", month, day);
                        //System.out.print(sql.concat("\n"));
                        stmt.executeUpdate(sql);
                    }
                }
            }
            String sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "2", "31");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "2", "30");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "2", "29");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "4", "31");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "6", "31");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "9", "31");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "11", "31");
            //System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            System.out.print("Calendar created!");
            return true;

        } catch (SQLException e) {
            System.err.println("Something went wrong with calendar");
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

}
