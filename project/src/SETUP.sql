-- Run this in the mysql terminal beforehand

-- Drop table
Drop TABLE IF EXISTS Sailors;
Drop TABLE IF EXISTS Address;
Drop TABLE IF EXISTS User;
Drop TABLE IF EXISTS Listing;
Drop TABLE IF EXISTS Book;
Drop TABLE IF EXISTS Calendar;
Drop TABLE IF EXISTS Comment;
Drop TABLE IF EXISTS Judgement;
Drop TABLE IF EXISTS Available;
-- Create new tables and their schema
create table Sailors (
    sid int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sname varchar(100) NOT NULL,
    rating INT NOT NULL,
    age INT NOT NULL
);

create table Address (
    unit INT NOT NULL, 
    city varchar(100) NOT NULL,
    country varchar(100) NOT NULL,
    postal_code char(7) NOT NULL,  
    PRIMARY KEY (postal_code, unit)
);

create table User (
    SIN int(12) NOT NULL PRIMARY KEY, 
    name varchar(100) NOT NULL,
    password varchar(100) NOT NULL,
    occupation varchar(100),
    birth int(5) NOT NULL   
);

create table Listing (
    lid int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lon decimal(10,6) NOT NULL,
    lat decimal(10,6) NOT NULL,
    type ENUM("full house", "apartment", "room") NOT NULL
);

create table Book (
    BID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment float(10,2) NOT NULL,
    cancellation TINYINT DEFAULT 0
);

create table Comment (
    CID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rate int(1) NOT NULL, /*range from 1 to 5, constraints set in java file*/
    text varchar(250) /*comments less than 250*/
);

create table Judgement (
    JID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    likes int(1) DEFAULT 3,
    words varchar(100)
);

/*It seems that Judgement should have a ISA relation with comment , nvm so far*/

create table Calendar (
    /*date int(3) NOT NULL PRIMARY KEY Convert month-day into integer in 0~365*/
    month int(2) NOT NULL,
    day int(2) NOT NULL,
    PRIMARY KEY (month, day)
);

create table Available (
    price int(50) NOT NULL PRIMARY KEY /*hasn't check how to set relation so far*/
);
-- Add data

/* POPULATE TABLES BELONGING IN OUR DESIRED SCHEMA */

INSERT INTO Sailors (sname,rating,age) VALUES ("Mike","10","23");
INSERT INTO Sailors (sname,rating,age) VALUES("Cicely","5","10");
INSERT INTO Sailors (sname,rating, age) VALUES ("Cicely","5","22");


INSERT INTO Address (unit, city, country, postal_code) VALUES ("1367", "Toronto", "Canada", "M1C 1A");
INSERT INTO User (SIN, name, password, birth) VALUES ("123124125", "Jonathan", "123456", "2001"); /*Do we need to parse sin*/
INSERT INTO Listing (lon, lat, type) VALUES ( "30.222", "18.999", "full house"); 
INSERT INTO Book (payment, BID) VALUES ( "130.22", "18"); 
INSERT INTO Calendar (month, day) VALUES("03", "23"); /*In Java, create all dates in a year*/
INSERT INTO Comment (rate, text) VALUES ("3", "The bed is noisy--it is shaky. The air conditioner also doesn't work well. But the view is nice and all other service are good.");
INSERT INTO Comment (rate, text) VALUES ("5", "My husband loves it");
INSERT INTO Judgement (words) VALUES ("The room was cleaned by the renter!");
INSERT INTO Available (price) VALUES ("30");



created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP