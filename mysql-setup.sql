select version();
CREATE SCHEMA IF NOT EXISTS vdt DEFAULT CHARACTER SET utf8;
SHOW DATABASES;


SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::" FROM performance_schema.global_variables WHERE variable_name='log_bin';

-- Create a demo user for bin_log based replication
CREATE USER 'foogaro' IDENTIFIED BY 'redis.2023';
-- Grant the required permissions to the user
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'foogaro';
-- Finalize the user’s permissions
FLUSH PRIVILEGES;
-- Check the permissions
SELECT * from mysql.`user` where user='foogaro';

SELECT variable_value as "BINARY LOGGING STATUS (log-bin) ::" FROM performance_schema.global_variables WHERE variable_name='log_bin';

use vdt;

CREATE TABLE IF NOT EXISTS person (
    id int NOT NULL,
    firstname varchar(50),
    lastname varchar(50),
    age int,
    PRIMARY KEY (id)
)
ENGINE = InnoDB;

desc person;

CREATE TABLE IF NOT EXISTS people (
    id int NOT NULL,
    firstname varchar(50),
    lastname varchar(50),
    age int,
    PRIMARY KEY (id)
)
ENGINE = InnoDB;

desc people;
