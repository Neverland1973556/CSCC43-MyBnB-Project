import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileNotFoundException;

public class JDBCExample {
    // database information
    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1:3306";

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

    // some strings
    private static final String is_owner_prompt = "1: Update Information, 2: Manage My Listings, 3: Check Booking, 4: check booking history, 5: rating, 9: logout";
    private static final String is_renter_prompt = "1: Update Information, 2: Manage My Booking, 5: rating, 9: logout";
    private static final String a_line = "--------------------------------------------------------------------------------";
    private static final String half_line = "------------------------------";

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
            System.out.println("Preparing start up database...");
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
                String data = set.next();
                stmt.execute(data.concat(";"));
            }
            set.close();
            System.out.println("Start up database done...");
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
                    System.out.println(a_line);
                    System.out.println("Please Login or register.");
                    System.out.println("Press 1 to Login, 2 to Register.");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            print_header("LOGIN");
                            // need to log in
                            System.out.println("LOGIN - Please input your username.");
                            String username = validate(sc);
                            System.out.println("LOGIN - Please input your password.");
                            String password = validate(sc);
                            String reg = "UserName:" + username + ", Password:" + password + ", Login!";
                            // debug use
                            System.out.println(reg);
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
                            print_header("REGISTER");
                            System.out.println("REGISTER - Please input your username.");
                            String username = validate(sc);
                            System.out.println("REGISTER - Please input your password.");
                            String password = validate(sc);
                            System.out.println("REGISTER - Please input your real name.");
                            String real_name = validate(sc);
                            System.out.println("REGISTER - Please input your occupation.");
                            String occupation = validate(sc);
                            System.out.println("REGISTER - Please input your birth year.");
                            String birth_year = validate_int(sc, 1800, 2022);
                            System.out.println("REGISTER - Please input your SIN.");
                            String sin = validate_int(sc, 0, 0);

                            String reg = "Name:" + real_name + ", Password:" + password + ", Occupation:" + occupation + ", SIN:" + sin + ", Birth:" + birth_year + ", UserName:" + username;
                            System.out.println(reg);
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
                            print_error("Please use valid input!");
                            System.out.println(a_line);
                            System.out.println("Press 1 to Login, 2 to Register.");
                        }
                    }
                }

                // after logged in, username is set, check if the login is owner;
                if (is_owner == -1) {
                    System.out.println(a_line);
                    System.out.println("Continue with Owner Account or Renter Account?");
                    System.out.println("Press 1: owner, 2: renter, 9: logout");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            print_header("OWNER");
                            // is owner account
                            is_owner = 1;
                            break;
                        } else if (input.equals("2")) {
                            print_header("RENTER");
                            is_owner = 0;
                            break;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            System.out.println("Logged out successfully");
                            continue label_whole;
                        } else if (input.equals("break")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        } else {
                            // not valid
                            print_error("Please use valid input!");
                        }
                    }
                }
                System.out.println(a_line);

                if (is_owner == 1) {
                    // is owner account
                    System.out.println("Welcome to MyBnB!, Owner \"" + username + "\"!");
                    System.out.println("Press the Corresponding number to continue");
                    System.out.println(is_owner_prompt);
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            select_profile(sc);
                        } else if (input.equals("2")) {
                            print_header("Manage My Listings");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Check My Listings, 2: Add a Listing");
                            String listing_decide = sc.nextLine();
                            if (listing_decide.equals("1")) {
                                print_header("Check My Listings");
                                // get all my listings
                                show_user_owns(username);
                            } else if (listing_decide.equals("2")) {
                                // add a listing to database
                                print_header("Add a Listing");
                                System.out.println("Add a Listing- Please input your listing latitude.");
                                String latitude = validate_double(sc, -90, 90);
                                System.out.println("Add a Listing - Please input your listing longitude.");
                                String longitude = validate_double(sc, -180, 180);
                                System.out.println("Add a Listing - Please input your listing type");
                                System.out.println("              - 1: full house, 2: apartment, 3: room");
                                String listing_type = validate_int(sc, 1, 3);
                                int listing_type_int = Integer.parseInt(listing_type);
                                if (listing_type_int == 1) {
                                    listing_type = "full house";
                                } else if (listing_type_int == 2) {
                                    listing_type = "apartment";
                                } else {
                                    listing_type = "room";
                                }
                                System.out.println("Add Listing Address - Please input your postal code.");
                                String postal_code = validate_postal_code(sc);
                                System.out.println("Add Listing Address - Please input your unit.");
                                String unit = validate_int(sc, 0, 9999);
                                System.out.println("Add Listing Address - Please input your city.");
                                String city = validate(sc);
                                System.out.println("Add Listing Address - Please input your country.");
                                String country = validate(sc);

                                String reg = "UserName:" + username + ", Lon:" + longitude + ", Lat:" + latitude + ", Type:" + listing_type;
                                System.out.println(reg);
                                boolean success = create_listing(reg, postal_code, unit, city, country);
                                if (!success) {
                                    // cannot login
                                    print_error("Cannot add, please try again");
                                    // continue the loop
                                } else {
                                    // success, can continue
                                    System.out.println("Add success!");
                                }
                            } else {
                                // not valid
                                print_error("Not Valid input in Managing my List!");
                            }
                        } else if (input.equals("3")) {
                            print_header("Manage My Booking");
                            // checking booking history
                            // check current booking
                            // check availability


                        } else if (input.equals("4")) {
                            System.out.println("Check Booking");
                        } else if (input.equals("5")) {
                            System.out.println("Rating and Comment");

                        } else if (input.equals("break")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            System.out.println("Try to logout");
                            continue label_whole;
                        } else {
                            // not valid
                            print_error("Not Valid input!");
                        }
                        end_of_owner();
                    }
                } else {
                    // is_owner == 0
                    // is renter account
                    System.out.println("Welcome to MyBnB!, Renter \"" + username + "\"!");
                    System.out.println("Press the Corresponding number to continue");
                    System.out.println(is_renter_prompt);
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            select_profile(sc);
                        } else if (input.equals("2")) {
                            print_header("Booking a list");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show my bookings, 2: Book a listing, 3: Cancel a booking.");
                            String booking_decide = sc.nextLine();
                            if (booking_decide.equals("1")) {
                                print_header("Show my bookings");
                                // get all my listings
//                                show_user_booking(username);
                            } else if (booking_decide.equals("2")) {
                                print_header("Book a listing");
                                // get_all listings that is available
                                // search?
                                // choose the one you want to book
                                // book_by_aid

                            } else if (booking_decide.equals("3")) {
                                print_header("Cancel a booking");
                                // get all my listings
//                                show_user_booking(username);
                                // choose the one you want to cancel
                                // cancel by aid
                            } else {
                                print_error("Not Valid input!");
                            }
                        } else if (input.equals("5")) {
                            print_header("Rating and Comment");
                        } else if (input.equals("break")) {
                            print_header("Terminate the program.");
                            break label_whole;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            System.out.println("Try to logout");
                            continue label_whole;
                        } else {
                            // not valid
                            print_error("Not Valid input!");
                        }
                        end_of_renter();
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


    // sql functions
    public static void create_calendar() throws SQLException {
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

        } catch (SQLException e) {
            System.err.println("Something went wrong with calendar");
        }
    }

    // login function, check if the user exists in the database and if the password matches
    public static boolean login(String string) throws SQLException {
        try {
            String reg = "UserName:(?<username>.*), Password:(?<password>.*), Login!";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find()) {
                String username = matcher.group("username").toString();
                String password = matcher.group("password").toString();
                String sql = String.format("SELECT * FROM User where username = \"%s\" and password = \"%s\";", username, password);
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    String UserName = rs.getString("username");
                    System.out.println("User: " + UserName + " Login succeed!");
                    return true;
                } else {
                    System.out.println("Login failed");
                    return false;
                }
                /**/

            } else {
                System.out.print("sad");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Something went wrong with user login");
            return false;
        }
    }

    // register function, register a user to the database
    public static boolean register(String input) throws SQLException {
        try {
            String reg = "Name:(?<name>.*), Password:(?<password>.*), Occupation:(?<occupation>.*), SIN:(?<sin>.*), Birth:(?<birth>.*), UserName:(?<username>.*)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String name = matcher.group("name").toString();
                String password = matcher.group("password").toString();
                String occupation = matcher.group("occupation").toString();
                String sin = matcher.group("sin").toString();
                String birth = matcher.group("birth").toString();
                String username = matcher.group("username").toString();
                String sql = String.format("INSERT INTO User (SIN, name, password, birth, occupation, username) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\");", sin, name, password, birth, occupation, username);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);
                sql = String.format("INSERT INTO Host (username) VALUES (\"%s\");", username);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);
                sql = String.format("INSERT INTO Renter (username) VALUES (\"%s\");", username);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);
                return true;
            } else {
                System.out.println("Invalid register.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong with register");
            return false;
        }
    }

    public static void show_user_owns(String username) throws SQLException {
        try {
            String sql = String.format("SELECT * FROM Owns where username = '%s';", username);
            ResultSet rs = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int lid = rs.getInt("lid");
                System.out.println("Lid: " + lid);
                //String username = rs.getString("username");
                //System.out.println(", HostName: " + username);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Something went wrong with the show database");
        }
    }

    public static boolean create_listing(String input, String postal_code, String unit, String city, String country) throws SQLException {
        try {
            String reg = "UserName:(?<username>.*), Lon:(?<lon>.*), Lat:(?<lat>.*), Type:(?<type>.*)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String lat = matcher.group("lat");
                String lon = matcher.group("lon");
                String type = matcher.group("type");
                String username = matcher.group("username");
                String sql = String.format("INSERT INTO Listing (lon, lat, type) VALUES ( '%s', '%s', '%s');", lon, lat, type);
                System.out.println(sql);
                stmt.executeUpdate(sql);

                sql = String.format("SELECT * FROM Listing where lon = '%s' and lat = '%s';", lon, lat);
                //System.out.print(sql.concat("\n"));
                //stmt.executeUpdate(sql);
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    String lid = rs.getString("lid").toString();
                    sql = String.format("INSERT INTO Owns (username, lid) VALUES ('%s', '%s');", username, lid);
                    System.out.println(sql);
                    stmt.executeUpdate(sql);
                    return owner_add_address(postal_code, unit, city, country, lid);
                }
                return false;
            } else {
                System.out.println("Invalid Listing.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong with Listing");
            return false;
        }
    }

    public static boolean get_profile(String username) throws SQLException {
        try {
            String sql = String.format("SELECT * FROM user where username = '%s';", username);
            ResultSet rs = stmt.executeQuery(sql);
            //STEP 5: Extract data from result set
            while (rs.next()) {
                int sid = rs.getInt("SIN");
                String name = rs.getString("name");
                String occupation = rs.getString("occupation");
                int birth = rs.getInt("birth");
                String password = rs.getString("password");
                //Display values
                System.out.print("SIN: " + sid);
                System.out.print(", Username: " + username);
                System.out.print(", Password: " + password);
                System.out.print(", Real Name: " + name);
                System.out.print(", birth: " + birth);
                System.out.println(", Occupation: " + occupation);
            }
            rs.close();
            String sql2 = String.format("select * from Lives NATURAL JOIN Address where username = '%s';", username);
            ResultSet rs2 = stmt.executeQuery(sql2);
            System.out.println(sql + "\n" + sql2);
            while (rs2.next()) {
                int unit = rs2.getInt("unit");
                String postal_code = rs2.getString("postal_code");
                String city = rs2.getString("city");
                String country = rs2.getString("country");
                //Display values
                System.out.print("Address: unit: " + unit);
                System.out.print(", Occupation: " + postal_code);
                System.out.print(", City: " + city);
                System.out.println(", Country: " + country);
            }
            rs2.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Cannot show profile");
            return false;
        }
    }

    public static boolean change_profile(String username, String to_change, int int_type) throws SQLException {
        try {
            String type;
            if (int_type == 1) {
                type = "name";
            } else if (int_type == 2) {
                type = "birth";
            } else if (int_type == 3) {
                type = "password";
            } else if (int_type == 4) {
                type = "occupation";
            } else {
                return false;
            }
            String sql = String.format("update user set %s = '%s' where username = '%s';", type, to_change, username);
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Cannot change my profile");
            return false;
        }
    }

    public static boolean change_address(String username, String postal_code, String unit, String city, String country) throws SQLException {
        try {
            System.out.println("here");
            String get_address = String.format("select * from Lives where username = '%s';", username);
            System.out.println("get_address");
            ResultSet rs = stmt.executeQuery(get_address);
            if (rs.next()) {
                //
                System.out.println("else");
                // update address
                String sql = String.format("update user set postal_code = '%s', unit = '%s', city = '%s', country = '%s' where username = '%s';", postal_code, unit, city, country, username);
                stmt.executeUpdate(sql);
            } else {
                System.out.println("rs = null");
                // add address
                String sql = String.format("INSERT INTO Address (postal_code, unit, city, country) VALUES ('%s','%s','%s','%s');", postal_code, unit, city, country);
                stmt.executeUpdate(sql);
                System.out.println(sql);
                String sql2 = String.format("INSERT INTO Lives (postal_code, unit, username) VALUES ('%s','%s','%s');", postal_code, unit, username);
                stmt.executeUpdate(sql2);
                System.out.println(sql2);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Cannot change my profile");
            return false;
        }
    }

    public static boolean owner_add_address(String postal_code, String unit, String city, String country, String lid) throws SQLException {
        try {
            String sql = String.format("INSERT INTO Address (postal_code, unit, city, country) VALUES ('%s','%s','%s','%s');", postal_code, unit, city, country);
            stmt.executeUpdate(sql);
            System.out.println(sql);
            String sql2 = String.format("INSERT INTO Located_At (postal_code, unit, lid) VALUES ('%s','%s','%s');", postal_code, unit, lid);
            stmt.executeUpdate(sql2);
            System.out.println(sql2);
            return true;
        } catch (SQLException e) {
            System.err.println("Cannot change my profile");
            return false;
        }
    }

    // my helper function
    public static String validate(Scanner sc) {
        String input;
        while (true) {
            input = sc.nextLine();
            if (!input.equals("")) {
                break;
            } else {
                print_error("Input cannot be NULL, please re submit your input.");
            }
        }
        return input;
    }

    public static String validate_postal_code(Scanner sc) {
        String input;
        String regex = "^(?!.*[DFIOQU])[A-VXY][0-9][A-Z] ?[0-9][A-Z][0-9]$";
        Pattern pattern = Pattern.compile(regex);
        while (true) {
            input = sc.nextLine();
            Matcher matcher = pattern.matcher(input);
            if (!matcher.matches()) {
                print_error("Input postal code is not valid!");
            } else {
                break;
            }
        }
        return input;
    }

    public static String validate_int(Scanner sc, int min, int max) {

        String result;
        int temp;
        while (true) {
            result = sc.nextLine();
            try {
                temp = Integer.parseInt(result);
            } catch (Exception e) {
                print_error("Input is not an integer.");
                continue;
            }

            if (temp >= min && max == 0) {
                break;
            }

            if (temp < min || temp > max) {
                print_error("Input range is not valid.");
            } else {
                break;
            }
        }
        return result;
    }

    public static String validate_double(Scanner sc, double min, double max) {

        String result;
        double temp;
        while (true) {
            result = sc.nextLine();
            try {
                temp = Double.parseDouble(result);
            } catch (Exception e) {
                print_error("Input is not an double.");
                continue;
            }

            if (temp >= min && max == 0) {
                break;
            }

            if (temp < min || temp > max) {
                print_error("Input range is not valid.");
            } else {
                break;
            }
        }
        return result;
    }

    public static void select_profile(Scanner sc) throws SQLException {
        System.out.println(half_line + "Update Information" + half_line);
        System.out.println("1: Check My Profile, 2: Change My Profile, 3: Delete My Account");
        String profile_decide = sc.nextLine();
        if (profile_decide.equals("1")) {
            print_header("Check My Profile");
            boolean success = get_profile(username);
            if (!success) {
                print_error("Cannot show my profile");
            }
        } else if (profile_decide.equals("2")) {
            print_header("Change My Profile");
            // select what your want to change
            System.out.println("Select the profile you want to change");
            System.out.println("1: Real Name, 2: Birth Year, 3: Password, 4: Occupation, 5: Address.");
            String change_profile_decide = validate_int(sc, 1, 5);
            int change_profile_decide_int = Integer.parseInt(change_profile_decide);
            if (change_profile_decide_int == 5) {
                // change address
                print_header("Change Address");
                System.out.println("Change Address - Please input your postal code.");
                // error checking of postal code
                String postal_code = validate_postal_code(sc);
                System.out.println("Change Address - Please input your unit.");
                String unit = validate_int(sc, 0, 9999);
                System.out.println("Change Address - Please input your city.");
                String city = validate(sc);
                System.out.println("Change Address - Please input your country.");
                String country = validate(sc);
                if (change_address(username, postal_code, unit, city, country)) {
                    print_header("Successfully change address");
                } else {
                    print_error("Cannot change address");
                }
            } else {
                System.out.println("What do you want to change to?");
                String to_change;
                if (change_profile_decide_int == 2) {
                    to_change = validate_int(sc, 1800, 2022);
                } else {
                    to_change = validate(sc);
                }
                if (change_profile(username, to_change, change_profile_decide_int)) {
                    System.out.println("Successfully change profile");
                } else {
                    print_error("Cannot change profile");
                }
            }
        } else if (profile_decide.equals("3")) {
            print_header("Delete My Account");
            // how to delete my account?
            // delete my profile
            // what should we delete?
        } else {
            // not valid
            System.out.println("Not Valid input in Update Information!");
        }
    }

    public static void print_header(String string) {
        System.out.println(half_line + string + half_line);
    }

    public static void print_error(String string) {
        System.err.println(half_line + string + half_line);
    }

    public static void end_of_owner() {
        System.out.println(a_line);
        System.out.println("Press the Corresponding number to continue");
        System.out.println(is_owner_prompt);
    }

    public static void end_of_renter() {
        System.out.println(a_line);
        System.out.println("Press the Corresponding number to continue");
        System.out.println(is_renter_prompt);
    }
}
