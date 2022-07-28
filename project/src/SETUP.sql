-- Run this in the mysql terminal beforehand

-- Drop table
Drop TABLE IF EXISTS Sailors;

-- Create new tables and their schema
create table Sailors (
    sid int(50) NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sname varchar(100) NOT NULL,
    rating INT NOT NULL,
    age INT NOT NULL
);

-- Add data

/* POPULATE TABLES BELONGING IN OUR DESIRED SCHEMA */

INSERT INTO Sailors (sname,rating,age) VALUES ("Mike","10","23");
INSERT INTO Sailors (sname,rating,age) VALUES("Cicely","5","10");
INSERT INTO Sailors (sname,rating, age) VALUES ("Cicely","5","22");


