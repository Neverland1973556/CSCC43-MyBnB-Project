import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static boolean is_admin = false;

    // user data
    private static String username;

    // some strings
    private static final String is_owner_prompt = "1: Personal Information, 2: Manage My Listings, 3: Listing Availability, 4: Check Booking, 5: rating, 9: logout";
    private static final String is_renter_prompt = "1: Personal Information, 2: Manage My Booking, 5: rating, 9: logout";
    private static final String a_line = "--------------------------------------------------------------------------------";
    private static final String half_line = "-----------------------------------";

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

            File setup = new File("src/setup_table.sql");
            assert (setup.exists());
            System.out.println("Preparing start up database...");
            Scanner set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
                String data = set.next();
                stmt.execute(data);
            }
            set.close();

            String calendar_sql = "DROP PROCEDURE IF EXISTS insert_year_dates;";
            System.out.println(calendar_sql);
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

            setup = new File("src/insert_data.sql");
            assert (setup.exists());
            set = new Scanner(setup);
            set.useDelimiter(";");
            while (set.hasNext()) {
                String data = set.next();
                stmt.execute(data);
            }
            set.close();

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
                        } else if (input.equals("3")) {
                            print_header("You are in administer mode");
                            is_admin = true;
                            break;
                        } else if (input.equals("break")) {
                            System.out.println("Terminate the program.");
                            break label_whole;
                        } else {
                            // not valid
                            print_error("Please use valid input!");
                            System.out.println(a_line);
                            System.out.println("Press 1 to Login, 2 to Register, 3: Administer Mode.");
                        }
                    }
                }

                if (is_admin) {
                    // can see report
                    System.out.println("Select a report to see!");
                    System.out.println("1: sdfsdf, 2: dssdfsd, 9: logout");
                    while (sc.hasNextLine()) {
                        String input = sc.nextLine();
                        if (input.equals("1")) {
                            print_header("sdfsdf");
                        } else if (input.equals("9")) {
                            is_admin = false;
                            continue label_whole;
                        } else {
                            print_error("Input is not valid");
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
                        String input = validate_int(sc, 1, 9);
                        if (input.equals("1")) {
                            select_profile(sc);
                            if (is_owner == -1) {
                                continue label_whole;
                            }
                        } else if (input.equals("2")) {
                            print_header("Manage My Listings");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Check My Listings, 2: Add a Listing, 3: Change a Listing, 4: Go Back.");
                            String listing_decide = validate_int(sc, 1, 4);
                            if (listing_decide.equals("1")) {
                                print_header("Check My Listings");
                                // get all my listings
                                if (!show_user_owns(username)) {
                                    print_header("Currently you don't have a listing.");
                                }
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
                                if (!check_address_owner(postal_code, unit, city, country)) {
                                    // not valid
                                } else {
                                    System.out.println("Address is valid");
                                    String reg = "UserName:" + username + ", Lon:" + longitude + ", Lat:" + latitude + ", Type:" + listing_type;
                                    System.out.println(reg);
                                    String success = create_listing(reg, postal_code, unit, city, country);
                                    if (success.equals("false")) {
                                        print_error("Cannot add, please try again");
                                    } else {
                                        // success, can continue
                                        System.out.println("Add success!");
                                    }
                                }

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
                                    System.out.println("Choose the property of the listing you want to modify.");
                                    System.out.println("1: latitude, 2: longitude, 3: type, 4: address");
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
                                    } else {
                                        print_error("Not Valid input when change a Listing!");
                                    }
                                } else if (change_listing_decide.equals("2")) {
                                    print_header("Delete a Listing");
                                    if (!show_user_owns(username)) {
                                        print_header("Currently you don't have a list");
                                        break label_listing;
                                    }
                                    System.out.println("Type the lid of the list your want to delete.");
                                    String lid = validate(sc);
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
                            } else if (listing_decide.equals("4")) {
                                // not valid
                                // continue;
                            }
                        } else if (input.equals("3")) {
                            print_header("Listing Availability");
                            System.out.println("Press the Corresponding number to continue");
                            System.out.println("1: Show Availability, 2: Add Availability, 3: Change Availability, 4: Go Back.");
                            String avail = validate_int(sc, 1, 4);
                            label_avail:
                            if (avail.equals("1")) {
                                print_header("Show Availability");
                                if (!show_user_owns(username)) {
                                    print_header("Currently you don't have a listing.");
                                    break label_avail;
                                }
                                System.out.println("Please type the lid your want to check");
                                String lid = validate_int(sc, 0, 9999);
                                if (!show_available(lid)) {
                                    print_error("Cannot find availability for giving lid.");
                                }

                            } else if (avail.equals("2")) {
                                print_header("Add Availability");
                                if (!show_user_owns(username)) {
                                    print_header("Currently you don't have a listing.");
                                    break label_avail;
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
                                    continue;
                                }
                                LocalDate d3 = LocalDate.parse("2022-01-01");
                                LocalDate d4 = LocalDate.parse("2024-01-01");
                                if (d1.isAfter(d2)) {
                                    print_error("End date is before start date.");
                                    end_of_owner();
                                    continue;
                                }
                                if (d3.isAfter(d1)) {
                                    print_error("Start date is too early");
                                    end_of_owner();
                                    continue;
                                }
                                if (d2.isAfter(d4)) {
                                    print_error("End date is too late");
                                    end_of_owner();
                                    continue;
                                }
                                System.out.println("Please input daily price of the available:");
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
                                        break label_avail;
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
                                        continue;
                                    }
                                    LocalDate d3 = LocalDate.parse("2022-01-01");
                                    LocalDate d4 = LocalDate.parse("2024-01-01");
                                    if (d1.isAfter(d2)) {
                                        print_error("End date is before start date.");
                                        end_of_owner();
                                        continue;
                                    }
                                    if (d3.isAfter(d1)) {
                                        print_error("Start date is too early");
                                        end_of_owner();
                                        continue;
                                    }
                                    if (d2.isAfter(d4)) {
                                        print_error("End date is too late");
                                        end_of_owner();
                                        continue;
                                    }
                                    System.out.println("Please input daily price of the available:");
                                    String price = validate_double(sc, 0, 9999);
                                    // in this period, this listing is this price
                                    // hard = true, will delete first
                                    add_available(d1, d2, price, lid, true);

                                } else if (change_avail.equals("2")) {
                                    print_header(" Remove Availability.");
                                    if (!show_user_owns(username)) {
                                        print_header("Currently you don't have a listing.");
                                        break label_avail;
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
                                        continue;
                                    }
                                    LocalDate d3 = LocalDate.parse("2022-01-01");
                                    LocalDate d4 = LocalDate.parse("2024-01-01");
                                    if (d1.isAfter(d2)) {
                                        print_error("End date is before start date.");
                                        end_of_owner();
                                        continue;
                                    }
                                    if (d3.isAfter(d1)) {
                                        print_error("Start date is too early");
                                        end_of_owner();
                                        continue;
                                    }
                                    if (d2.isAfter(d4)) {
                                        print_error("End date is too late");
                                        end_of_owner();
                                        continue;
                                    }
                                    remove_available(d1, d2, lid);
                                } else if (change_avail.equals("3")) {
                                    // continue;
                                }
                            } else if (avail.equals("4")) {
                                // continue the loop;
                            }
                        } else if (input.equals("4")) {
                            System.out.println("Check Booking");
                        } else if (input.equals("5")) {
                            System.out.println("Rating and Comment");
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
                            System.out.println("1: Show my bookings, 2: Book a listing, 3: Cancel a booking.");
                            String booking_decide = sc.nextLine();
                            if (booking_decide.equals("1")) {
                                print_header("Show my bookings");
                                if (!show_user_books(username)) {
                                    print_header("you don't have any booking");
                                }
                                // get all my listings
                                // show_user_booking(username);
                            } else if (booking_decide.equals("2")) {
                                print_header("Book a listing");

                                String reg = "";
                                // create_book(username, payment, month, day, lid);
                                // get_all listings that is available
                                // search?
                                // choose the one you want to book
                                // book_by_aid

                            } else if (booking_decide.equals("3")) {
                                print_header("Cancel a booking");
                                // get all my listings
                                // show_user_booking(username);
                                // choose the one you want to cancel
                                // cancel by aid
                            } else {
                                print_error("Not Valid input!");
                            }
                        } else if (input.equals("5")) {
                            print_header("Rating and Comment");
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

    // sql functions
    // public static void create_calendar() throws SQLException {
    //     try {
    //         String month = "1";
    //         String day = "1";
    //
    //         for (int j = 1; j < 13; j++) {
    //             month = String.format("%d", j);
    //             for (int i = 1; i < 32; i++) {
    //                 if (!(i == 23 && j == 3)) { // Avoid deleting the available related
    //                     day = String.format("%d", i);
    //                     String sql = String.format("INSERT INTO Calendar (month, day) VALUES (\"%s\", \"%s\");", month, day);
    //                     // System.out.print(sql.concat("\n"));
    //                     stmt.executeUpdate(sql);
    //                 }
    //             }
    //         }
    //         String sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "2", "31");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "2", "30");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "2", "29");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "4", "31");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "6", "31");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "9", "31");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         sql = String.format("DELETE FROM Calendar where month = \"%s\" and day = \"%s\";", "11", "31");
    //         // System.out.print(sql.concat("\n"));
    //         stmt.executeUpdate(sql);
    //         System.out.print("Calendar created!");
    //
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

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
            String reg = "Name:(?<name>.*), Password:(?<password>.*), Occupation:(?<occupation>.*), SIN:(?<sin>.*), Birth:(?<birth>.*), UserName:(?<username>.*)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String name = matcher.group("name");
                String password = matcher.group("password");
                String occupation = matcher.group("occupation");
                String sin = matcher.group("sin");
                String birth = matcher.group("birth");
                String username = matcher.group("username");
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
            e.printStackTrace();
            return false;
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
                System.out.println(", Type: " + type);
            }
            rs.close();
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            return result;
        }
    }

    public static String create_listing(String input, String postal_code, String unit, String city, String country) throws SQLException {
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
                String sql = String.format("INSERT INTO Listing (lon, lat, type) VALUES ( '%s', '%s', '%s');", lon, lat, type);
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
                int birth = rs.getInt("birth");
                String password = rs.getString("password");
                // Display values
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
            String sql = String.format("SELECT * FROM Books Natural Join Available Natural Join Listing where username = '%s';", username);
            ResultSet rs = stmt.executeQuery(sql);
            // STEP 5: Extract data from result set
            while (rs.next()) {
                // Retrieve by column name
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
            String sql;
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

    public static boolean add_available(LocalDate start_time, LocalDate end_time, String price, String lid, boolean hard) throws SQLException {
        try {
            // start time, end time, price, lid is valid
            LocalDate temp = start_time;
            String sql;
            if (hard) {
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

    /*
    public static boolean create_available(String input) throws SQLException {
        try {
            String reg = "Price:(?<price>.*), Month:(?<month>.*), Day:(?<day>.*), Lid:(?<lid>.*)";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String price = matcher.group("price").toString();
                String month = matcher.group("month").toString();
                String day = matcher.group("day").toString();
                String lid = matcher.group("lid").toString();
                String sql = String.format("INSERT INTO Available (price, month, day, lid) VALUES ( \"%s\", \"%s\", \"%s\", \"%s\");", price, month, day, lid);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);
                return true;
            } else {
                System.out.println("Invalid Listing.");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Something went wrong with creating Available");
            return false;
        }
    }

    public static boolean create_book(String username, String payment, String month, String day, String lid) throws SQLException {
        try {
            String sql = String.format("SELECT * FROM Available where month = \"%s\" and day = \"%s\" and lid = \"%s\";", month, day, lid);
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String price = rs.getString("price");
                sql = String.format("DELETE FROM Available where month = \"%s\" and day = \"%s\" and lid = \"%s\";", month, day, lid);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);

                bookid++;
                sql = String.format("INSERT INTO Book (payment, BID) VALUES (\"%s\", \"%s\");", payment, bookid);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);

                sql = String.format("INSERT INTO Available (price, month, day, lid, BID) VALUES (\"%s\", \"%s\", \"%s\", \"%s\", \"%s\");", price, month, day, lid, bookid);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);

                sql = String.format("INSERT INTO Books (BID, username) VALUES (\"%s\", \"%s\");", bookid, username);
                System.out.print(sql.concat("\n"));
                stmt.executeUpdate(sql);
                return true;
            }
            System.out.println("The booking is not available.");
            return false;
        } catch (SQLException e) {
            System.err.println("Something went wrong with creating Book");
            return false;
        }
    }


     */
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
                print_error("Input time is not valid!");
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
        try {
            System.out.println(half_line + "Update Information" + half_line);
            System.out.println("1: Check My Profile, 2: Change My Profile, 3: Delete My Account, 4: Go Back.");
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
                System.out.println("1: Real Name, 2: Birth Year, 3: Password, 4: Occupation, 5: Address, 6: Go Back.");
                String change_profile_decide = validate_int(sc, 1, 6);
                int change_profile_decide_int = Integer.parseInt(change_profile_decide);
                if (change_profile_decide_int == 6) {
                    // do nothing
                } else if (change_profile_decide_int == 5) {
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
                System.out.println("Are you sure you want to delete your user?");
                System.out.println("Press 1 to continue");
                String delete_decide = sc.nextLine();
                if (delete_decide.equals("1")) {
                    if (delete_user(username)) {
                        print_header("delete_success");
                        // set log out
                        is_login = 0;
                        is_owner = -1;
                        // todo: break
                        // continue label_whole;
                    } else {
                        print_error("delete error");
                    }
                } else {
                    // don't delete
                    // go back
                }
            } else if (profile_decide.equals("4")) {
                // not valid
                // go back
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
