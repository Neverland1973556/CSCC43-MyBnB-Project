import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDBCExample {
    // database information
    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1:3306";
    private static final String USER = "root";
    private static final String PASS = "123456";
    // private static final String PASS = "123456";
    // connection
    private static Statement stmt;
    private static Connection conn;
    // state of the while loop
    private static int is_login = 0;
    private static int is_owner = -1;

    private static boolean is_admin = false;

    // user data
    private static String username;

    // some strings
    private static final String is_owner_prompt = "1: Personal Information, 2: Manage My Listings, 3: Listing Availability, 4: Check Booking, 5: Rate User, 9: logout";
    private static final String is_renter_prompt = "1: Personal Information, 2: Manage My Booking, 4. Comment Listing 5: Rate User, 9: logout";
    private static final String report_message = "1: Booking by city, 2: Booking by postal-code, 3: Num of booking per country, city, and zip code, 4. Rank host by num of listing within a country, 5: Flaggd host, \n6:Renter ranked by booking, 7:Largest cancellation 10: logout";
	private static final String a_line = "--------------------------------------------------"
            + "--------------------------------------------------";
    private static final String half_line = "--------------------------------------------------";

    private static final String start_date = "2022-01-01";
    private static final String end_date = "2024-01-01";
    private static final String valid_date = "Valid date: " + start_date + " to " + end_date + ".";

    public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, SQLException {
        Class.forName(dbClassName);
        // connection
        System.out.println("Connecting to database...");
        conn = DriverManager.getConnection(CONNECTION, USER, PASS);
        stmt = conn.createStatement();
        try {
            // initialize the database
            System.out.println("Successfully connected to MySQL!");

            // File setup = new File("project/src/setup_table.sql");
            File setup = new File("project/src/setup_table.sql");
            assert (setup.exists());
            System.out.println("Preparing start up database...");
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
                String data = set.next();
                // System.out.println(data);
                stmt.execute(data);
            }
            set.close();

            String calendar_sql = "DROP PROCEDURE IF EXISTS insert_year_dates;";
            stmt.execute(calendar_sql);
            calendar_sql = "create PROCEDURE insert_year_dates()\n" +
                    "BEGIN\n" +
                    // "    SET @t_current = DATE(NOW());\n" +
                    // "    SET @t_end = DATE(DATE_ADD(NOW(), INTERVAL 1 YEAR));\n" +
                    "    SET @t_current = DATE('" + start_date + "');\n" +
                    "    SET @t_end = DATE('" + end_date + "');\n" +
                    "    WHILE(@t_current< @t_end) DO\n" +
                    "        INSERT INTO Calendar (date) VALUES (@t_current);\n" +
                    "        SET @t_current = DATE_ADD(@t_current, INTERVAL 1 DAY);\n" +
                    "    END WHILE;\n" +
                    "END;";
            stmt.execute(calendar_sql);
            calendar_sql = "CALL insert_year_dates();";
            stmt.execute(calendar_sql);

            // setup = new File("project/src/insert_data.sql");
            setup = new File("project/src/insert_data.sql");
            assert (setup.exists());
            set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
                String data = set.next();
                // System.out.println(data);
                stmt.execute(data);
            }
            set.close();

            // scanner initialization
            Scanner sc = new Scanner(System.in);
            sc.useDelimiter("\n");

            // the while loop start
            label_whole:
            while (true) {
                // didn't login
                if (is_login == 0) {
                    System.out.println(a_line);
                    System.out.println("Please Login or register.");
                    System.out.println("Press 1 to Login, 2 to Register, 3: Administer Mode.");
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
                            System.out.println("REGISTER - Please input your birth day in form of yyyy-mm-dd.");
                            String birth_year = validate_time(sc);
                            LocalDate d1 = LocalDate.parse(birth_year);
                            LocalDate d3 = LocalDate.parse("2004-01-01");
                            if (d1.isAfter(d3)) {
                                print_header("To young to register, should over 18.");
                            } else {
                                System.out.println("REGISTER - Please input your SIN.");
                                String sin = validate_int(sc, 0, 0);

                                String reg = "Name:" + real_name + ", Password:" + password + ", SIN:" + sin + ", Birth:" + birth_year + ", UserName:" + username;
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
                            }
                        } else if (input.equals("3")) {
                            print_header("You are in administer mode");
                            is_admin = true;
                            break;
                        } else if (input.equals("break")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        }
                        // not valid
                        print_error("Please use valid input!");
                        System.out.println(a_line);
                        System.out.println("Press 1 to Login, 2 to Register, 3: Administer Mode.");
                    }
                }

                // output the report
                if (is_admin) {
                    // can see report
                    System.out.println("Select a report to see!");
                    System.out.println(report_message);
                    // todo: report
                    while (sc.hasNextLine()) {
                        String input = validate_int(sc, 1, 10);

                        if (input.equals("1")) {
                            print_header("Number of booking within a date range by city");
                            System.out.println("Please input the start date in this format: yyyy-mm-dd");
                            String start_time = validate_time(sc);
                            System.out.println("Please input the end date in this format: yyyy-mm-dd");
                            String end_time = validate_time(sc);

                            String reportsql = String.format("select COUNT(BID) as bookid, city from Book Natural join listing natural join located_at natural join address where start_date >= '%s' and end_date <= '%s' group by city;", start_time, end_time);
                            ResultSet rs = stmt.executeQuery(reportsql);
                            int count = 0;
                            while (rs.next()) {
                                count++;
                                String bookid = rs.getString("bookid");
                                System.out.printf("Number of books: %s", bookid);
                                String city = rs.getString("city");
                                System.out.printf(", City: %s\n", city);
                            }
                            if (count == 0) {
                                System.out.println("No booking within these days.");
                            }
                        } else if (input.equals("2")) {
                            print_header("Number of booking within a date range by postal_code within a city");
                            System.out.println("Please input the start date in this format: yyyy-mm-dd");
                            String start_time = validate_time(sc);
                            System.out.println("Please input the end date in this format: yyyy-mm-dd");
                            String end_time = validate_time(sc);
                            System.out.println("Please input the city:");
                            String city = validate(sc);
                            String reportsql = String.format("select COUNT(BID) as bookid, postal_code from Book Natural join listing natural join located_at natural join address where start_date >= '%s' and end_date <= '%s' and city = '%s' group by postal_code;", start_time, end_time, city);
                            ResultSet rs = stmt.executeQuery(reportsql);
                            int count = 0;
                            while (rs.next()) {
                                count++;
                                String bookid = rs.getString("bookid");
                                System.out.printf("Number of books: %s", bookid);
                                // String city = rs.getString("city");
                                String postal_code = rs.getString("postal_code");
                                System.out.printf(", Postal Code: %s", postal_code);
                                System.out.printf(", City: %s\n", city);

                            }
                            if (count == 0) {
                                System.out.println("No booking within these days and this city.");
                            }
                        } else if (input.equals("3")) {
                            report3();

                        } else if (input.equals("4")) {
                            report4(sc);
                        } else if (input.equals("5")) {
                            report5(sc);
                        } else if (input.equals("6")) {
                            report6(sc);
                        } else if (input.equals("7")) {
                            report7();
                        } else if (input.equals("10")) {
                            is_admin = false;
                            continue label_whole;
                        }
                        System.out.println(a_line);

                        //System.out.println("Continue with Owner Account or Renter Account?");
                        System.out.println(report_message);
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
                        String input = validate_int(sc, 1, 9);
                        if (input.equals("1")) {
                            select_profile(sc);
                            if (is_owner == -1) {
                                continue label_whole;
                            }
                        } else if (input.equals("2")) {
                            print_header("Manage My Listings");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Check My Listings, 2: Add a Listing, 3: Change a Listing, 4: Comments of my Listings, 5: Go Back.");
                            while (sc.hasNextLine()) {
                                String listing_decide = validate_int(sc, 1, 5);
                                if (listing_decide.equals("1")) {
                                    print_header("Check My Listings");
                                    // get all my listings
                                    if (!show_user_owns(username)) {
                                        print_header("Currently you don't have a listing.");
                                    }
                                } else if (listing_decide.equals("2")) {
                                    // add a listing to database
                                    print_header("Add a Listing");
                                    System.out.println("Do you want to add a listing? Press 1 to continue.");
                                    String add_input = validate(sc);
                                    if (add_input.equals("1")) {
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
                                        System.out.println("Add a Listing - Please add your amenities");
                                        recommend_amenities();
                                        System.out.println("Add a Listing - Input the corresponding number and put space in between");
                                        System.out.println("1: wifi, 2: kitchen, 3: washer, 4: dryer, 5: ac, 6: heating, 7: tv, 8: hair dryer, 9: gym");
                                        String[] am = validate_am(sc);

                                        System.out.println("Add Listing Address - Please input your postal code.");
                                        String postal_code = validate_postal_code(sc);
                                        System.out.println("Add Listing Address - Please input your unit.");
                                        String unit = validate_int(sc, 0, 9999);
                                        System.out.println("Add Listing Address - Please input your city.");
                                        String city = validate(sc);
                                        System.out.println("Add Listing Address - Please input your country.");
                                        String country = validate(sc);
                                        if (check_address_owner(postal_code, unit, city, country)) {
                                            System.out.println("Address is valid");
                                            String reg = "UserName:" + username + ", Lon:" + longitude + ", Lat:" + latitude + ", Type:" + listing_type;
                                            System.out.println(reg);
                                            String success = create_listing(reg, postal_code, unit, city, country, am);
                                            if (success.equals("false")) {
                                                print_error("Cannot add, please try again");
                                            } else {
                                                // success, can continue
                                                System.out.println("Add success!");
                                            }
                                        }
                                    }
                                    // else not == 1

                                } else if (listing_decide.equals("3")) {
                                    print_header("Change a Listing Information");
                                    System.out.println("Press the Corresponding number to continue.");
                                    System.out.println("1: Modify a Listing, 2: Delete a Listing, 3: Go Back.");
                                    String change_listing_decide = sc.nextLine();
                                    label_listing:
                                    if (change_listing_decide.equals("1")) {
                                        print_header("Modify a Listing");
                                        if (!show_user_owns(username)) {
                                            print_header("Currently you don't have a listing.");
                                            break label_listing;
                                        }
                                        System.out.println("Type the lid of the list your want to modify.");
                                        String lid = validate(sc);
                                        if (!check_lid_in_book(lid)) {
                                            // it's in book where cancel == 0
                                            print_error("It's in a book, you cannot change the listing information.");
                                            break label_listing;
                                        }
                                        System.out.println("Choose the property of the listing you want to modify.");
                                        System.out.println("1: latitude, 2: longitude, 3: type, 4: address, 5: amenities");
                                        String modify_listing_decide = sc.nextLine();
                                        String to_change;
                                        int type;
                                        if (modify_listing_decide.equals("1")) {
                                            print_header("Modify listing - latitude");
                                            System.out.println("Type the latitude your want to change to.");
                                            to_change = validate_double(sc, -90, 90);
                                            type = 1;
                                            if (handle_modify_listing(lid, type, to_change)) {
                                                print_header("modify success");
                                            } else {
                                                print_error("cannot modify");
                                            }
                                        } else if (modify_listing_decide.equals("2")) {
                                            print_header("Modify listing - longitude");
                                            System.out.println("Type the longitude your want to change to.");
                                            to_change = validate_double(sc, -180, 180);
                                            type = 2;
                                            if (handle_modify_listing(lid, type, to_change)) {
                                                print_header("modify success");
                                            } else {
                                                print_error("cannot modify");
                                            }

                                        } else if (modify_listing_decide.equals("3")) {
                                            print_header("Modify listing - type");
                                            System.out.println("Type the listing type your want to change to.");
                                            System.out.println("1: full house, 2: apartment, 3: room");
                                            to_change = validate_int(sc, 1, 3);
                                            int listing_type_int = Integer.parseInt(to_change);
                                            if (listing_type_int == 1) {
                                                to_change = "full house";
                                            } else if (listing_type_int == 2) {
                                                to_change = "apartment";
                                            } else {
                                                to_change = "room";
                                            }
                                            type = 3;
                                            if (handle_modify_listing(lid, type, to_change)) {
                                                print_header("modify success");
                                            } else {
                                                print_error("cannot modify");
                                            }
                                        } else if (modify_listing_decide.equals("4")) {
                                            print_header("Modify listing - address");
                                            System.out.println("Modify listing address - Please input your new postal code.");
                                            String postal_code = validate_postal_code(sc);
                                            System.out.println("Modify listing address - Please input your new unit.");
                                            String unit = validate_int(sc, 0, 9999);
                                            System.out.println("Modify listing address - Please input your new city.");
                                            String city = validate(sc);
                                            System.out.println("Modify listing address - Please input your new country.");
                                            String country = validate(sc);
                                            if (change_address_listing(lid, postal_code, unit, city, country)) {
                                                print_header("modify success");
                                            } else {
                                                print_error("cannot modify");
                                            }
                                        } else if (modify_listing_decide.equals("5")) {
                                            print_header("Modify listing - amenities");
                                            System.out.println("Please choose your amenities");
                                            recommend_amenities();
                                            System.out.println("Input the corresponding number and put space in between");
                                            System.out.println("1: wifi, 2: kitchen, 3: washer, 4: dryer, 5: ac, 6: heating, 7: tv, 8: hair dryer, 9: gym");
                                            String[] am = validate_am(sc);
                                            if (handle_modify_am(lid, am)) {
                                                print_header("modify success");
                                            } else {
                                                print_error("cannot modify");
                                            }
                                        } else {
                                            print_error("Not Valid input when change a Listing!");
                                        }
                                    } else if (change_listing_decide.equals("2")) {
                                        print_header("Delete a Listing");
                                        if (!show_user_owns(username)) {
                                            print_header("Currently you don't have a list");
                                            break label_listing;
                                        }
                                        System.out.println("Type the lid of the listing your want to delete.");
                                        String lid = validate(sc);
                                        if (!check_lid_in_book(lid)) {
                                            // it's in book where cancel == 0
                                            print_error("It's in a book, you cannot delete the listing.");
                                            break label_listing;
                                        }
                                        if (handle_delete_listing(username, lid)) {
                                            print_header("delete success");
                                        } else {
                                            print_error("cannot delete");
                                        }
                                    } else if (change_listing_decide.equals("3")) {
                                        // do nothing
                                    } else {
                                        print_error("Not Valid input when change a Listing!");
                                    }
                                } else if (listing_decide.equals("5")) {
                                    break;
                                } else if (listing_decide.equals("4")) {
                                    print_header("Comment of my Listing");
                                    if (!show_user_owns(username)) {
                                        print_error("You don't have any listings so far.");
                                        continue;
                                    }
                                    System.out.println("Select the listing's LID which you want to look at: (input any other number to go back)");
                                    // boolean indicator = false;

                                    while (sc.hasNextLine()) {
                                        String lid = validate_int(sc, 0, 99999);
                                        if (!show_listing_comment(lid, username)) {
                                            break;
                                        }
                                        System.out.println("Select the listing's LID which you want to look at:(input any other number to go back)");
                                    }
                                }
                                System.out.println("1: Check My Listings, 2: Add a Listing, 3: Change a Listing, 4: Comments of my Listings, 5: Go Back.");
                            }
                        } else if (input.equals("3")) {
                            print_header("Listing Availability");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show Availability, 2: Add Availability, 3: Change Availability, 4: Go Back.");
                            while (sc.hasNextLine()) {
                                String avail = validate_int(sc, 1, 4);
                                label_if_avail:
                                if (avail.equals("1")) {
                                    print_header("Show Availability");
                                    if (!show_user_owns(username)) {
                                        print_header("Currently you don't have a listing.");
                                        break label_if_avail;
                                    }
                                    System.out.println("Please type the lid your want to check");
                                    String lid = validate_int(sc, 0, 9999);
                                    if (!show_available(lid)) {
                                        print_error("Cannot find availability for the given lid.");
                                    }

                                } else if (avail.equals("2")) {
                                    print_header("Add Availability");
                                    if (!show_user_owns(username)) {
                                        print_header("Currently you don't have a listing.");
                                        break label_if_avail;
                                    }
                                    // new add will replace old one, add can be not success
                                    System.out.println("Please type the lid you want to add availability to");
                                    String lid = validate_int(sc, 1, 10000);
                                    print_header("Add date period for Listing Available.");
                                    System.out.println(valid_date);
                                    System.out.println("Please input the start date in this format: yyyy-mm-dd");
                                    String start_time = validate_time(sc);
                                    System.out.println("Please input the end date in this format: yyyy-mm-dd");
                                    String end_time = validate_time(sc);
                                    LocalDate d1;
                                    LocalDate d2;
                                    try {
                                        d1 = LocalDate.parse(start_time);
                                        d2 = LocalDate.parse(end_time);
                                    } catch (Exception e) {
                                        print_error("input date is not valid");
                                        end_of_owner();
                                        break label_if_avail;
                                    }
                                    LocalDate d3 = LocalDate.parse("2022-01-01");
                                    LocalDate d4 = LocalDate.parse("2024-01-01");
                                    if (d1.isAfter(d2)) {
                                        print_error("End date is before start date.");
                                        end_of_owner();
                                        break label_if_avail;
                                    }
                                    if (d3.isAfter(d1)) {
                                        print_error("Start date is too early");
                                        end_of_owner();
                                        break label_if_avail;
                                    }
                                    if (d2.isAfter(d4)) {
                                        print_error("End date is too late");
                                        end_of_owner();
                                        break label_if_avail;
                                    }
                                    System.out.println("Please input daily price of the available:");
                                    recommend_price(lid);
                                    String price = validate_double(sc, 0, 9999);
                                    // in this period, this listing is this price
                                    add_available(d1, d2, price, lid, false);
                                } else if (avail.equals("3")) {
                                    print_header("Change Availability");
                                    System.out.println("1: Change Availability Price, 2: Remove Existing Availability, 3: Go Back.");
                                    String change_avail = validate_int(sc, 1, 3);
                                    if (change_avail.equals("1")) {
                                        print_header("Change Availability Price.");
                                        if (!show_user_owns(username)) {
                                            print_header("Currently you don't have a listing.");
                                            break label_if_avail;
                                        }
                                        // new add will replace old one, add can be not success
                                        System.out.println("Please type the lid you want to change it's availability");
                                        String lid = validate_int(sc, 1, 10000);
                                        print_header("Add date period for changing Listing Available.");
                                        System.out.println(valid_date);
                                        System.out.println("Please input the start date in this format: yyyy-mm-dd");
                                        String start_time = validate_time(sc);
                                        System.out.println("Please input the end date in this format: yyyy-mm-dd");
                                        String end_time = validate_time(sc);
                                        LocalDate d1;
                                        LocalDate d2;
                                        try {
                                            d1 = LocalDate.parse(start_time);
                                            d2 = LocalDate.parse(end_time);
                                        } catch (Exception e) {
                                            print_error("input date is not valid");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        LocalDate d3 = LocalDate.parse("2022-01-01");
                                        LocalDate d4 = LocalDate.parse("2024-01-01");
                                        if (d1.isAfter(d2)) {
                                            print_error("End date is before start date.");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        if (d3.isAfter(d1)) {
                                            print_error("Start date is too early");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        if (d2.isAfter(d4)) {
                                            print_error("End date is too late");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        System.out.println("Please input daily price of the available:");
                                        recommend_price(lid);
                                        String price = validate_double(sc, 0, 9999);
                                        // in this period, this listing is this price
                                        // hard = true, will delete first
                                        add_available(d1, d2, price, lid, true);

                                    } else if (change_avail.equals("2")) {
                                        print_header(" Remove Availability.");
                                        if (!show_user_owns(username)) {
                                            print_header("Currently you don't have a listing.");
                                            break label_if_avail;
                                        }
                                        // new add will replace old one, add can be not success
                                        System.out.println("Please type the lid you want to remove it's availability");
                                        String lid = validate_int(sc, 1, 10000);
                                        print_header("Add date period for remove Listing Available.");
                                        System.out.println(valid_date);
                                        System.out.println("Please input the start date in this format: yyyy-mm-dd");
                                        String start_time = validate_time(sc);
                                        System.out.println("Please input the end date in this format: yyyy-mm-dd");
                                        String end_time = validate_time(sc);
                                        LocalDate d1;
                                        LocalDate d2;
                                        try {
                                            d1 = LocalDate.parse(start_time);
                                            d2 = LocalDate.parse(end_time);
                                        } catch (Exception e) {
                                            print_error("input date is not valid");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        LocalDate d3 = LocalDate.parse("2022-01-01");
                                        LocalDate d4 = LocalDate.parse("2024-01-01");
                                        if (d1.isAfter(d2)) {
                                            print_error("End date is before start date.");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        if (d3.isAfter(d1)) {
                                            print_error("Start date is too early");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        if (d2.isAfter(d4)) {
                                            print_error("End date is too late");
                                            end_of_owner();
                                            break label_if_avail;
                                        }
                                        remove_available(d1, d2, lid);
                                    } else if (change_avail.equals("3")) {
                                        // continue;
                                    }
                                } else if (avail.equals("4")) {
                                    break;
                                }
                                System.out.println("1: Show Availability, 2: Add Availability, 3: Change Availability, 4: Go Back.");
                            }
                        } else if (input.equals("4")) {
                            System.out.println("Check Booking");
                            print_header("Show My Bookings - Owner");
                            if (!show_owner_books(username)) {
                                print_header("you don't have any booking");
                            } else {
                                System.out.println("Do you want to cancel existing booking?");
                                System.out.println("Press 1 to cancel a booking, other to go back");
                                String booking_input = validate(sc);
                                if (booking_input.equals("1")) {
                                    System.out.println("Input the booking id you want to cancel.");
                                    String bid = validate_int(sc, 1, 9999);
                                    if (!cancel_book(bid, 2)) {
                                        print_error("Cannot cancel book.");
                                    }
                                } else {
                                    print_header("Go Back.");
                                }
                            }
                        } else if (input.equals("5")) {
                            print_header("Rate User");

                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show how I was being rated as a host, 2: Show what I've rated, 3: Rate a renter, 4: Delete a rating, 5: Go Back.");
                            while (sc.hasNextLine()) {
                                String booking_decide = sc.nextLine();
                                if (booking_decide.equals("1")) {
                                    print_header("Show how I was being rated");
                                    if (!show_host_s_judgement(username)) {
                                        print_header("No renter has rated you so far.");
                                    }
                                } else if (booking_decide.equals("2")) {
                                    print_header("Show what I've rated");
                                    if (!show_host_s_rate(username)) {
                                        print_header("You haven't rate anyone yet.");
                                    }
                                } else if (booking_decide.equals("3")) {
                                    print_header("Rate a renter");
                                    String sql = String.format("SELECT distinct Book.username as rentername, BID, Owns.lid as llid, postal_code, unit FROM Book Join Owns join Located_At where Book.lid = Owns.lid and Owns.lid = Located_At.lid and cancellation = 0 and Owns.username = '%s';", username);
                                    ResultSet rs = stmt.executeQuery(sql);
                                    int count = 0;
                                    while (rs.next()) {
                                        count++;
                                        String bid = rs.getString("BID");
                                        String renter = rs.getString("rentername");
                                        String lid = rs.getString("llid");
                                        String postal_code = rs.getString("postal_code");
                                        String unit = rs.getString("unit");
                                        System.out.format("Renter: %s, BID: %s, Lid: %s, Postal Code: %s, Unit: %s\n", renter, bid, lid, postal_code, unit);
                                    }
                                    if (count == 0) {
                                        System.out.println("You can't comment any renter yet, no one booked your house!");
                                        print_header("Rate User");
                                        System.out.println("Press the Corresponding number to continue");
                                        System.out.println("1: Show how I was being rated as a host, 2: Show what I've rated, 3: Rate a renter, 4: Delete a rating, 5: Go Back.");
                                        continue;
                                    }
                                    System.out.println("Please input the renter's username that you want to rate");
                                    String renter_username = validate(sc);
                                    System.out.println("Please input how much you like this renter from 1-5");
                                    String likes = validate_int(sc, 1, 5);
                                    System.out.println("Please make any comment (100 words limit):");
                                    String words = validate(sc);

                                    create_host_rate(renter_username, username, likes, words);
                                } else if (booking_decide.equals("4")) {
                                    // cannot change a book
                                    print_header("Delete a rating");
                                    System.out.println("Input the JID that you want to cancel (If you don't want to delete, just input any other integer)");
                                    if (!show_host_s_rate(username)) {
                                        print_header("you haven't rate anyone");
                                    }
                                    String jid = validate_int(sc, 1, 9999);
                                    if (!cancel_judgement_host(jid, username)) {
                                        print_error("Cannot cancel the rate");
                                    }
                                } else if (booking_decide.equals("5")) {
                                    // go back
                                    break;
                                }
                                print_header("Rate User");
                                System.out.println("Press the Corresponding number to continue");
                                System.out.println("1: Show how I was being rated as a host, 2: Show what I've rated, 3: Rate a renter, 4: Delete a rating, 5: Go Back.");
                            }


                        } else if (input.equals("8")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            print_header("Log Out");
                            continue label_whole;
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
                        String input = validate_int(sc, 1, 9);
                        if (input.equals("1")) {
                            select_profile(sc);
                            if (is_owner == -1) {
                                continue label_whole;
                            }
                        } else if (input.equals("2")) {
                            print_header("Booking a list");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show my bookings, 2: Book a listing, 3: Cancel a booking, 4: Go Back.");
                            while (sc.hasNextLine()) {
                                String booking_decide = sc.nextLine();
                                label_if_book:
                                if (booking_decide.equals("1")) {
                                    print_header("Show my bookings");
                                    if (!show_user_books(username)) {
                                        print_header("you don't have any booking");
                                    }
                                } else if (booking_decide.equals("2")) {
                                    print_header("Book a listing");
                                    // have a start and end date
                                    LocalDate d1;
                                    LocalDate d2;
                                    String lid = "";
                                    System.out.println("Please input the start booking date in this format: yyyy-mm-dd");
                                    String start_time = validate_time(sc);
                                    System.out.println("Please input the end booking date in this format: yyyy-mm-dd");
                                    String end_time = validate_time(sc);
                                    try {
                                        d1 = LocalDate.parse(start_time);
                                        d2 = LocalDate.parse(end_time);
                                    } catch (Exception e) {
                                        print_error("input date is not valid");
                                        break label_if_book;
                                    }
                                    LocalDate d3 = LocalDate.parse("2022-01-01");
                                    LocalDate d4 = LocalDate.parse("2024-01-01");
                                    if (d1.isAfter(d2)) {
                                        print_error("End date is before start date.");
                                        break label_if_book;
                                    }
                                    if (d3.isAfter(d1)) {
                                        print_error("Start date is too early");
                                        break label_if_book;
                                    }
                                    if (d2.isAfter(d4)) {
                                        print_error("End date is too late");
                                        break label_if_book;
                                    }

                                    // specific search or vague search
                                    System.out.println("Do you have a specific address (unit & postal_code) want to book?");
                                    System.out.println("1: yes, 2: No");
                                    String address_input = validate_int(sc, 1, 2);
                                    if (address_input.equals("1")) {
                                        System.out.println("Please input the postal_code.");
                                        String postal_code = validate_postal_code(sc);
                                        System.out.println("Please input the unit.");
                                        String unit = validate_int(sc, 0, 99999);
                                        // need to get some information of the list
                                        lid = search_lid(d1, d2, postal_code, unit);
                                        if (lid.equals("")) {
                                            // cannot find available lid
                                            print_header("No this listing or this listing is not available;");
                                            break label_if_book;
                                        }
                                    } else {
                                        System.out.println("Do you want to search by a postal_code and it's nearby listings?");
                                        System.out.println("1: yes, 2: No");
                                        String vague_input_1 = validate_int(sc, 1, 2);
                                        if (vague_input_1.equals("1")) {
                                            System.out.println("Please Input the postal_code.");
                                            String postal_code = validate_postal_code(sc);
                                            // going to show all lid that's available
                                            if (!find_lid_by_postal_code(postal_code, d1, d2)) {
                                                print_header("No listing qualified.");
                                                break label_if_book;
                                            }
                                        } else {
                                            System.out.println("How about a lat & lon with distance between which you want to book?");
                                            System.out.println("1: yes, 2: No");
                                            String vague_input_2 = validate_int(sc, 1, 2);
                                            if (vague_input_2.equals("1")) {
                                                System.out.println("Please input your aim latitude.");
                                                String lat = validate_double(sc, -90, 90);
                                                System.out.println("Please input your aim longitude.");
                                                String lon = validate_double(sc, -180, 180);
                                                System.out.println("Please input distance between (unit: degree, max: 90).");
                                                String distance = validate_double(sc, 0, 90);
                                                if (!find_lid_by_location(lat, lon, d1, d2, distance)) {
                                                    print_header("No listing qualified.");
                                                    break label_if_book;
                                                }
                                            } else {
                                                System.out.println("How about by amenities you want?");
                                                System.out.println("1: yes, 2: No");
                                                String vague_input_3 = validate_int(sc, 1, 2);
                                                if (vague_input_3.equals("1")) {
                                                    System.out.println("Please input the amenities you want - input the corresponding number and put space in between");
                                                    System.out.println("1: wifi, 2: kitchen, 3: washer, 4: dryer, 5: ac, 6: heating, 7: tv, 8: hair dryer, 9: gym");
                                                    String[] am = validate_am(sc);
                                                    if (!find_lid_by_am(d1, d2, am)) {
                                                        print_header("No listing qualified.");
                                                        break label_if_book;
                                                    }
                                                } else {
                                                    System.out.println("Get all available with the time period.");
                                                    if (!get_available(d1, d2)) {
                                                        print_header("No listing qualified.");
                                                        break label_if_book;
                                                    }
                                                }
                                            }
                                        }
                                        System.out.println("Please select the lid you want to book");
                                        lid = validate_int(sc, 1, 9999);
                                    }

                                    // at this point, we should have d1, d2, lid, start_time, end_time
                                    // get the price and payment method
                                    int total_price = get_price_book(lid, d1, d2);
                                    if (total_price == 0) {
                                        print_error("Input lid is not valid");
                                        break label_if_book;
                                    }
                                    // get the total_price, get payment method
                                    String payment = get_payment();
                                    if (payment == null) {
                                        print_header("Didn't detect credit card payment, Please input new card.");
                                        payment = validate(sc);
                                    } else {
                                        System.out.println("Get stored credit card number: " + payment);
                                    }
                                    System.out.println("Ready to book the listing? 1 to continue.");
                                    String ready = validate(sc);
                                    if (!ready.equals("1")) {
                                        print_header("Not ready");
                                        break label_if_book;
                                    } else {
                                        // == 1
                                        create_book(payment, total_price, start_time, end_time, lid);
                                    }
                                } else if (booking_decide.equals("3")) {
                                    // cannot change a book
                                    print_header("Cancel a booking");
                                    System.out.println("Input the bid that you want to cancel");
                                    if (!show_user_books(username)) {
                                        print_header("you don't have any booking");
                                    }
                                    String bid = validate_int(sc, 1, 9999);
                                    if (!cancel_book(bid, 1)) {
                                        print_error("Cannot cancel book");
                                    }
                                } else if (booking_decide.equals("4")) {
                                    break;
                                }
                                print_header("Booking a list");
                                System.out.println("Press the Corresponding number to continue");
                                System.out.println("1: Show my bookings, 2: Book a listing, 3: Cancel a booking, 4: Go Back.");
                            }
                        } else if (input.equals("5")) {
                            print_header("Rate User");

                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show how I was being rated as a renter, 2: Show what I've rated, 3: Rate a host, 4: Delete a rating, 5: Go Back.");

                            while (sc.hasNextLine()) {
                                String booking_decide = sc.nextLine();
                                if (booking_decide.equals("1")) {
                                    print_header("Show how I was being rated");
                                    if (!show_renter_s_judgement(username)) {
                                        print_header("No host has rated you so far.");
                                    }
                                } else if (booking_decide.equals("2")) {
                                    print_header("Show what I've rated");
                                    if (!show_renter_s_rate(username)) {
                                        print_header("You haven't rate anyone yet.");
                                    }
                                } else if (booking_decide.equals("3")) {
                                    print_header("Rate a host");
                                    String sql = String.format("SELECT distinct Owns.username as hostname, Owns.lid as llid, postal_code, unit FROM Book Join Owns join Located_At where Book.lid = Owns.lid and Owns.lid = Located_At.lid and cancellation = 0 and Book.username = '%s';", username);
                                    ResultSet rs = stmt.executeQuery(sql);
                                    int count = 0;
                                    while (rs.next()) {
                                        count++;
                                        String host = rs.getString("hostname");
                                        String lid = rs.getString("llid");
                                        String postal_code = rs.getString("postal_code");
                                        String unit = rs.getString("unit");
                                        System.out.format("Host: %s, Lid: %s, Postal Code: %s, Unit: %s\n", host, lid, postal_code, unit);
                                    }
                                    if (count == 0) {
                                        System.out.println("You can't comment any host yet, you should book first!");
                                        print_header("Rate User");
                                        System.out.println("Press the Corresponding number to continue");
                                        System.out.println("1: Show how I was being rated as a host, 2: Show what I've rated, 3: Rate a renter, 4: Delete a rating, 5: Go Back.");
                                        continue;
                                    }
                                    System.out.println("Please input the host's username that you want to rate");
                                    String host_username = validate(sc);
                                    System.out.println("Please input how much you like this host from 1-5");
                                    String likes = validate_int(sc, 1, 5);
                                    System.out.println("Please make any comment (100 words limit):");
                                    String words = validate(sc);

                                    create_renter_rate(username, host_username, likes, words);
                                } else if (booking_decide.equals("4")) {
                                    // cannot change a book
                                    print_header("Delete a rating");
                                    System.out.println("Input the JID that you want to cancel (If you don't want to delete, just input any other integer)");
                                    if (!show_renter_s_rate(username)) {
                                        print_header("you don't have any booking");
                                    }
                                    String jid = validate_int(sc, 1, 9999);
                                    if (!cancel_judgement_renter(jid, username)) {
                                        print_error("Cannot cancel the rate");
                                    }
                                } else if (booking_decide.equals("5")) {
                                    break;
                                }
                                print_header("Rate User");
                                System.out.println("Press the Corresponding number to continue");
                                System.out.println("1: Show how I was being rated as a renter, 2: Show what I've rated, 3: Rate a host, 4: Delete a rating, 5: Go Back.");

                            }
                        } else if (input.equals("4")) {
                            // comment
                            print_header("Comment Listing");

                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show my comments, 2: Comment a listing, 3: Delete a comment, 5: Go Back.");
                            while (sc.hasNextLine()) {
                                String booking_decide = sc.nextLine();
                                if (booking_decide.equals("1")) {
                                    print_header("Show what I have commented");
                                    if (!show_renter_comment(username)) {
                                        print_header("You haven't comment any listing so far.");
                                    }
                                } else if (booking_decide.equals("2")) {
                                    print_header("Comment a Listing");
                                    if (!show_renter_booked_listing(username)) {
                                        print_header("You haven't book any listing so far.");
                                    }
                                    System.out.println("Please input the ListingID that you want to rate");
                                    String lid = validate_int(sc, 0, 9999);
                                    System.out.println("Please input how much you like this listing from 1-5");
                                    String rate = validate_int(sc, 1, 5);
                                    System.out.println("Please make any comment (250 words limit):");
                                    String text = validate(sc);

                                    create_renter_listing_comment(username, lid, rate, text);

                                } else if (booking_decide.equals("3")) {
                                    // cannot change a book
                                    print_header("Delete a rating");
                                    System.out.println("Input the LID of that comment that you want to delete (If you don't want to delete, just input any other integer)");
                                    if (!show_renter_comment(username)) {
                                        print_header("you don't have any comment");
                                    }
                                    String lid = validate_int(sc, 1, 9999);
                                    if (!delete_renter_listing_comment(username, lid)) {
                                        print_error("Cannot cancel the comment of this Listing");
                                    }
                                } else if (booking_decide.equals("5")) {
                                    // go back
                                    break;
                                }
                                print_header("Comment Listing");
                                System.out.println("Press the Corresponding number to continue");
                                System.out.println("1: Show my comments, 2: Comment a lising, 3: Delete a comment, 5: Go Back.");
                            }

                        } else if (input.equals("8")) {
                            print_header("Terminate the program.");
                            break label_whole;
                        } else if (input.equals("9")) {
                            // logout
                            is_login = 0;
                            is_owner = -1;
                            System.out.println("Try to logout");
                            continue label_whole;
                        }
                        end_of_renter();
                    }
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
        }
        System.out.println("Closing connection...");
        stmt.close();
        conn.close();
        System.out.println("Closing success!");
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
            e.printStackTrace();
            return false;
        }
    }

    // register function, register a user to the database
    public static boolean register(String input) throws SQLException {
        try {
            String reg = "Name:(?<name>.*), Password:(?<password>.*), SIN:(?<sin>.*), Birth:(?<birth>.*), UserName:(?<username>.*)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String name = matcher.group("name");
                String password = matcher.group("password");
                String sin = matcher.group("sin");
                String birth = matcher.group("birth");
                String username = matcher.group("username");
                String sql = String.format("INSERT INTO User (SIN, name, password, birth, username) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");", sin, name, password, birth, username);
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
            e.printStackTrace();
            return false;
        }
    }

    public static void report3() throws SQLException {
        try {
            print_header("Num of listing per ~~ ");
            String reportsql = String.format("select COUNT(listing.lid) as listid, country from listing natural join located_at natural join address group by country");
            ResultSet rs = stmt.executeQuery(reportsql);
            int count = 0;
            while (rs.next()) {
                count++;
                String numlist = rs.getString("listid");
                System.out.printf("Number of listing: %s", numlist);
                String country = rs.getString("country");
                System.out.printf(", Country: %s\n", country);

            }
            if (count == 0) {
                System.out.println("No Listing added.");
            }
            System.out.println(half_line);
            reportsql = String.format("select COUNT(listing.lid) as listid, country, city from listing natural join located_at natural join address group by country, city");
            rs = stmt.executeQuery(reportsql);
            count = 0;
            while (rs.next()) {
                count++;
                String numlist = rs.getString("listid");
                System.out.printf("Number of listing: %s", numlist);
                String city = rs.getString("city");
                // String postal_code = rs.getString("postal_code");
                // System.out.printf(", Postal Code: %s", postal_code);
                System.out.printf(", City: %s", city);
                String country = rs.getString("country");
                System.out.printf(", Country: %s\n", country);

            }
            if (count == 0) {
                System.out.println("No Listing added.");
            }
            System.out.println(half_line);
            reportsql = String.format("select COUNT(listing.lid) as listid, country, city, postal_code from listing natural join located_at natural join address group by country, city, postal_code");
            rs = stmt.executeQuery(reportsql);
            count = 0;
            while (rs.next()) {
                count++;
                String numlist = rs.getString("listid");
                System.out.printf("Number of listing: %s", numlist);
                String city = rs.getString("city");
                String postal_code = rs.getString("postal_code");
                System.out.printf(", Postal Code: %s", postal_code);
                System.out.printf(", City: %s", city);
                String country = rs.getString("country");
                System.out.printf(", Country: %s\n", country);

            }
            if (count == 0) {
                System.out.println("No Listing added.");
            }
            System.out.println(half_line);

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    public static void report4(Scanner sc) throws SQLException {
        try {
            print_header("Rank host by number of listing within a country");
			System.out.println("Please input: 1.per country, 2: per city and country");
			String choice = validate_int(sc, 1, 2);
			if(choice.equals("1")){
				System.out.println("Please input the country you want ot check:");
				String country = validate(sc);
				String reportsql = String.format("select COUNT(listing.lid) as listid, RANK() OVER(ORDER BY COUNT(listing.lid) DESC) as ranking, owns.username as hostname from listing natural join owns natural join located_at natural join address where country = '%s' group by owns.username;", country);
				ResultSet rs = stmt.executeQuery(reportsql);
				int count = 0;
				while(rs.next()){
					count++;
					String hostname = rs.getString("hostname");
					System.out.printf("Host: %s, ", hostname);
					String ranking = rs.getString("ranking");
					System.out.printf(", Ranking: %s", ranking);
					String listid = rs.getString("listid");
					System.out.printf(", Number of Listing: %s", listid);
					System.out.printf(", Country: %s\n", country);
				}
				if(count ==0){
					System.out.println("No listing in this country.");
				}
			}else if (choice.equals("2")){
				System.out.println("Please input the country you want to check:");
				String country = validate(sc);
				System.out.println("Please input the city you want to check:");
				String city = validate(sc);
				String reportsql = String.format("select COUNT(listing.lid) as listid, RANK() OVER(ORDER BY COUNT(listing.lid) DESC) as ranking, owns.username as hostname from listing natural join owns natural join located_at natural join address where country = '%s' and city = '%s' group by owns.username;", country, city);
				ResultSet rs = stmt.executeQuery(reportsql);
				int count = 0;
				while(rs.next()){
					count++;
					String hostname = rs.getString("hostname");
					System.out.printf("Host: %s, ", hostname);
					String ranking = rs.getString("ranking");
					System.out.printf(", Ranking: %s", ranking);
					String listid = rs.getString("listid");
					System.out.printf(", Number of Listing: %s", listid);
					System.out.printf(", City: %s", city);
					System.out.printf(", Country: %s\n", country);
				}
				if(count ==0){
					System.out.println("No listing in this city.");
				}
			}
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }
	
    public static void report6(Scanner sc) throws SQLException {
        try {
            print_header("Rank renter by number of booking within a specific time");
			System.out.println("Please input: 1.specify the time period, 2: specify the time period and the city");
			String choice = validate_int(sc, 1, 2);
			if(choice.equals("1")){
				System.out.println("Please input the start date in this format: yyyy-mm-dd");
				String start_time = validate_time(sc);
				System.out.println("Please input the end date in this format: yyyy-mm-dd");
				String end_time = validate_time(sc);
				System.out.println(a_line);
				// String city = validate(sc);
				String reportsql = String.format("select count(bid) as num, Rank() Over(Order by COUnt(bid) DESC) as ranking, book.username from book where cancellation = 0 and start_date >= '%s' and end_date <= '%s' group by book.username having num >= 2;", start_time, end_time);
				ResultSet rs = stmt.executeQuery(reportsql);
				int count = 0;
				while (rs.next()) {
					count++;
					String username = rs.getString("username");
					System.out.printf("Renter: %s", username);
					String ranking = rs.getString("ranking");
					String num = rs.getString("num");
					System.out.printf(", Ranking: %s", ranking);
					System.out.printf(", Number of booking: %s\n", num);

				}
				if (count == 0) {
					System.out.println("No valid renter within these days.");
				}
			}else if (choice.equals("2")){

				System.out.println("Please input the city you want to check:");
				String city = validate(sc);
				System.out.println("Please input the start date in this format: yyyy-mm-dd");
				String start_time = validate_time(sc);
				System.out.println("Please input the end date in this format: yyyy-mm-dd");
				String end_time = validate_time(sc);
                System.out.println(a_line);
				String reportsql = String.format("select count(bid) as num, Rank() Over(Order by COUnt(bid) DESC) as ranking, book.username, city from book natural join listing natural join located_at natural join address where cancellation = 0 and start_date >= '%s' and end_date <= '%s' and city = '%s' group by book.username having num >= 2;", start_time, end_time, city);
				ResultSet rs = stmt.executeQuery(reportsql);
				int count = 0;
				while (rs.next()) {
					count++;
					String username = rs.getString("username");
					System.out.printf("Renter: %s", username);
					String ranking = rs.getString("ranking");
					String num = rs.getString("num");
					System.out.printf(", Ranking: %s", ranking);
					System.out.printf(", Number of booking: %s", num);
					String cityy = rs.getString("city");
					System.out.printf(", City: %s\n", cityy);

				}
				if (count == 0) {
					System.out.println("No valid renter within these days in the city.");
				}
			}
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }
	public static void report5(Scanner sc) throws SQLException{
		try{
			print_header("Output the host that owns more than 10% listings in this city");
			System.out.println("Please input the country you want ot check:");
			String country = validate(sc);
			System.out.println("Please input the city you want ot check:");
			String city = validate(sc);
			String reportsql = String.format("select COUNT(listing.lid) as listid, owns.username as hostname, (select COUNT(listing.lid) as sum from listing natural join owns natural join located_at natural join address where country = '%s' and city = '%s') as sum from listing natural join owns natural join located_at natural join address where country = '%s' and city = '%s'  group by owns.username having 10*listid > sum;", country, city, country, city);
			ResultSet rs = stmt.executeQuery(reportsql);
			int count = 0;
			while(rs.next()){
				count++;
				String hostname = rs.getString("hostname");
				System.out.printf("Host: %s, ", hostname);
				String listid = rs.getString("listid");
				System.out.printf(", Number of Listing: %s", listid);
				String sum = rs.getString("sum");
				System.out.printf(", Total num of listings in this city: %s", sum);
				System.out.printf(", City: %s", city);
				System.out.printf(", Country: %s\n", country);
			}
			if(count ==0){
				System.out.println("No listing in this city.");
			}

		}catch (SQLException e) {
            e.printStackTrace();
            return;
        }
	}
    public static void report7() throws SQLException{
		try{
			print_header("Output the host and the renter with the largest cancellation:");
			
			String reportsql = String.format("select num, username from (select count(bid) as num, book.username from book where cancellation = 1 group by book.username) as good where num >= ALL (select count(bid) as num from book where cancellation = 1 group by book.username);");
			ResultSet rs = stmt.executeQuery(reportsql);
			int count = 0;
			while(rs.next()){
				count++;
				String username = rs.getString("username");
				System.out.printf("Renter: %s, ", username);
				String num = rs.getString("num");
				System.out.printf(", Number of cancellation: %s\n", num);
			}
            reportsql = String.format("select num, username from (select count(bid) as num, owns.username from book natural join owns where cancellation = 1 group by owns.username) as good where num >= ALL (select count(bid) as num from book natural join owns where cancellation = 1 group by owns.username);");
			rs = stmt.executeQuery(reportsql);
            while(rs.next()){
				count++;
				String username = rs.getString("username");
				System.out.printf("Host: %s, ", username);
				String num = rs.getString("num");
				System.out.printf(", Number of cancellation: %s\n", num);
			}
			if(count ==0){
				System.out.println("No cancellation so far.");
			}

		}catch (SQLException e) {
            e.printStackTrace();
            return;
        }
	}

    public static boolean show_user_owns(String username) throws SQLException {
        boolean result = false;
        try {
            String sql = String.format("SELECT * FROM Owns Natural Join Listing where username = '%s';", username);
            ResultSet rs = stmt.executeQuery(sql);
            // STEP 5: Extract data from result set
            while (rs.next()) {
                result = true;
                // Retrieve by column name
                int lid = rs.getInt("lid");
                System.out.print("Lid: " + lid);
                String lat = rs.getString("lat");
                System.out.print(", Latitude: " + lat);
                String lon = rs.getString("lon");
                System.out.print(", Longitude: " + lon);
                String type = rs.getString("type");
                System.out.print(", Type: " + type);
                String amenities = rs.getString("amenities");
                System.out.println(", Amenities: " + amenities);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    public static String create_listing(String input, String postal_code, String unit, String city, String country, String[] am) throws SQLException {
        String result;
        try {
            String reg = "UserName:(?<username>.*), Lon:(?<lon>.*), Lat:(?<lat>.*), Type:(?<type>.*)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String lat = matcher.group("lat");
                String lon = matcher.group("lon");
                String type = matcher.group("type");
                String username = matcher.group("username");

                StringBuilder amenities = new StringBuilder();
                for (String temp : am) {
                    switch (temp) {
                        case "1" -> amenities.append(",").append("wifi");
                        case "2" -> amenities.append(",").append("kitchen");
                        case "3" -> amenities.append(",").append("washer");
                        case "4" -> amenities.append(",").append("dryer");
                        case "5" -> amenities.append(",").append("ac");
                        case "6" -> amenities.append(",").append("heating");
                        case "7" -> amenities.append(",").append("tv");
                        case "8" -> amenities.append(",").append("hair dryer");
                        case "9" -> amenities.append(",").append("gym");
                    }
                }

                System.out.println(amenities);
                String sql = String.format("INSERT INTO Listing (lon, lat, type, amenities) VALUES ( '%s', '%s', '%s', '%s');", lon, lat, type, amenities.substring(1));
                System.out.println(sql);
                stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                String lid = "";
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next())
                    lid = rs.getString(1);

                // String lid = rs.getString("LAST_INSERT_ID()");
                result = lid;
                sql = String.format("INSERT INTO Owns (username, lid) VALUES ('%s', '%s');", username, lid);
                System.out.println(sql);
                stmt.executeUpdate(sql);
                if (!owner_add_address(postal_code, unit, city, country, lid)) {
                    return "false";
                }
                return result;
            } else {
                System.out.println("Invalid Listing.");
                return "false";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "false";
        }
    }

    public static boolean get_profile(String username) throws SQLException {
        try {
            String sql = String.format("SELECT * FROM user where username = '%s';", username);
            ResultSet rs = stmt.executeQuery(sql);
            // STEP 5: Extract data from result set
            while (rs.next()) {
                int sid = rs.getInt("SIN");
                String name = rs.getString("name");
                String occupation = rs.getString("occupation");
                String birth = rs.getString("birth");
                String password = rs.getString("password");
                String credit_card = rs.getString("payment");
                // Display values
                System.out.print("My Information --- Username: " + username);
                System.out.print(", Password: " + password);
                System.out.print(", Real Name: " + name);
                System.out.print(", SIN: " + sid);
                System.out.print(", birth: " + birth);

                if (occupation != null) {
                    System.out.print(", Occupation: " + occupation);
                }
                if (credit_card != null) {
                    System.out.print(", Credit Card: " + credit_card);
                }
                System.out.println("");
            }
            rs.close();
            String sql2 = String.format("select * from Lives NATURAL JOIN Address where username = '%s';", username);
            ResultSet rs2 = stmt.executeQuery(sql2);
            // System.out.println(sql + "\n" + sql2);
            while (rs2.next()) {
                int unit = rs2.getInt("unit");
                String postal_code = rs2.getString("postal_code");
                String city = rs2.getString("city");
                String country = rs2.getString("country");
                // Display values
                System.out.print("Address (Unit: " + unit);
                System.out.print(", Postal Code: " + postal_code);
                System.out.print(", City: " + city);
                System.out.println(", Country: " + country + ")");
            }
            rs2.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
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
            } else if (int_type == 5) {
                type = "payment";
            } else {
                return false;
            }
            String sql = String.format("update user set %s = '%s' where username = '%s';", type, to_change, username);
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean show_user_books(String username) throws SQLException {
        boolean result = false;
        try {
            String sql = String.format("SELECT distinct cancellation, owns.username as owner_name, book.bid, book.lid, lon, lat, type, start_date, end_date, unit, postal_code, city, country FROM Book Natural Join Listing natural join located_at natural join address inner join owns where book.lid = listing.lid and listing.lid = located_at.lid and owns.lid = located_at.lid and book.username = '%s'", username);
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            // STEP 5: Extract data from result set
            while (rs.next()) {
                // Retrieve by column name
                String bid = rs.getString("BID");
                System.out.print("BID: " + bid);
                String owner_name = rs.getString("owner_name");
                System.out.print(", Owner Name: " + owner_name);
                String lid = rs.getString("lid");
                System.out.print(", ListingID: " + lid);
                String lon = rs.getString("lon");
                System.out.print(", Longitude: " + lon);
                String lat = rs.getString("lat");
                System.out.print(", Latitude: " + lat);
                String type = rs.getString("type");
                System.out.print(", Type: " + type);
                String unit = rs.getString("unit");
                System.out.print(", unit: " + unit);
                String postal_code = rs.getString("postal_code");
                System.out.print(", postal_code: " + postal_code);
                String city = rs.getString("city");
                System.out.print(", City: " + city);
                String country = rs.getString("country");
                System.out.print(", Country: " + country);
                String start_date = rs.getString("start_date");
                System.out.print(", start date: " + start_date);
                String end_date = rs.getString("end_date");
                System.out.print(", end date: " + end_date);
                String cancellation = rs.getString("cancellation");
                if (cancellation.equals("0")) {
                    System.out.println(", Not cancelled.");
                } else if (cancellation.equals("1")) {
                    System.out.println(", Cancelled by Renter.");
                } else if (cancellation.equals("2")) {
                    System.out.println(", Cancelled by Owner.");
                }
                result = true;
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean show_renter_booked_listing(String rentername) throws SQLException {
        boolean result = false;
        try {
            String sql = String.format("SELECT lid, lon, lat, type, unit, postal_code, city, country FROM Book Natural Join Listing natural join located_at natural join address where book.username = '%s' and cancellation = 0 group by lid;", rentername);
            // System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            // STEP 5: Extract data from result set
            while (rs.next()) {
                String lid = rs.getString("lid");
                System.out.print("ListingID: " + lid);
                String lon = rs.getString("lon");
                System.out.print(", Longitude: " + lon);
                String lat = rs.getString("lat");
                System.out.print(", Latitude: " + lat);
                String type = rs.getString("type");
                System.out.print(", Type: " + type);
                String unit = rs.getString("unit");
                System.out.print(", unit: " + unit);
                String postal_code = rs.getString("postal_code");
                System.out.print(", postal_code: " + postal_code);
                String city = rs.getString("city");
                System.out.print(", City: " + city);
                String country = rs.getString("country");
                System.out.println(", Country: " + country);

                result = true;
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean create_renter_listing_comment(String rentername, String lid, String rate, String text) throws SQLException {
        try {
            String sql = String.format("SELECT lid, lon, lat, type, unit, postal_code, city, country FROM Book Natural Join Listing natural join located_at natural join address where book.username = '%s' and cancellation = 0 and lid = '%s' group by lid;", rentername, lid);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String sql3 = String.format("SELECT * FROM Comment where username = '%s' and lid = '%s';", rentername, lid);
                ResultSet rs3 = stmt.executeQuery(sql3);
                if (rs3.next()) {
                    System.out.println("Since you have already comment this listing, we delete the previous one and create new comment.");
                    String CID = rs3.getString("CID");
                    String sql4 = String.format("DELETE FROM Comment where CID = '%s';", CID);
                    stmt.executeUpdate(sql4);
                }
                String sql2 = String.format("INSERT INTO Comment (text, lid, username, rate) VALUES ('%s', '%s', '%s', '%s');", text, lid, rentername, rate);
                print_header("Comment added.");
                stmt.executeUpdate(sql2);
                return true;

            }
            print_header("You can't comment this lising since you haven't book this listing or cancelled.");
            return false;

        } catch (SQLException e) {
            System.err.println("You can't comment this lising since you haven't book this listing or cancelled.");
            return false;
        }
    }

    public static boolean delete_renter_listing_comment(String rentername, String lid) throws SQLException {
        try {
            String sql = String.format("SELECT * FROM Comment where username = '%s' and lid = '%s' ", rentername, lid);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String CID = rs.getString("CID");
                String sql4 = String.format("DELETE FROM Comment where CID = '%s';", CID);
                stmt.executeUpdate(sql4);
                print_header("Comment deleted success.");
                return true;
            }
            print_header("You can't delete you comment of this lising since you haven't comment this listing.");
            return false;

        } catch (SQLException e) {
            System.err.println("You can't delete you comment of this lising since you haven't comment this listing.");
            return false;
        }
    }

    public static boolean show_owner_books(String username) throws SQLException {
        boolean result = false;
        try {
            String sql = String.format("select distinct cancellation, book.username as renter_name, book.bid as bid, start_date, end_date, payment, book.price, book.lid as lid, located_at.unit as unit, located_at.postal_code as postal_code from book inner join located_at inner join owns where book.lid = located_at.lid and located_at.lid = owns.lid and owns.username = '%s'", username);
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            // STEP 5: Extract data from result set
            while (rs.next()) {
                String bid = rs.getString("bid");
                System.out.print("BID: " + bid);
                String renter_name = rs.getString("renter_name");
                System.out.print(", Renter Name: " + renter_name);
                String lid = rs.getString("lid");
                System.out.print(", ListingID: " + lid);
                String unit = rs.getString("unit");
                System.out.print(", unit: " + unit);
                String postal_code = rs.getString("postal_code");
                System.out.print(", postal_code: " + postal_code);
                String start_date = rs.getString("start_date");
                System.out.print(", start date: " + start_date);
                String end_date = rs.getString("end_date");
                System.out.print(", end date: " + end_date);
                String cancellation = rs.getString("cancellation");
                if (cancellation.equals("0")) {
                    System.out.println(", Not cancelled.");
                } else if (cancellation.equals("1")) {
                    System.out.println(", Cancelled by Renter.");
                } else if (cancellation.equals("2")) {
                    System.out.println(", Cancelled by Owner.");
                }
                result = true;
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean show_available(String lid) throws SQLException {
        try {
            boolean result = false;
            // use lid to show all avaiable

            // check if the time is available
            String check_avail = String.format("select * from available where lid = '%s';", lid);
            ResultSet rs = stmt.executeQuery(check_avail);
            while (rs.next()) {
                result = true;
                String date = rs.getString("date");
                String price = rs.getString("price");
                System.out.print("Date: " + date);
                System.out.println(", Price: " + price);
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // renter use
    public static boolean get_available(LocalDate start_time, LocalDate end_time) throws SQLException {
        try {
            boolean result = false;
            long date_between = ChronoUnit.DAYS.between(start_time, end_time) + 1;
            // System.out.println(date_between);
            // get all listing that satisfy start time and end time
            // find all that not qualified
            String count_query = String.format("select lid, count(date) as count from available where date >= '%s' and date <= '%s' group by lid;", start_time, end_time);
            ResultSet rs = stmt.executeQuery(count_query);
            while (rs.next()) {
                long count = rs.getInt("count");
                // System.out.println(count);
                String lid = rs.getString("lid");
                if (count == date_between) {
                    // this one is qualified
                    System.out.println("lid: " + lid);
                    result = true;
                }
            }
            return result;
        } catch (SQLException e) {
            print_error("Cannot get available");
            e.printStackTrace();
            return false;
        }
    }

    // renter use
    public static boolean find_lid_by_postal_code(String postal_code, LocalDate start_time, LocalDate end_time) throws SQLException {
        try {
            boolean result = false;
            long date_between = ChronoUnit.DAYS.between(start_time, end_time) + 1;
            // System.out.println(date_between);
            // get all listing that satisfy start time and end time
            // find all that not qualified
            String count_query = String.format("select lid, count(date) as count, postal_code, unit from available natural join located_at where LEFT(postal_code,3) = LEFT('%s',3) and date >= '%s' and date <= '%s' group by lid;", postal_code, start_time, end_time);
            ResultSet rs = stmt.executeQuery(count_query);
            while (rs.next()) {
                long count = rs.getInt("count");
                // System.out.println(count);
                String lid = rs.getString("lid");
                if (count == date_between) {
                    // this one is qualified
                    System.out.println("lid: " + lid);
                    result = true;
                }
            }
            return result;
        } catch (SQLException e) {
            print_error("Cannot get available");
            e.printStackTrace();
            return false;
        }
    }

    // renter use
    public static boolean find_lid_by_am(LocalDate start_time, LocalDate end_time, String[] am) throws SQLException {
        try {
            boolean result = false;
            String temp_am = "";
            long date_between = ChronoUnit.DAYS.between(start_time, end_time) + 1;
            StringBuilder count_query = new StringBuilder(String.format("select lid, count(date) as count, type from available natural join listing where date >= '%s' and date <= '%s'", start_time, end_time));
            for (String temp : am) {
                switch (temp) {
                    case "1" -> temp_am = "wifi";
                    case "2" -> temp_am = "kitchen";
                    case "3" -> temp_am = "washer";
                    case "4" -> temp_am = "dryer";
                    case "5" -> temp_am = "ac";
                    case "6" -> temp_am = "heating";
                    case "7" -> temp_am = "tv";
                    case "8" -> temp_am = "hair dryer";
                    case "9" -> temp_am = "gym";
                }
                count_query.append(" and FIND_IN_SET('").append(temp_am).append("', amenities)");
            }
            count_query.append(" group by lid;");
            System.out.println(count_query);
            ResultSet rs = stmt.executeQuery(count_query.toString());
            while (rs.next()) {
                long count = rs.getInt("count");
                // System.out.println(count);
                String lid = rs.getString("lid");
                if (count == date_between) {
                    // this one is qualified
                    System.out.println("lid: " + lid);
                    result = true;
                }
            }
            return result;
        } catch (SQLException e) {
            print_error("Cannot get available");
            e.printStackTrace();
            return false;
        }
    }

    // renter use
    public static boolean find_lid_by_location(String lat, String lon, LocalDate start_time, LocalDate end_time, String distance) throws SQLException {
        try {
            double lat_double = Double.parseDouble(lat);
            double lon_double = Double.parseDouble(lon);
            double distance_double = Double.parseDouble(distance);
            double lat_min = lat_double - distance_double;
            double lat_max = lat_double + distance_double;
            double lon_min = lon_double - distance_double;
            double lon_max = lon_double + distance_double;
            boolean result = false;
            long date_between = ChronoUnit.DAYS.between(start_time, end_time) + 1;
            String count_query = String.format("select lid, count(date) as count, type from available natural join listing where lat >= '%s' and lat <= '%s' and lon >= '%s' and lon <= '%s' and date >= '%s' and date <= '%s' group by lid;", lat_min, lat_max, lon_min, lon_max, start_time, end_time);
            System.out.println(count_query);
            ResultSet rs = stmt.executeQuery(count_query);
            while (rs.next()) {
                long count = rs.getInt("count");
                // System.out.println(count);
                String lid = rs.getString("lid");
                if (count == date_between) {
                    // this one is qualified
                    System.out.println("lid: " + lid);
                    result = true;
                }
            }
            return result;
        } catch (SQLException e) {
            print_error("Cannot get available");
            e.printStackTrace();
            return false;
        }
    }

    public static String search_lid(LocalDate start_time, LocalDate end_time, String postal_code, String unit) throws SQLException {
        String result = "";
        try {
            long date_between = ChronoUnit.DAYS.between(start_time, end_time) + 1;
            String count_query = String.format("select available.lid, count(date) as count from available natural join located_at where unit = '%s' and postal_code = '%s' and date >= '%s' and date <= '%s' group by lid;", unit, postal_code, start_time, end_time);
            ResultSet rs = stmt.executeQuery(count_query);
            while (rs.next()) {
                long count = rs.getInt("count");
                // System.out.println(count);
                String lid = rs.getString("lid");
                if (count == date_between) {
                    System.out.println("result of search_lid: " + lid);
                    result = lid;
                }
            }
            return result;
        } catch (SQLException e) {
            print_error("Cannot get available");
            e.printStackTrace();
            return result;
        }
    }

    public static int get_price_book(String lid, LocalDate start_time, LocalDate end_time) throws SQLException {
        try {
            System.out.println("Price of the Listing: ");
            LocalDate temp = start_time;
            String sql;
            while (temp.isBefore(end_time)) {
                sql = String.format("select * from available where date = '%s' and lid = '%s';", temp, lid);
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    System.out.println("Price of " + temp + " is " + rs.getString("price") + ", ");
                    ;
                }
                temp = temp.plusDays(1);
            }

            int result = 0;
            String query = String.format("select sum(price) as price from available where date >= '%s' and date <= '%s' and lid = '%s';", start_time, end_time, lid);
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                result = rs.getInt("price");
                System.out.println("Total cost is: " + result + ".");
            }
            return result;
        } catch (SQLException e) {
            print_error("Cannot get available");
            e.printStackTrace();
            return 0;
        }
    }

    public static boolean check_lid_in_book(String lid) throws SQLException {
        try {
            // check if the lid is in book
            String sql = String.format("select * from book where lid = '%s' and cancellation = '0' and end_date >= NOW();", lid);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                // found one
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error in check lid.");
            return false;
        }

    }


    public static boolean add_available(LocalDate start_time, LocalDate end_time, String price, String lid, boolean hard) throws SQLException {
        try {
            // start time, end time, price, lid is valid
            LocalDate temp = start_time;
            String sql;
            if (hard) {
                String check_avail = String.format("select * from available where lid = '%s' and date >= '%s' and date <= '%s' and BID is not NULL;", lid, start_time, end_time);
                ResultSet rs = stmt.executeQuery(check_avail);
                if (rs.next()) {
                    print_error("The period cannot be change.");
                    return false;
                }

                // delete the corresponding date first
                while (temp.isBefore(end_time)) {
                    sql = String.format("delete from Available where lid = '%s' and date = '%s';", lid, temp);
                    System.out.println(sql);
                    stmt.executeUpdate(sql);
                    temp = temp.plusDays(1);
                }
            } else {
                // check if the time is available
                String check_avail = String.format("select * from available where lid = '%s' and date >= '%s' and date <= '%s';", lid, start_time, end_time);
                ResultSet rs = stmt.executeQuery(check_avail);
                if (rs.next()) {
                    print_error("Input time period is duplicate");
                    return false;
                }
            }
            temp = start_time;
            while (temp.isBefore(end_time)) {
                sql = String.format("INSERT INTO Available (price, date, lid) VALUES ('%s', '%s', '%s');", price, temp, lid);
                System.out.println(sql);
                stmt.executeUpdate(sql);
                temp = temp.plusDays(1);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean remove_available(LocalDate start_time, LocalDate end_time, String lid) throws SQLException {
        try {
            LocalDate temp = start_time;
            String sql;
            // validate the available to avoid delete existing bid
            sql = String.format("select * from available where lid = '%s' and date >= '%s' and date <= '%s' and bid is not NULL", lid, start_time, end_time);
            // sql is the date where bid is not null
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                // exist
                print_error("Cannot change available when there is a book in the period.");
                return false;
            }
            while (temp.isBefore(end_time)) {
                sql = String.format("delete from Available where lid = '%s' and date = '%s';", lid, temp);
                System.out.println(sql);
                stmt.executeUpdate(sql);
                temp = temp.plusDays(1);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean create_book(String payment, int price, String start_date, String end_date, String lid) throws SQLException {
        try {
            String sql = String.format("INSERT INTO Book (start_date, end_date, price, payment, username, lid) VALUES ('%s','%s','%d','%s','%s', '%s');", start_date, end_date, price, payment, username, lid);
            System.out.println(sql);
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            String bid = "";
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                bid = rs.getString(1);
            }

            sql = String.format("update Available set bid = '%s' where date >= '%s' and date <= '%s' and lid = '%s';", bid, start_date, end_date, lid);
            System.out.println(sql);
            stmt.executeUpdate(sql);
            return true;

        } catch (SQLException e) {
            System.err.println("Something went wrong with creating Book");
            return false;
        }
    }

    public static boolean cancel_book(String bid, int is_owner) throws SQLException {
        try {
            String sql = String.format("Update book set cancellation = '%d' where bid = '%s';", is_owner, bid);
            System.out.println(sql);
            stmt.executeUpdate(sql);
            // update the available
            String avail_sql = String.format("Update available set bid = NULL where bid = '%s'", bid);
            System.out.println(avail_sql);
            stmt.executeUpdate(avail_sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Something went wrong with creating Book");
            return false;
        }
    }

    public static boolean handle_modify_listing(String lid, int int_type, String to_change) throws SQLException {
        try {
            String type;
            if (int_type == 1) {
                type = "lat";
            } else if (int_type == 2) {
                type = "lon";
            } else if (int_type == 3) {
                type = "type";
            } else {
                return false;
            }
            String sql = String.format("update listing set %s = '%s' where lid = '%s';", type, to_change, lid);
            System.out.println(sql);
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Cannot modify listing");
            return false;
        }
    }

    public static boolean handle_modify_am(String lid, String[] am) throws SQLException {
        try {
            StringBuilder amenities = new StringBuilder();
            for (String temp : am) {
                switch (temp) {
                    case "1" -> amenities.append(",").append("wifi");
                    case "2" -> amenities.append(",").append("kitchen");
                    case "3" -> amenities.append(",").append("washer");
                    case "4" -> amenities.append(",").append("dryer");
                    case "5" -> amenities.append(",").append("ac");
                    case "6" -> amenities.append(",").append("heating");
                    case "7" -> amenities.append(",").append("tv");
                    case "8" -> amenities.append(",").append("hair dryer");
                    case "9" -> amenities.append(",").append("gym");
                }
            }
            System.out.println(amenities);
            String sql = String.format("update listing set amenities = '%s' where lid = '%s';", amenities.substring(1), lid);
            System.out.println(sql);
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Cannot modify listing");
            return false;
        }
    }

    public static boolean change_address(String username, String postal_code, String unit, String city, String country) throws SQLException {
        try {
            // first, check if the address exists
            // if not exist, add it
            String check_address = String.format("select * from address where postal_code = '%s' and unit = '%s';", postal_code, unit);
            ResultSet rs_check = stmt.executeQuery(check_address);
            if (!rs_check.next()) {
                // not exist, add it
                String sql = String.format("INSERT INTO Address (postal_code, unit, city, country) VALUES ('%s','%s','%s','%s');", postal_code, unit, city, country);
                stmt.executeUpdate(sql);
            }
            rs_check.close();
            // the address exists, check if there's old connection
            String get_address = String.format("select * from Lives where username = '%s';", username);
            ResultSet rs = stmt.executeQuery(get_address);
            if (rs.next()) {
                // exist old connection, delete the connection
                int old_unit = rs.getInt("unit");
                String old_postal_code = rs.getString("postal_code");
                // update address
                String sql = String.format("delete from Lives where postal_code = '%s' and unit = '%d' and username = '%s';", old_postal_code, old_unit, username);
                stmt.executeUpdate(sql);

                // if the address in not in live nor in located at, delete it
                String check_live = String.format("select * from Lives where postal_code = '%s' and unit = '%d';", old_postal_code, old_unit);
                String check_located = String.format("select * from Located_at where postal_code = '%s' and unit = '%d';", old_postal_code, old_unit);
                ResultSet rs_temp1 = stmt.executeQuery(check_live);
                System.out.println(check_live);
                if (!rs_temp1.next()) {
                    ResultSet rs_temp2 = stmt.executeQuery(check_located);
                    System.out.println(check_located);
                    if ((!rs_temp2.next())) {
                        // no live and located
                        // delete it
                        sql = String.format("delete from address where postal_code = '%s' and unit = '%d';", old_postal_code, old_unit);
                        System.out.println(sql);
                        stmt.executeUpdate(sql);
                    }
                }

            }
            // no connection btw user and address
            // add new relation
            String sql2 = String.format("INSERT INTO Lives (postal_code, unit, username) VALUES ('%s','%s','%s');", postal_code, unit, username);
            System.out.println(sql2);
            stmt.executeUpdate(sql2);
            return true;

        } catch (SQLException e) {
            System.err.println("Cannot change my profile");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean change_address_listing(String lid, String postal_code, String unit, String city, String country) throws SQLException {
        try {
            String check_address = String.format("select * from address where postal_code = '%s' and unit = '%s';", postal_code, unit);
            ResultSet rs_check = stmt.executeQuery(check_address);
            // if not in the database, add it
            if (!rs_check.next()) {
                // not exist, add it
                String sql = String.format("INSERT INTO Address (postal_code, unit, city, country) VALUES ('%s','%s','%s','%s');", postal_code, unit, city, country);
                stmt.executeUpdate(sql);
            }
            rs_check.close();
            // delete old address from located_at and re add it
            String get_address = String.format("select * from Located_At where lid = '%s';", lid);
            ResultSet rs = stmt.executeQuery(get_address);
            if (rs.next()) {
                int old_unit = rs.getInt("unit");
                String old_postal_code = rs.getString("postal_code");
                String sql = String.format("delete from Located_At where postal_code = '%s' and unit = '%d' and lid = '%s';", old_postal_code, old_unit, lid);
                stmt.executeUpdate(sql);
                // if the address in not in live nor in located at, delete it
                String check_live = String.format("select * from Lives where postal_code = '%s' and unit = '%d';", old_postal_code, old_unit);
                String check_located = String.format("select * from Located_at where postal_code = '%s' and unit = '%d';", old_postal_code, old_unit);
                ResultSet rs_temp1 = stmt.executeQuery(check_live);
                System.out.println(check_live);
                if (!rs_temp1.next()) {
                    ResultSet rs_temp2 = stmt.executeQuery(check_located);
                    System.out.println(check_located);
                    if ((!rs_temp2.next())) {
                        // no live and located
                        // delete it
                        sql = String.format("delete from address where postal_code = '%s' and unit = '%d';", old_postal_code, old_unit);
                        System.out.println(sql);
                        stmt.executeUpdate(sql);
                    }
                }
            }
            // delete done, need to re add
            String sql2 = String.format("INSERT INTO Located_At (postal_code, unit, lid) VALUES ('%s','%s','%s');", postal_code, unit, lid);
            System.out.println(sql2);
            stmt.executeUpdate(sql2);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean owner_add_address(String postal_code, String unit, String city, String country, String lid) throws SQLException {
        try {
            String sql = String.format("INSERT INTO Located_At (postal_code, unit, lid) VALUES ('%s','%s','%s');", postal_code, unit, lid);
            stmt.executeUpdate(sql);
            System.out.println(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean check_address_owner(String postal_code, String unit, String city, String country) throws SQLException {
        try {
            // if the address is in located_at, return false;
            String check = String.format("select * from located_at where postal_code = '%s' and unit = '%s';", postal_code, unit);
            ResultSet rs = stmt.executeQuery(check);
            if (rs.next()) {
                // fault, already exists
                print_error("Address is duplicated, please try another");
                return false;
            }
            // not exist
            String check_2 = String.format("select * from address where postal_code = '%s' and unit = '%s';", postal_code, unit);
            System.out.println(check_2);
            ResultSet rs2 = stmt.executeQuery(check_2);
            if (!rs2.next()) {
                String sql = String.format("INSERT INTO Address (postal_code, unit, city, country) VALUES ('%s','%s','%s','%s');", postal_code, unit, city, country);
                System.out.println(sql);
                stmt.executeUpdate(sql);
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean delete_user(String username) throws SQLException {
        try {
            String sql = String.format("DELETE FROM User where username = \"%s\";", username);
            System.out.print(sql.concat("\n"));
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean cancel_judgement_host(String jid, String hostname) throws SQLException {
        try {

            String sql = String.format("SELECT * FROM Judgement where host_username = '%s' and direction = 1;", hostname);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                if (JID.equals(jid)) {
                    String sql2 = String.format("DELETE FROM Judgement where JID = \"%s\";", jid);
                    System.out.print(sql.concat("\n"));
                    stmt.executeUpdate(sql2);
                    return true;
                }
            }
            print_header("Invalid JID: please select from what you have rated");
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean cancel_judgement_renter(String jid, String rentername) throws SQLException {
        try {

            String sql = String.format("SELECT * FROM Judgement where renter_username = '%s' and direction = 0;", rentername);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                if (JID.equals(jid)) {
                    String sql2 = String.format("DELETE FROM Judgement where JID = \"%s\";", jid);
                    System.out.print(sql.concat("\n"));
                    stmt.executeUpdate(sql2);
                    return true;
                }
            }
            print_header("Invalid JID: please select from what you have rated");
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String get_payment() throws SQLException {
        String result = null;
        try {
            String sql = String.format("select * from User where username = '%s';", username);
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                result = rs.getString("payment");
            }
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean handle_delete_listing(String username, String lid) throws SQLException {
        try {
            // first, check if the address exists
            // if not exist, add it
            String check_address = String.format("select * from located_at where lid = '%s';", lid);
            System.out.println(check_address);
            ResultSet rs_check = stmt.executeQuery(check_address);
            if (rs_check.next()) {
                // get postal_code and unit
                String postal_code = rs_check.getString("postal_code");
                String unit = rs_check.getString("unit");

                String sql = String.format("delete from listing where lid = '%s';", lid);
                System.out.println(sql);
                stmt.executeUpdate(sql);

                String check_live = String.format("select * from Lives where postal_code = '%s' and unit = '%s';", postal_code, unit);
                System.out.println(check_live);
                ResultSet rs_live = stmt.executeQuery(check_live);
                if (!rs_live.next()) {
                    // address is useless
                    // delete the address
                    sql = String.format("delete from address where postal_code = '%s' and unit = '%s';", postal_code, unit);
                    System.out.println(sql);
                    stmt.executeUpdate(sql);
                }
                return true;
            } else {
                print_error("cannot find the listing");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Cannot delete listing");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean create_renter_rate(String rentername, String hostname, String rate, String text) throws SQLException {
        try {

            String sql = String.format("SELECT Owns.username FROM Book Join Owns where Book.lid = Owns.lid and cancellation = 0 and Book.username = '%s';", rentername);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String host = rs.getString("username");
                if (host.equals(hostname)) {
                    String sql3 = String.format("SELECT * FROM Judgement where renter_username = '%s' and host_username = '%s' and direction = 0;", rentername, hostname);
                    ResultSet rs3 = stmt.executeQuery(sql3);
                    if (rs3.next()) {
                        System.out.println("Since you have already rated this host, we delete the previous one and create new rating.");
                        String JID = rs3.getString("JID");
                        String sql4 = String.format("DELETE FROM Judgement where JID = '%s';", JID);
                        stmt.executeUpdate(sql4);
                    }
                    String sql2 = String.format("INSERT INTO Judgement (words, host_username, renter_username, direction, likes) VALUES ('%s', '%s', '%s', '%s', '%s');", text, hostname, rentername, "0", rate);
                    System.out.print(sql2.concat("Rating added."));
                    stmt.executeUpdate(sql2);
                    return true;
                }

            }
            print_header("You can't judge this host since you haven't book any from this host.");
            return false;

        } catch (SQLException e) {
            System.err.println("You can't judge this host since you haven't book any from this host.");
            return false;
        }
    }

    public static boolean create_host_rate(String rentername, String hostname, String rate, String text) throws SQLException {
        try {

            String sql = String.format("SELECT Book.username FROM Book Join Owns where Book.lid = Owns.lid and cancellation = 0 and Owns.username = '%s';", hostname);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String renter = rs.getString("username");
                if (renter.equals(rentername)) {
                    String sql3 = String.format("SELECT * FROM Judgement where renter_username = '%s' and host_username = '%s' and direction = 1;", rentername, hostname);
                    ResultSet rs3 = stmt.executeQuery(sql3);
                    if (rs3.next()) {
                        System.out.println("Since you have already rated this renter, we delete the previous one and create new rating.");
                        String JID = rs3.getString("JID");
                        String sql4 = String.format("DELETE FROM Judgement where JID = '%s';", JID);
                        stmt.executeUpdate(sql4);
                    }
                    String sql2 = String.format("INSERT INTO Judgement (words, host_username, renter_username, direction, likes) VALUES ('%s', '%s', '%s', '%s', '%s');", text, hostname, rentername, "1", rate);
                    print_header("Rating added.");
                    stmt.executeUpdate(sql2);
                    return true;
                }

            }
            print_header("You can't judge this renter since he/she hasn't book any from you.");
            return false;

        } catch (SQLException e) {
            System.err.println("You can't judge this renter since he/she hasn't book any from you.");
            return false;
        }
    }

    /*show how the renter is being judged*/
    public static boolean show_renter_s_judgement(String rentername) throws SQLException {
        try {
            boolean result = false;
            String sql = String.format("SELECT * FROM Judgement where renter_username = '%s';", rentername);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                String likes = rs.getString("likes");
                String words = rs.getString("words");
                String direction = rs.getString("direction");
                String host_username = rs.getString("host_username");

                if (direction.equals("1")) {
                    String sql2 = String.format("JID = '%s', Host: '%s', Likes = '%s', Comment: '%s';", JID, host_username, likes, words);
                    System.out.print(sql2.concat("\n"));
                    // stmt.executeUpdate(sql2);
                    result = true;
                }
            }
            return result;

        } catch (SQLException e) {
            System.err.println("Something went wrong with judgement showing.");
            return false;
        }
    }

    /*show how the host is being judged*/
    public static boolean show_host_s_judgement(String hostname) throws SQLException {
        try {
            boolean result = false;
            String sql = String.format("SELECT * FROM Judgement where host_username = '%s';", hostname);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                String likes = rs.getString("likes");
                String words = rs.getString("words");
                String direction = rs.getString("direction");
                String renter_username = rs.getString("renter_username");

                if (direction.equals("0")) {
                    String sql2 = String.format("JID = '%s', Renter: '%s', Likes = '%s', Comment: '%s';", JID, renter_username, likes, words);
                    System.out.print(sql2.concat("\n"));
                    // stmt.executeUpdate(sql2);
                    result = true;
                }
            }
            return result;

        } catch (SQLException e) {
            System.err.println("Something went wrong with judgement showing.");
            return false;
        }
    }

    /*Show what renter have rated*/
    public static boolean show_renter_s_rate(String rentername) throws SQLException {
        try {
            boolean result = false;
            String sql = String.format("SELECT * FROM Judgement where renter_username = '%s';", rentername);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                String likes = rs.getString("likes");
                String words = rs.getString("words");
                String direction = rs.getString("direction");
                String host_username = rs.getString("host_username");

                if (direction.equals("0")) {
                    String sql2 = String.format("JID = '%s', Host: '%s', Likes = '%s', Comment: '%s';", JID, host_username, likes, words);
                    System.out.print(sql2.concat("\n"));
                    // stmt.executeUpdate(sql2);
                    result = true;
                }
            }
            return result;

        } catch (SQLException e) {
            System.err.println("Something went wrong with judgement showing.");
            return false;
        }
    }

    /*Show what host have rated*/
    public static boolean show_host_s_rate(String hostname) throws SQLException {
        try {
            boolean result = false;
            String sql = String.format("SELECT * FROM Judgement where host_username = '%s';", hostname);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                String likes = rs.getString("likes");
                String words = rs.getString("words");
                String direction = rs.getString("direction");
                String renter_username = rs.getString("renter_username");

                if (direction.equals("1")) {
                    String sql2 = String.format("JID = '%s', Renter: '%s', Likes = '%s', Comment: '%s';", JID, renter_username, likes, words);
                    System.out.print(sql2.concat("\n"));
                    // stmt.executeUpdate(sql2);
                    result = true;
                }
            }
            return result;

        } catch (SQLException e) {
            System.err.println("Something went wrong with judgement showing.");
            return false;
        }
    }

    public static void recommend_amenities() throws SQLException {
        try {
            int[] count_array = new int[9];
            String sql;
            ResultSet rs;

            sql = "select count(*) as count from listing where FIND_IN_SET('wifi', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[0] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('kitchen', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[1] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('washer', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[2] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('dryer', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[3] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('ac', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[4] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('heating', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[5] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('tv', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[6] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('hair dryer', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[7] = rs.getInt("count");
            }
            sql = "select count(*) as count from listing where FIND_IN_SET('gym', amenities);";
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count_array[8] = rs.getInt("count");
            }

            // get the largest index
            int largest = 0;
            for ( int i = 0; i < 9; i++ ) {
                if(count_array[i] > count_array[largest]) {
                    largest = i;
                }
            }
            count_array[largest] = 0;
            int s_largest = 0;
            for ( int i = 0; i < 9; i++ ) {
                if(count_array[i] > count_array[s_largest]) {
                    s_largest = i;
                }
            }
            count_array[s_largest] = 0;
            int t_largest = 0;
            for ( int i = 0; i < 9; i++ ) {
                if(count_array[i] > count_array[t_largest]) {
                    t_largest = i;
                }
            }

            System.out.print("Recommend Listing Amenities: ");
            switch (largest + 1) {
                case 1 -> System.out.print("wifi");
                case 2 -> System.out.print("kitchen");
                case 3 -> System.out.print("washer");
                case 4 -> System.out.print("dryer");
                case 5 -> System.out.print("ac");
                case 6 -> System.out.print("heating");
                case 7 -> System.out.print("tv");
                case 8 -> System.out.print("hair dryer");
                case 9 -> System.out.print("gym");
            }
            switch (s_largest + 1) {
                case 1 -> System.out.print(", wifi");
                case 2 -> System.out.print(", kitchen");
                case 3 -> System.out.print(", washer");
                case 4 -> System.out.print(", dryer");
                case 5 -> System.out.print(", ac");
                case 6 -> System.out.print(", heating");
                case 7 -> System.out.print(", tv");
                case 8 -> System.out.print(", hair dryer");
                case 9 -> System.out.print(", gym");
            }
            switch (t_largest + 1) {
                case 1 -> System.out.println(" and wifi");
                case 2 -> System.out.println(" and kitchen");
                case 3 -> System.out.println(" and washer");
                case 4 -> System.out.println(" and dryer");
                case 5 -> System.out.println(" and ac");
                case 6 -> System.out.println(" and heating");
                case 7 -> System.out.println(" and tv");
                case 8 -> System.out.println(" and hair dryer");
                case 9 -> System.out.println(" and gym");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void recommend_price(String lid) throws SQLException {
        try {
            int type_price_avg = 0;
            int all_price_avg = 0;
            String sql = "select avg(price) as avg from available;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                all_price_avg = rs.getInt("avg");
            }

            // for your type
            String type = "";
            sql = String.format("select * from listing where lid = '%s';", lid);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                type = rs.getString("type");
            }

            sql = String.format("select avg(price) as avg from listing natural join available where lid != '%s' and type = '%s';", lid, type);
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                type_price_avg = rs.getInt("avg");
            }

            if(type_price_avg == 0){
                System.out.println("No Recommend Listing price range for your listing type since no enough host sample.");
            }else{
                int type_price_low =  (int) (type_price_avg * 0.9);
                int type_price_high = (int) (type_price_avg * 1.1);
                System.out.println("Recommend Listing price range for your listing type: " + type_price_low + " to " + type_price_high + ",");
            }

            int all_price_low =  (int) (all_price_avg * 0.9);
            int all_price_high = (int) (all_price_avg * 1.1);
            System.out.println("Recommend Listing price range for new listing: " + all_price_low + " to " + all_price_high + ".");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean show_listing_comment(String lid, String username) throws SQLException {
        boolean result = false;
        try {
            String sql = String.format("SELECT * FROM Owns Natural Join Listing where username = '%s' and Listing.lid = '%s';", username, lid);
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            // STEP 5: Extract data from result set
            if (rs.next()) {
                result = true;

                // Retrieve by column name
                String sqll = String.format("SELECT * FROM Comment Natural Join Listing where Listing.lid = '%s';", lid);
                ResultSet rss = stmt.executeQuery(sqll);
                while (rss.next()) {
                    count++;
                    // System.out.print("Lid: " + lid);
                    String renter = rss.getString("username");
                    System.out.print("Renter: " + renter);
                    String rate = rss.getString("rate");
                    System.out.print(", Rate: " + rate);
                    String text = rss.getString("text");
                    System.out.println(", Comment: " + text);
                }

                rss.close();
                if (count == 0) {
                    System.out.println("No one comment yet.");
                }
                System.out.println(a_line);
            }

            rs.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    /*show how the host is being judged*/
    public static boolean show_owner_s_judgement(String host_username) throws SQLException {
        try {
            boolean result = false;
            String sql = String.format("SELECT * FROM Judgement where host_username = '%s';", host_username);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String JID = rs.getString("JID");
                String likes = rs.getString("likes");
                String words = rs.getString("words");
                String direction = rs.getString("direction");
                String renter_username = rs.getString("renter_username");

                if (direction.equals("0")) {
                    String sql2 = String.format("JID = '%s', Renter: '%s', Likes = '%s', Comment: '%s';", JID, renter_username, likes, words);
                    System.out.print(sql2.concat("\n"));
                    // stmt.executeUpdate(sql2);
                    result = true;
                }
            }
            return result;

        } catch (SQLException e) {
            System.err.println("Something went wrong with judgement showing.");
            return false;
        }
    }

    /*show how the renter is being judged*/
    public static boolean show_renter_comment(String rentername) throws SQLException {
        try {
            boolean result = false;
            String sql = String.format("SELECT * FROM Comment where username = '%s';", rentername);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String CID = rs.getString("CID");
                String rate = rs.getString("rate");
                String lid = rs.getString("lid");
                String text = rs.getString("text");
                String sql2 = String.format("LID = '%s', cid: '%s' Rate: '%s', Comment = '%s';", lid, CID, rate, text);
                System.out.print(sql2.concat("\n"));
                // stmt.executeUpdate(sql2);
                result = true;

            }
            return result;

        } catch (SQLException e) {
            System.err.println("Something went wrong with comment showing.");
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

    public static String validate_time(Scanner sc) {
        String input;
        String regex = "^\\d{4}\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$";
        Pattern pattern = Pattern.compile(regex);
        while (true) {
            input = sc.nextLine();

            Matcher matcher = pattern.matcher(input);
            if (!matcher.matches()) {
                print_error("Input date is not valid!");
            } else {
                try {
                    LocalDate.parse(input);
                    break;
                } catch (Exception e) {
                    print_error("Input date is not valid!");
                }
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

    public static String[] validate_am(Scanner sc) {
        String[] result;
        String temp;
        label_am_list:
        while (true) {
            temp = sc.nextLine();
            if (!temp.toLowerCase().equals(temp.toUpperCase())) {
                print_error("Input is not valid.");
                continue;
            }
            result = temp.split(" ");
            for (String one_string : result) {
                try {
                    int temp_int = Integer.parseInt(one_string);
                    if (temp_int > 9 || temp_int < 1) {
                        print_error("Input is not valid");
                        continue label_am_list;
                    }
                } catch (Exception e) {
                    print_error("Input is not valid");
                    continue label_am_list;
                }
            }
            return result;
        }
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
        try {
            System.out.println(half_line + "Update Information" + half_line);
            System.out.println("1: Check My Profile, 2: Change My Profile, 3: Delete My Account, 4: Go Back.");
            while (sc.hasNextLine()) {
                String profile_decide = validate_int(sc, 1, 4);
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
                    System.out.println("1: Real Name, 2: Birth Year, 3: Password, 4: Occupation, 5:Payment(Credit Card), 6: Address, 7: Go Back.");
                    String change_profile_decide = validate_int(sc, 1, 7);
                    int change_profile_decide_int = Integer.parseInt(change_profile_decide);
                    if (change_profile_decide_int == 7) {
                        // do nothing
                    } else if (change_profile_decide_int == 6) {
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
                        boolean is_valid = true;
                        String to_change;
                        if (change_profile_decide_int == 2) {
                            System.out.println("Format: (yyyy-mm-dd) ");
                            to_change = validate_time(sc);
                            LocalDate d1 = LocalDate.parse(to_change);
                            LocalDate d3 = LocalDate.parse("2004-01-01");
                            if (d1.isAfter(d3)) {
                                print_header("To young, should over 18.");
                                is_valid = false;
                            }
                        } else {
                            to_change = validate(sc);
                            if (change_profile_decide_int == 5 && (!to_change.toLowerCase().equals(to_change.toUpperCase()))) {
                                print_error("credit card can only have integer in it");
                                is_valid = false;
                            }
                        }
                        if (!is_valid) {
                            // too young
                        } else {
                            if (change_profile(username, to_change, change_profile_decide_int)) {
                                System.out.println("Successfully change profile");
                            } else {
                                print_error("Cannot change profile");
                            }
                        }
                    }
                } else if (profile_decide.equals("3")) {
                    print_header("Delete My Account");
                    System.out.println("Are you sure you want to delete your user?");
                    System.out.println("Press 1 to continue");
                    String delete_decide = sc.nextLine();
                    if (delete_decide.equals("1")) {
                        if (delete_user(username)) {
                            print_header("delete_success");
                            // set log out
                            is_login = 0;
                            is_owner = -1;
                            break;
                        } else {
                            print_error("delete error");
                        }
                    } else {
                    }
                } else if (profile_decide.equals("4")) {
                    break;
                }
                System.out.println("1: Check My Profile, 2: Change My Profile, 3: Delete My Account, 4: Go Back.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
