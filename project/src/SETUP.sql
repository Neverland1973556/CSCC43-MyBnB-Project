-- Run this in the mysql terminal beforehand
Create DATABASE IF NOT EXISTS mydatabase;
USE mydatabase;
-- Drop table
Drop TABLE IF EXISTS Available;
Drop TABLE IF EXISTS Judgement;
Drop TABLE IF EXISTS Owns;
Drop TABLE IF EXISTS Books;
Drop TABLE IF EXISTS Located_At;
Drop TABLE IF EXISTS Lives;
Drop TABLE IF EXISTS Comment;
Drop TABLE IF EXISTS Address;
Drop TABLE IF EXISTS Renter;
Drop TABLE IF EXISTS Host;
Drop TABLE IF EXISTS User;
Drop TABLE IF EXISTS Listing;
Drop TABLE IF EXISTS Book;
Drop TABLE IF EXISTS Calendar;
-- Create new tables and their schema

create table IF NOT EXISTS Address  (
                                        unit INT NOT NULL,
                                        city varchar(100) NOT NULL,
    country varchar(100) NOT NULL,
    postal_code char(7) NOT NULL,
    PRIMARY KEY (postal_code, unit)
    );

create table IF NOT EXISTS User (
    SIN int(12) NOT NULL UNIQUE,
    name varchar(100) NOT NULL,
    password varchar(100) NOT NULL,
    occupation varchar(100),
    birth int(5) NOT NULL,
    username varchar(100) NOT NULL PRIMARY KEY
    );

create table IF NOT EXISTS Renter (
    username varchar(100) NOT NULL PRIMARY KEY,
    foreign key (username) references User(username) ON DELETE CASCADE ON UPDATE CASCADE
    );

create table IF NOT EXISTS Host (
    username varchar(100) NOT NULL PRIMARY KEY,
    foreign key (username) references User(username) ON DELETE CASCADE ON UPDATE CASCADE
    );

create table IF NOT EXISTS Listing (
    lid int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lon decimal(10,6) NOT NULL,
    lat decimal(10,6) NOT NULL,
    type ENUM("full house", "apartment", "room") NOT NULL
    );

create table IF NOT EXISTS Lives (
                                     unit INT NOT NULL,
                                     postal_code char(7) NOT NULL,
    foreign key (postal_code, unit) references Address(postal_code, unit) ON DELETE CASCADE ON UPDATE CASCADE,
    username varchar(100) NOT NULL,
    foreign key (username) references USER(username) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (postal_code, unit, username)     /*changed to M-to-M relation*/
    );

create table IF NOT EXISTS Located_At (
                                          unit INT NOT NULL,
                                          postal_code char(7) NOT NULL,
    foreign key (postal_code, unit) references Address(postal_code, unit) ON DELETE CASCADE ON UPDATE CASCADE,
    lid int(50) NOT NULL,
    foreign key (lid) references Listing(lid) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (postal_code, unit),     /*changed to 1-to-1 relation*/
    UNIQUE (lid)
    );

create table IF NOT EXISTS Owns (
    lid int(50) NOT NULL,
    foreign key (lid) references Listing(lid) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (lid),
    username varchar(100) NOT NULL,
    foreign key (username) references Host(username) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE(lid)
    );

create table IF NOT EXISTS Book (
    BID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment float(10,2) NOT NULL,
    cancellation TINYINT DEFAULT 0
    );

create table IF NOT EXISTS Books (
    username varchar(100) NOT NULL,
    foreign key (username) references Renter(username) ON DELETE CASCADE ON UPDATE CASCADE,
    BID int(50) NOT NULL,
    foreign key (BID) references Book(BID) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (BID)
    );

create table IF NOT EXISTS Comment (
    CID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rate int(1) NOT NULL, /*range from 1 to 5, constraints set in java file*/
    text varchar(250), /*comments less than 250*/
    lid int(50) NOT NULL,
    foreign key (lid) references Listing(lid) ON DELETE CASCADE ON UPDATE CASCADE,
    username varchar(100) NOT NULL,
    foreign key (username) references Renter(username) ON DELETE CASCADE ON UPDATE CASCADE /*M-to-M & multiple time allowed*/
    );

