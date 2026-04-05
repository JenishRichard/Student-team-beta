INSERT INTO users (user_id, email, password_hash, role, status)
SELECT 'ADMIN001', 'admin@tus.ie', '$2y$10$ueEtBQG/tMdjpqyDn.6nLu/KSbUWyPccFPxlr3twNemN9e/OLh3NS', 'ADMIN', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE email = 'admin@tus.ie'
);

INSERT INTO users (user_id, email, password_hash, role, status)
SELECT 'TEACHER001', 'teacher@tus.ie', '$2y$10$7G9goz8EyNugOaoy9Q/HZOSw8xAiyUex4aZdOw3KkpoEj4fPaZGWO', 'TEACHER', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM users WHERE email = 'teacher@tus.ie'
);
