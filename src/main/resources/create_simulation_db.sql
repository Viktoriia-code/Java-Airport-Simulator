CREATE DATABASE simulation;

USE simulation;

CREATE TABLE parameters (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            check_in INT NOT NULL,
                            security_check INT NOT NULL,
                            fasttrack INT NOT NULL,
                            border_control INT NOT NULL,
                            EU_boarding INT NOT NULL,
                            non_EU_Boarding INT NOT NULL
);

CREATE TABLE results (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         servedPassenger INT NOT NULL,
                         simulationTime DOUBLE NOT NULL,
                         averageServiceTime DOUBLE NOT NULL,
                         longestQueue VARCHAR(255),
                         parameters_id INT NOT NULL UNIQUE,
                         FOREIGN KEY (parameters_id) REFERENCES parameters(id) ON DELETE CASCADE
);

-- Create a new user
CREATE USER 'user'@'localhost' IDENTIFIED BY 'password';

-- Grant all privileges on the simulation database
GRANT ALL PRIVILEGES ON simulation.* TO 'user'@'localhost';

-- Apply the changes
FLUSH PRIVILEGES;
