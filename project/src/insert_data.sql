INSERT INTO Address (unit, city, country, postal_code) VALUES ("1367", "Toronto", "Canada", "M1C 1A2");
INSERT INTO Address (unit, city, country, postal_code) VALUES ("1367", "Toronto", "Canada", "M1C 1A3");
INSERT INTO User (SIN, name, password, birth, username) VALUES ("123124125", "Jonathan", "123456", "2001", "Jonathan"); /*Do we need to parse sin*/
INSERT INTO User (SIN, name, password, birth, occupation, username) VALUES ("987654321", "Felix", "123456", "2002", "IronMan", "Felix"); /*Do we need to parse sin*/
/*delete from user where name="Felix"*/
INSERT INTO User (SIN, name, password, birth, occupation, username, payment) VALUES ("777777", "test real name", "test", "1999", "teacher", "test", "1234567891234567"); /*Do we need to parse sin*/
INSERT INTO Host (username) VALUES ("Jonathan");
INSERT INTO Host (username) VALUES ("test");
INSERT INTO Host (username) VALUES ("Felix");

INSERT INTO Renter (username) VALUES ("Jonathan");
INSERT INTO Renter (username) VALUES ("test");
INSERT INTO Renter (username) VALUES ("Felix");
INSERT INTO Listing (lon, lat, type) VALUES ( "30.22", "18.99", "full house");
INSERT INTO Listing (lon, lat, type) VALUES ( "3.22", "19", "apartment");
INSERT INTO Listing (lon, lat, type) VALUES ( "30.62", "1.99", "room");


INSERT INTO Book (start_date, end_date, price, payment, BID, username) VALUES ("2022-07-19", "2022-07-25","450", "4510199974972547", "1" , "Jonathan");
/*In Java, create all dates in a year*/
/*INSERT INTO Calendar (date) VALUES("2022-08-19")*/
INSERT INTO Comment (rate, text, lid, username) VALUES ("3", "The bed is noisy--it is shaky. The air conditioner also doesn't work well. But the view is nice and all other service are good.",2,"Jonathan");
INSERT INTO Comment (rate, text, lid, username) VALUES ("5", "My husband loves it", 3,"Felix");
INSERT INTO Judgement (words, host_username, renter_username, direction) VALUES ("The room was cleaned by the renter!", "Jonathan", "Jonathan",1);
INSERT INTO Available (price, date, lid) VALUES ("30", "2022-08-19","2");
INSERT INTO Lives (postal_code, unit, username) VALUES ( "M1C 1A2", "1367", "Jonathan");
INSERT INTO Lives (postal_code, unit, username) VALUES ( "M1C 1A3", "1367", "test");
INSERT INTO Located_At (postal_code, unit, lid) VALUES ( "M1C 1A2", "1367", "1");
INSERT INTO Owns (username, lid) VALUES ( "Jonathan", "1");
INSERT INTO Owns (username, lid) VALUES ( "Jonathan", "2");
INSERT INTO Owns (username, lid) VALUES ( "Jonathan", "3");