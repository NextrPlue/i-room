-- Initialize databases for all services
CREATE DATABASE IF NOT EXISTS iroom_user;
CREATE DATABASE IF NOT EXISTS iroom_management;
CREATE DATABASE IF NOT EXISTS iroom_sensor;
CREATE DATABASE IF NOT EXISTS iroom_dashboard;
CREATE DATABASE IF NOT EXISTS iroom_alarm;

-- Grant privileges
GRANT ALL PRIVILEGES ON iroom_user.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON iroom_management.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON iroom_sensor.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON iroom_dashboard.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON iroom_alarm.* TO 'root'@'%';

FLUSH PRIVILEGES;