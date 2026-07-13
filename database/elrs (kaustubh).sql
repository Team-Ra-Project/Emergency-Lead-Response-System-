-- ============================================================================
--  Emergency Lead Response System (ELRS) — MySQL 8.x schema
-- ----------------------------------------------------------------------------
--  Import order matters (foreign keys). Run the whole file on a fresh `elrs`
--  database. All statements use CREATE TABLE IF NOT EXISTS so re-running is
--  safe and it will not drop existing data.
--
--    CREATE DATABASE IF NOT EXISTS elrs
--      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
--    USE elrs;
-- ============================================================================

CREATE DATABASE IF NOT EXISTS elrs
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE elrs;

-- ---------------------------------------------------------------------------
-- 1. Roles
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    role_id   INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 2. Users  (auth + Profile module)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    password_hash VARCHAR(100) NOT NULL,
    role_id       INT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    -- Profile module extras
    avatar_url    VARCHAR(255),
    department    VARCHAR(120),
    designation   VARCHAR(120),
    employee_code VARCHAR(50),
    date_joined   DATE,
    -- Password reset support
    reset_token   VARCHAR(120),
    reset_expiry  DATETIME,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 3. Staff
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS staff (
    staff_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    designation    VARCHAR(120),
    specialization VARCHAR(120),
    availability   VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    joined_on      DATE,
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uq_staff_user (user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 4. Customers  (Customer Management module)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id   INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(120) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    email         VARCHAR(150),
    address       VARCHAR(255),
    city          VARCHAR(80),
    state         VARCHAR(80),
    pincode       VARCHAR(12),
    customer_type VARCHAR(30) NOT NULL DEFAULT 'Individual',
    status        VARCHAR(20) NOT NULL DEFAULT 'Active',
    notes         TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customers_name (full_name),
    INDEX idx_customers_phone (phone)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 5. Services  (Services module)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS services (
    service_id  INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    category    VARCHAR(80),
    description TEXT,
    price       DECIMAL(10,2) DEFAULT 0.00,
    duration    VARCHAR(40),
    status      VARCHAR(30) NOT NULL DEFAULT 'Active',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 6. Leads  (Lead Management module — already used by LeadDAO)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS leads (
    lead_id        INT AUTO_INCREMENT PRIMARY KEY,
    customer_id    INT NOT NULL,
    service_id     INT,
    priority       VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',   -- LOW|MEDIUM|HIGH|EMERGENCY
    status         VARCHAR(20) NOT NULL DEFAULT 'NEW',      -- NEW|CONTACTED|QUALIFIED|QUOTE_SENT|BOOKED|COMPLETED|LOST
    assigned_staff INT,
    notes          TEXT,
    source         VARCHAR(60),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leads_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_leads_service  FOREIGN KEY (service_id)  REFERENCES services(service_id)  ON DELETE SET NULL,
    CONSTRAINT fk_leads_staff    FOREIGN KEY (assigned_staff) REFERENCES staff(staff_id)   ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 7. Appointments  (Appointment module)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointments (
    appointment_id   INT AUTO_INCREMENT PRIMARY KEY,
    customer_id      INT,
    customer_name    VARCHAR(120) NOT NULL,
    staff_id         INT,
    staff_name       VARCHAR(120),
    service_id       INT,
    service_name     VARCHAR(120),
    appointment_date DATE NOT NULL,
    appointment_time TIME,
    duration         VARCHAR(40),
    status           VARCHAR(20) NOT NULL DEFAULT 'Booked',  -- Booked|Pending|Rescheduled|Completed|Cancelled
    location         VARCHAR(255),
    notes            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appt_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_appt_staff    FOREIGN KEY (staff_id)    REFERENCES staff(staff_id)        ON DELETE SET NULL,
    CONSTRAINT fk_appt_service  FOREIGN KEY (service_id)  REFERENCES services(service_id)   ON DELETE SET NULL,
    INDEX idx_appt_date (appointment_date)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 8. Settings  (Settings module — flexible key/value store)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settings (
    setting_key   VARCHAR(80) PRIMARY KEY,
    setting_value TEXT,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 9. Notifications  (Notification module — used by NotificationDAO)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT,
    title           VARCHAR(150),
    message         TEXT,
    type            VARCHAR(40),
    channel         VARCHAR(40),
    is_read         TINYINT(1) NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================================
--  If your `users` table already exists WITHOUT the Profile columns, run these
--  once (ignore "Duplicate column" errors for columns you already have):
--
--    ALTER TABLE users ADD COLUMN avatar_url    VARCHAR(255);
--    ALTER TABLE users ADD COLUMN department    VARCHAR(120);
--    ALTER TABLE users ADD COLUMN designation   VARCHAR(120);
--    ALTER TABLE users ADD COLUMN employee_code VARCHAR(50);
--    ALTER TABLE users ADD COLUMN date_joined   DATE;
-- ============================================================================

-- ============================================================================
--  SEED DATA
-- ============================================================================
INSERT INTO roles (role_name) VALUES
    ('ADMIN'), ('BUSINESS_OWNER'), ('STAFF'), ('CUSTOMER')
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- Default admin login  ->  email: admin@elrs.com   password: Admin@123
INSERT INTO users (full_name, email, phone, password_hash, role_id, status,
                   department, designation, employee_code, date_joined)
SELECT 'System Administrator', 'admin@elrs.com', '+91 90000 00000',
       '$2a$10$XiIeLM2c509nb9tvWeH8tOc1hC2bMPEydNoykzfe7bMnFmIHU/B72',
       r.role_id, 'ACTIVE', 'Emergency Operations', 'Administrator',
       'ELRS-EMP-1001', CURDATE()
FROM roles r WHERE r.role_name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@elrs.com');

-- Sample services
INSERT INTO services (name, category, description, price, duration, status) VALUES
    ('Ambulance',          'Medical',   'Emergency ambulance dispatch and patient transport.', 2500.00, '30 Minutes', 'Active'),
    ('Fire Rescue',        'Rescue',    'Rapid fire response and rescue operations.',          5000.00, '1 Hour',     'Active'),
    ('Medical Assistance', 'Medical',   'On-site emergency medical assistance.',               1500.00, '45 Minutes', 'Active'),
    ('Police Assistance',  'Security',  'Emergency police assistance and coordination.',          0.00, '1 Hour',     'Active')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Default settings
INSERT INTO settings (setting_key, setting_value) VALUES
    ('business_name',   'Emergency Lead Response System'),
    ('business_email',  'contact@elrs.com'),
    ('business_phone',  '+91 90000 00000'),
    ('business_address',''),
    ('working_hours',   '24/7'),
    ('whatsapp_api_key',''),
    ('smtp_host',       ''),
    ('smtp_port',       '587'),
    ('smtp_user',       ''),
    ('sms_api_key',     ''),
    ('logo_url',        '../../images/logo.png'),
    ('theme',           'light')
ON DUPLICATE KEY UPDATE setting_value = setting_value;

-- ============================================================================
--  End of schema
-- ============================================================================
