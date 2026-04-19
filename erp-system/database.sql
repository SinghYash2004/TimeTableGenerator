CREATE DATABASE IF NOT EXISTS erp_system;
USE erp_system;

CREATE TABLE IF NOT EXISTS department (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    budget_limit DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS faculty (
    faculty_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department_id INT NOT NULL,
    max_hours_per_week INT NOT NULL,
    cost_per_hour DOUBLE NOT NULL,
    UNIQUE KEY uk_faculty_name_dept (name, department_id),
    FOREIGN KEY (department_id) REFERENCES department(department_id)
);

CREATE TABLE IF NOT EXISTS subject (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    weekly_hours INT NOT NULL,
    department_id INT NOT NULL,
    UNIQUE KEY uk_subject_name_dept (name, department_id),
    FOREIGN KEY (department_id) REFERENCES department(department_id)
);

CREATE TABLE IF NOT EXISTS classroom (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_code VARCHAR(30) NOT NULL UNIQUE,
    building VARCHAR(50) NOT NULL DEFAULT 'MAIN',
    floor_no INT NOT NULL DEFAULT 0,
    room_type VARCHAR(30) NOT NULL DEFAULT 'LECTURE',
    equipment_tags VARCHAR(255) NULL,
    capacity INT NOT NULL,
    cost_per_hour DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS timeslot (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    day VARCHAR(20) NOT NULL,
    period INT NOT NULL,
    UNIQUE KEY uk_day_period (day, period)
);

CREATE TABLE IF NOT EXISTS section (
    section_id INT PRIMARY KEY AUTO_INCREMENT,
    section_name VARCHAR(20) NOT NULL,
    semester_no INT NOT NULL,
    strength INT NOT NULL,
    department_id INT NOT NULL,
    UNIQUE KEY uk_section_name_sem_dept (section_name, semester_no, department_id),
    FOREIGN KEY (department_id) REFERENCES department(department_id)
);

CREATE TABLE IF NOT EXISTS faculty_availability (
    faculty_id INT NOT NULL,
    slot_id INT NOT NULL,
    available TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (faculty_id, slot_id),
    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id),
    FOREIGN KEY (slot_id) REFERENCES timeslot(slot_id)
);

CREATE TABLE IF NOT EXISTS timetable (
    id INT PRIMARY KEY AUTO_INCREMENT,
    semester VARCHAR(20) NOT NULL,
    faculty_id INT NOT NULL,
    subject_id INT NOT NULL,
    room_id INT NOT NULL,
    slot_id INT NOT NULL,
    department_id INT NOT NULL,
    section_id INT NULL,
    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id),
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id),
    FOREIGN KEY (room_id) REFERENCES classroom(room_id),
    FOREIGN KEY (slot_id) REFERENCES timeslot(slot_id),
    FOREIGN KEY (department_id) REFERENCES department(department_id),
    FOREIGN KEY (section_id) REFERENCES section(section_id),
    UNIQUE KEY uk_subject_section_slot_sem (subject_id, section_id, slot_id, semester)
);

CREATE INDEX idx_faculty_slot ON timetable(faculty_id, slot_id, semester);
CREATE INDEX idx_room_slot ON timetable(room_id, slot_id, semester);
CREATE INDEX idx_section_slot ON timetable(section_id, slot_id, semester);

CREATE TABLE IF NOT EXISTS class_cancellation (
    cancellation_id INT PRIMARY KEY AUTO_INCREMENT,
    timetable_id INT NOT NULL,
    cancel_date DATE NOT NULL,
    reason VARCHAR(255) NULL,
    created_by VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_timetable_cancel_date (timetable_id, cancel_date),
    FOREIGN KEY (timetable_id) REFERENCES timetable(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS room_reservation (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT NOT NULL,
    slot_id INT NOT NULL,
    semester VARCHAR(20) NOT NULL,
    reserve_date DATE NOT NULL,
    faculty_id INT NULL,
    reservation_type VARCHAR(20) NOT NULL DEFAULT 'RESERVE',
    reason VARCHAR(255) NULL,
    created_by VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_room_slot_date (room_id, slot_id, reserve_date),
    FOREIGN KEY (room_id) REFERENCES classroom(room_id),
    FOREIGN KEY (slot_id) REFERENCES timeslot(slot_id),
    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id)
);