create table IF NOT EXISTS Judgement (
    JID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    likes int(1) DEFAULT 3,
    words varchar(100),
    direction TINYINT DEFAULT 0,
    renter_username varchar(100) NOT NULL,
    host_username varchar(100) NOT NULL,
    foreign key (renter_username) references Renter(username) ON DELETE CASCADE ON UPDATE CASCADE,
    foreign key (host_username) references Host(username) ON DELETE CASCADE ON UPDATE CASCADE
    );

/*It seems that Judgement should have a ISA relation with comment , nvm so far*/

create table IF NOT EXISTS Calendar (
    /*date int(3) NOT NULL PRIMARY KEY Convert month-day into integer in 0~365*/
    month int(2) NOT NULL,
    day int(2) NOT NULL,
    PRIMARY KEY (month, day)
    );

create table IF NOT EXISTS Available (
    price int(50) NOT NULL,
    month int(2) NOT NULL,
    day int(2) NOT NULL,
    foreign key (month, day) references Calendar(month, day)
    ON DELETE CASCADE ON UPDATE CASCADE, /*Don't forget to create all days
                                                                in Java implementation, otherwise can not create*/
    lid int(50) NOT NULL,
    foreign key (lid) references Listing(lid) ON DELETE CASCADE ON UPDATE CASCADE,
    BID int(50) ,
    foreign key (BID) references Book(BID) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (BID)   /*to-One book*/
    );
-- Add data

/* POPULATE TABLES BELONGING IN OUR DESIRED SCHEMA */

INSERT INTO Address (unit, city, country, postal_code) VALUES ("1367", "Toronto", "Canada", "M1C 1A");
INSERT INTO User (SIN, name, password, birth, username) VALUES ("123124125", "Jonathan", "123456", "2001", "Jonathan"); /*Do we need to parse sin*/
INSERT INTO User (SIN, name, password, birth, occupation, username) VALUES ("987654321", "Felix", "123456", "2002", "IronMan", "Felix"); /*Do we need to parse sin*/
/*delete from user where name="Felix"*/
INSERT INTO Host (username) VALUES ("Jonathan");
INSERT INTO Renter (username) VALUES ("Jonathan");
INSERT INTO Renter (username) VALUES ("Felix");
INSERT INTO Listing (lon, lat, type) VALUES ( "30.222", "18.999", "full house");
INSERT INTO Listing (lon, lat, type) VALUES ( "3.222", "19", "apartment");
INSERT INTO Listing (lon, lat, type) VALUES ( "30.622", "1.999", "room");

INSERT INTO Book (payment, BID) VALUES ( "130.22", "18");
/*In Java, create all dates in a year*/
INSERT INTO Calendar (month, day) VALUES("03", "23");
INSERT INTO Comment (rate, text, lid, username) VALUES ("3", "The bed is noisy--it is shaky. The air conditioner also doesn't work well. But the view is nice and all other service are good.",2,"Jonathan");
INSERT INTO Comment (rate, text, lid, username) VALUES ("5", "My husband loves it", 3,"Felix");
INSERT INTO Judgement (words, host_username, renter_username, direction) VALUES ("The room was cleaned by the renter!", "Jonathan", "Jonathan",1);
INSERT INTO Available (price, month, day, lid) VALUES ("30", "03", "23","2");
INSERT INTO Lives (postal_code, unit, username) VALUES ( "M1C 1A", "1367", "Jonathan");
INSERT INTO Located_At (postal_code, unit, lid) VALUES ( "M1C 1A", "1367", "1");
INSERT INTO Owns (username, lid) VALUES ( "Jonathan", "1");
INSERT INTO Owns (username, lid) VALUES ( "Jonathan", "2");
INSERT INTO Owns (username, lid) VALUES ( "Jonathan", "3");
INSERT INTO Books (BID, username) VALUES ( "18", "Jonathan");



/*created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP*/