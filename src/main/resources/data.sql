INSERT INTO PROJECT (NAME, FINISHING_DATE, ACTIVATED)
VALUES
    ('EFV', '2020-04-20', true),
    ('CXTRANET', '2020-04-25', true),
    ('CRYSTAL BALL', '2020-04-28', true),
    ('IOC CLIENT EXTRANET', '2020-06-07', true),
    ('TRADEECO', '2020-06-08', true);

INSERT INTO USER (USERNAME, USERNAME_LENGTH)
VALUES
    ('USER1', 5),
    ('USER2', 5),
    ('USER3', 5);

INSERT INTO TASK(NAME, DEADLINE, PROJECT_ID, USER_ID)
VALUES
    ('EFV_TASK_1', '2020-03-05', 1, 1),
    ('EFV_TASK_2', '2020-03-10', 1, null),
    ('EFV_TASK_3', '2020-03-15', 1, null),
    ('EFV_TASK_4', '2020-03-20', 1, null),
    ('CXTRANET_TASK_1', '2020-04-01', 2, null),
    ('CXTRANET_TASK_2', '2020-04-10', 2, null),
    ('CXTRANET_TASK_3', '2020-04-15', 2, null),
    ('CRYSTAL_TASK_1', '2020-04-05', 3, null),
    ('CRYSTAL_TASK_2', '2020-04-15', 3, null),
    ('IOC_TASK_1', '2020-05-01', 4, null),
    ('IOC_TASK_2', '2020-05-15', 4, null),
    ('TRADEECO_TASK_1', '2020-05-20', 5, null),
    ('TRADEECO_TASK_2', '2020-05-25', 5, null);
