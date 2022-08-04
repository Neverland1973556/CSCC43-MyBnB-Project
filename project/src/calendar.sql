
DROP PROCEDURE IF EXISTS insert_year_dates;
DELIMITER $$
create PROCEDURE insert_year_dates()
BEGIN
    SET @t_current = DATE(NOW());
    SET @t_end = DATE(DATE_ADD(NOW(), INTERVAL 1 YEAR));
    WHILE(@t_current< @t_end) DO
        INSERT INTO Calendar (date) VALUES (@t_current);
        SET @t_current = DATE_ADD(@t_current, INTERVAL 1 DAY);
    END WHILE;
END;
$$
DELIMITER ;
CALL insert_year_dates();

---
