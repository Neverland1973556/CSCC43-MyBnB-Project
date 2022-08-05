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
Drop TABLE IF EXISTS Book;

Drop TABLE IF EXISTS Renter;
Drop TABLE IF EXISTS Host;
Drop TABLE IF EXISTS User;
Drop TABLE IF EXISTS Listing;
Drop TABLE IF EXISTS Calendar;
-- Create new tables and their schema

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

create table IF NOT EXISTS Address  (
    unit INT NOT NULL,
    city varchar(100) NOT NULL,
    country varchar(100) NOT NULL,
    postal_code char(7) NOT NULL,
    PRIMARY KEY (postal_code, unit)
    );

create table IF NOT EXISTS Listing (
    lid int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    lon decimal(10,2) NOT NULL,
    lat decimal(10,2) NOT NULL,
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
    foreign key (username) references Host(username) ON DELETE CASCADE ON UPDATE CASCADE
    );

create table IF NOT EXISTS Book (
    BID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price int(50) NOT NULL,
    payment BIGINT NOT NULL,
    cancellation Boolean DEFAULT false,
    username varchar(100) NOT NULL,
    foreign key (username) references Renter(username) ON DELETE CASCADE ON UPDATE CASCADE
    );

create table IF NOT EXISTS Comment (
    CID int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rate int(1) NOT NULL, /*range from 1 to 5, constraints set in java file*/
    lid int(50) NOT NULL,
    foreign key (lid) references Listing(lid) ON DELETE CASCADE ON UPDATE CASCADE,
    username varchar(100) NOT NULL,
    foreign key (username) references Renter(username) ON DELETE CASCADE ON UPDATE CASCADE, /*M-to-M & multiple time allowed*/
    text varchar(250) /*comments less than 250*/
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
    date Date NOT NULL,
    PRIMARY KEY (date)
    );

create table IF NOT EXISTS Available (
    price int(50) NOT NULL,
    date Date NOT NULL,
    foreign key (date) references Calendar(date)
    ON DELETE CASCADE ON UPDATE CASCADE, /*Don't forget to create all days
                                                                in Java implementation, otherwise can not create*/
    lid int(50) NOT NULL,
    foreign key (lid) references Listing(lid) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (lid, date),
    BID int(50) ,
    foreign key (BID) references Book(BID) ON DELETE CASCADE ON UPDATE CASCADE
    /*UNIQUE (BID)   to-One book*/
    );
-- Add data

/* POPULATE TABLES BELONGING IN OUR DESIRED SCHEMA */