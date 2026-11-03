-- Sample data. Run after schema.sql.
-- Passwords (BCrypt-hashed below):
--   admin / admin123
--   abu  / user123
--   ali  / user123
INSERT INTO users (username, password, full_name, role, created_dts) VALUES
    ('admin', '$2a$10$m7Nhz7JyR938SzlhNXJQMOPSPvzv8SChzSjW32fF3MyMM8GcKpHO.', 'System Administrator', 'ADMIN', CURRENT_TIMESTAMP),
    ('abu',  '$2a$10$wDuJhAexjxiKCWezAxIOCeI9..7n8PZs3A.5q/9ZZzaEFzvrjMflC', 'Abu Bakar',             'USER', CURRENT_TIMESTAMP),
    ('ali',  '$2a$10$wDuJhAexjxiKCWezAxIOCeI9..7n8PZs3A.5q/9ZZzaEFzvrjMflC', 'Ali bin Abu',           'USER', CURRENT_TIMESTAMP);

INSERT INTO tasks (title, description, status, assigned_uid, created_uid, created_dts, updated_dts) VALUES
    ('Task 1',     'Description for Task 1', 'IN_PROGRESS',
        (SELECT id FROM users WHERE username = 'ali'), (SELECT id FROM users WHERE username = 'admin'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Task 2', 'Description for Task 2',        'NEW',
        (SELECT id FROM users WHERE username = 'ali'), (SELECT id FROM users WHERE username = 'admin'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Task 3',  'Description for Task 3',    'COMPLETED',
        (SELECT id FROM users WHERE username = 'abu'), (SELECT id FROM users WHERE username = 'admin'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Task 4',   'Description for Task 4',             'NEW',
        NULL,                                           (SELECT id FROM users WHERE username = 'admin'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
