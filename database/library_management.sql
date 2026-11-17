CREATE DATABASE IF NOT EXISTS library_management
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE library_management;

CREATE TABLE IF NOT EXISTS admins (
    admin_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS books (
    book_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    author VARCHAR(140) NOT NULL,
    publisher VARCHAR(140) NOT NULL DEFAULT '',
    category VARCHAR(100) NOT NULL DEFAULT '',
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    quantity INT NOT NULL,
    available_quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_books_price CHECK (price >= 0),
    CONSTRAINT chk_books_quantity CHECK (quantity >= 0),
    CONSTRAINT chk_books_available CHECK (available_quantity >= 0 AND available_quantity <= quantity)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(140) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL DEFAULT '',
    department VARCHAR(120) NOT NULL DEFAULT '',
    academic_year VARCHAR(30) NOT NULL DEFAULT '',
    address VARCHAR(255) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS issued_books (
    issue_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(30) NOT NULL,
    book_id BIGINT NOT NULL,
    issued_on DATE NOT NULL,
    due_on DATE NOT NULL,
    status ENUM('ISSUED', 'RETURNED') NOT NULL DEFAULT 'ISSUED',
    returned_on DATE NULL,
    CONSTRAINT fk_issue_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_issue_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_issue_status_due (status, due_on),
    INDEX idx_issue_student (student_id),
    INDEX idx_issue_book (book_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS returned_books (
    return_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    issue_id BIGINT NOT NULL UNIQUE,
    student_id VARCHAR(30) NOT NULL,
    book_id BIGINT NOT NULL,
    returned_on DATE NOT NULL,
    overdue_days INT NOT NULL DEFAULT 0,
    fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    CONSTRAINT fk_return_issue FOREIGN KEY (issue_id) REFERENCES issued_books(issue_id) ON DELETE RESTRICT,
    CONSTRAINT fk_return_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_return_book FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE RESTRICT,
    CONSTRAINT chk_return_overdue CHECK (overdue_days >= 0),
    CONSTRAINT chk_return_fine CHECK (fine_amount >= 0),
    INDEX idx_return_date (returned_on)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fines (
    fine_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    return_id BIGINT NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'PARTIAL', 'PAID') NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    CONSTRAINT fk_fine_return FOREIGN KEY (return_id) REFERENCES returned_books(return_id) ON DELETE RESTRICT,
    CONSTRAINT chk_fine_amount CHECK (amount >= 0 AND paid_amount >= 0 AND paid_amount <= amount),
    INDEX idx_fine_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS fine_payments (
    payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fine_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    paid_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_fine FOREIGN KEY (fine_id) REFERENCES fines(fine_id) ON DELETE RESTRICT,
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    INDEX idx_payment_date (paid_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS settings (
    setting_key VARCHAR(80) PRIMARY KEY,
    setting_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

INSERT INTO settings (setting_key, setting_value)
VALUES ('fine_per_day', '1.00'), ('loan_period_days', '14')
ON DUPLICATE KEY UPDATE setting_key = VALUES(setting_key);

INSERT IGNORE INTO books (isbn, title, author, publisher, category, price, quantity, available_quantity) VALUES
('9780132350884', 'Clean Code', 'Robert C. Martin', 'Prentice Hall', 'Software Engineering', 42.50, 5, 5),
('9780134685991', 'Effective Java', 'Joshua Bloch', 'Addison-Wesley', 'Programming', 48.00, 4, 4),
('9780596009205', 'Head First Design Patterns', 'Eric Freeman', 'O''Reilly Media', 'Software Engineering', 39.99, 3, 3),
('9781491950357', 'Learning SQL', 'Alan Beaulieu', 'O''Reilly Media', 'Database', 35.00, 2, 2);

INSERT IGNORE INTO students (student_id, name, email, phone, department, academic_year, address) VALUES
('STU-1001', 'Aarav Sharma', 'aarav.sharma@example.edu', '555-0101', 'Computer Science', 'Year 2', 'North Campus'),
('STU-1002', 'Maya Patel', 'maya.patel@example.edu', '555-0102', 'Information Systems', 'Year 3', 'West Campus'),
('STU-1003', 'Noah Williams', 'noah.williams@example.edu', '555-0103', 'Computer Science', 'Year 1', 'East Campus');
