-- ============================================================
-- ELRS - Emergency Lead Response System
-- Database Schema (MySQL 8.x)
-- ============================================================
DROP DATABASE IF EXISTS elrs;
CREATE DATABASE elrs CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elrs;

-- ---------- ROLES ----------
CREATE TABLE roles (
  role_id     INT AUTO_INCREMENT PRIMARY KEY,
  role_name   VARCHAR(50) UNIQUE NOT NULL,
  description VARCHAR(255),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO roles(role_name, description) VALUES
 ('ADMIN','Full system access'),
 ('BUSINESS_OWNER','Business dashboard & management'),
 ('STAFF','Handles assigned leads'),
 ('CUSTOMER','Submits enquiries');

-- ---------- USERS ----------
CREATE TABLE users (
  user_id       INT AUTO_INCREMENT PRIMARY KEY,
  full_name     VARCHAR(120) NOT NULL,
  email         VARCHAR(150) UNIQUE NOT NULL,
  phone         VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL,
  role_id       INT NOT NULL,
  status        ENUM('ACTIVE','INACTIVE','LOCKED') DEFAULT 'ACTIVE',
  reset_token   VARCHAR(100),
  reset_expiry  DATETIME,
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- ---------- STAFF ----------
CREATE TABLE staff (
  staff_id      INT AUTO_INCREMENT PRIMARY KEY,
  user_id       INT NOT NULL,
  designation   VARCHAR(100),
  specialization VARCHAR(150),
  availability  ENUM('AVAILABLE','BUSY','OFF') DEFAULT 'AVAILABLE',
  joined_on     DATE,
  CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------- CUSTOMERS ----------
CREATE TABLE customers (
  customer_id INT AUTO_INCREMENT PRIMARY KEY,
  full_name   VARCHAR(120) NOT NULL,
  phone       VARCHAR(20) NOT NULL,
  email       VARCHAR(150),
  address     VARCHAR(255),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ---------- SERVICES ----------
CREATE TABLE services (
  service_id  INT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) UNIQUE NOT NULL,
  description VARCHAR(255),
  base_price  DECIMAL(10,2) DEFAULT 0,
  active      TINYINT(1) DEFAULT 1
);
INSERT INTO services(name, description) VALUES
 ('Plumbing','Plumbing services'),
 ('Waterproofing','Waterproofing services'),
 ('Roof Repair','Roofing work'),
 ('Electrician','Electrical repairs'),
 ('Pest Control','Pest control'),
 ('Cleaning','Cleaning services');

-- ---------- LEADS ----------
CREATE TABLE leads (
  lead_id       INT AUTO_INCREMENT PRIMARY KEY,
  customer_id   INT NOT NULL,
  service_id    INT,
  priority      ENUM('LOW','MEDIUM','HIGH','EMERGENCY') DEFAULT 'MEDIUM',
  status        ENUM('NEW','CONTACTED','QUALIFIED','QUOTE_SENT','BOOKED','COMPLETED','LOST') DEFAULT 'NEW',
  assigned_staff INT,
  notes         TEXT,
  source        VARCHAR(50),
  created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_lead_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
  CONSTRAINT fk_lead_service  FOREIGN KEY (service_id)  REFERENCES services(service_id),
  CONSTRAINT fk_lead_staff    FOREIGN KEY (assigned_staff) REFERENCES staff(staff_id)
);

-- ---------- APPOINTMENTS ----------
CREATE TABLE appointments (
  appointment_id INT AUTO_INCREMENT PRIMARY KEY,
  lead_id        INT NOT NULL,
  staff_id       INT,
  scheduled_at   DATETIME NOT NULL,
  status         ENUM('BOOKED','RESCHEDULED','CANCELLED','COMPLETED') DEFAULT 'BOOKED',
  notes          VARCHAR(255),
  CONSTRAINT fk_appt_lead  FOREIGN KEY (lead_id)  REFERENCES leads(lead_id) ON DELETE CASCADE,
  CONSTRAINT fk_appt_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
);

-- ---------- NOTIFICATIONS ----------
CREATE TABLE notifications (
  notification_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id     INT NOT NULL,
  channel     ENUM('BROWSER','EMAIL','SMS','WHATSAPP') DEFAULT 'BROWSER',
  title       VARCHAR(150),
  message     VARCHAR(500),
  is_read     TINYINT(1) DEFAULT 0,
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ---------- REVIEWS ----------
CREATE TABLE reviews (
  review_id   INT AUTO_INCREMENT PRIMARY KEY,
  lead_id     INT NOT NULL,
  rating      TINYINT NOT NULL,
  comment     VARCHAR(500),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_lead FOREIGN KEY (lead_id) REFERENCES leads(lead_id) ON DELETE CASCADE
);

-- ---------- SETTINGS ----------
CREATE TABLE settings (
  setting_id  INT AUTO_INCREMENT PRIMARY KEY,
  key_name    VARCHAR(100) UNIQUE NOT NULL,
  value       VARCHAR(500)
);

-- ---------- ACTIVITY LOGS ----------
CREATE TABLE activity_logs (
  log_id      INT AUTO_INCREMENT PRIMARY KEY,
  user_id     INT,
  action      VARCHAR(150),
  entity      VARCHAR(100),
  entity_id   INT,
  ip_address  VARCHAR(45),
  created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ---------- SEED ADMIN ----------
-- password = Admin@123  (bcrypt hash generated via PasswordUtil)
INSERT INTO users(full_name,email,phone,password_hash,role_id)
VALUES ('System Admin','admin@elrs.local','9999999999',
 '$2a$10$7EqJtq98hPqEX7fNZaFWoOa5m0m0Yq8fW9d3ZJ8FQGm3RfE0eS4mK', 1);
