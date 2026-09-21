INSERT INTO EMPLOYEE (ID, VERSION, VISA, FIRST_NAME, LAST_NAME, BIRTH_DATE) VALUES
    (1, 0, 'DTH', 'Thien', 'Doan', '1995-05-15'),
    (2, 0, 'BHU', 'Hung', 'Bui', '1992-08-20'),
    (3, 0, 'JHV', 'Hai', 'Vu', '1994-11-10'),
    (4, 0, 'HTV', 'Thang', 'Vo', '1990-03-25'),
    (5, 0, 'NQN', 'Nhat', 'Nguyen', '1996-07-12'),
    (6, 0, 'HNH', 'Hao', 'Nguyen', '1993-09-08'),
    (7, 0, 'TQP', 'Phuong', 'Tran', '1991-12-01'),
    (8, 0, 'QMV', 'Minh', 'Quach', '1989-04-18');

INSERT INTO "group" (ID, VERSION, GROUP_LEADER_ID) VALUES
    (1, 0, 1),
    (2, 0, 2),
    (3, 0, 3);

INSERT INTO PROJECT (ID, VERSION, GROUP_ID, PROJECT_NUMBER, NAME, CUSTOMER, STATUS, START_DATE, END_DATE) VALUES
    (1, 0, 1, 1001, 'EFV', 'Customer A', 'NEW', '2025-01-10', '2025-12-31'),
    (2, 0, 1, 1002, 'CXTRANET', 'Customer B', 'PLA', '2025-02-01', '2025-08-30'),
    (3, 0, 2, 1003, 'CRYSTAL BALL', 'Customer A', 'INP', '2024-06-15', '2025-06-15'),
    (4, 0, 2, 1004, 'IOC CLIENT EXTRANET', 'Customer C', 'FIN', '2023-01-01', '2024-01-01'),
    (5, 0, 3, 1005, 'TRADEECO', 'Customer B', 'NEW', '2025-03-01', null),
    (6, 0, 3, 1006, 'KSTA MIGRATION', 'Customer D', 'INP', '2024-09-01', '2025-09-01');

INSERT INTO PROJECT_EMPLOYEE (PROJECT_ID, EMPLOYEE_ID) VALUES
    (1, 4),
    (1, 5),
    (2, 5),
    (2, 6),
    (3, 7),
    (3, 8),
    (4, 4),
    (4, 7),
    (4, 8),
    (5, 6),
    (6, 5),
    (6, 7);

ALTER SEQUENCE hibernate_sequence RESTART WITH 100;
