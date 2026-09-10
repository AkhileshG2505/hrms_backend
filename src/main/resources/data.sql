--used ai to seed data

INSERT INTO employees (id, name, email, annual_leave_quota, leave_taken)
SELECT * FROM (SELECT 1, 'Asha Rao', 'asha.rao@texlaculture.com', 20, 0) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 1);

INSERT INTO employees (id, name, email, annual_leave_quota, leave_taken)
SELECT * FROM (SELECT 2, 'Vikram Shah', 'vikram.shah@texlaculture.com', 18, 0) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM employees WHERE id = 2);


INSERT INTO app_users (id, username, password, role, employee_id)
SELECT * FROM (SELECT 1, 'hr_admin', '$2b$10$zVCpHtEwpwp0PR8Iq5QqbeHcCk7FKvabVFtSB41iDcc1I7GHleoEW', 'HR', NULL) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE id = 1);

INSERT INTO app_users (id, username, password, role, employee_id)
SELECT * FROM (SELECT 2, 'asha', '$2b$10$jNJ0yw3FypILA/nYnCNRBuWgjBZ8uEDvqd0L7B0SeE4mjSYYgN8RK', 'EMPLOYEE', 1) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE id = 2);


INSERT INTO app_users (id, username, password, role, employee_id)
SELECT * FROM (SELECT 3, 'vikram', '$2b$10$Es/vVTTAGMRjiONfifUoTeeFC3ztvY2lTY.TvhC6jaO5uL1NSn0/u', 'EMPLOYEE', 2) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM app_users WHERE id = 3);
