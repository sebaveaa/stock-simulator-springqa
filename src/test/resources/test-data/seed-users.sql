-- Script de Datos de Prueba - Usuarios
-- ISO/IEC 29119 - Test Data Specification
-- Password para todos los usuarios de prueba: "test123"
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu
-- Usa INSERT simple ya que la base de datos se limpia antes de cada test

-- Usuario de Prueba 1
INSERT INTO stockuser (username, email, first_name, last_name, hashed_password, verified, admin, confirmation_code) VALUES ('testuser1', 'test1@stocksim.com', 'Test', 'User1', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, false, null);

-- Usuario de Prueba 2
INSERT INTO stockuser (username, email, first_name, last_name, hashed_password, verified, admin, confirmation_code) VALUES ('testuser2', 'test2@stocksim.com', 'Test', 'User2', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, false, null);

-- Usuario para Pruebas de Carga
INSERT INTO stockuser (username, email, first_name, last_name, hashed_password, verified, admin, confirmation_code) VALUES ('loadtest', 'load@stocksim.com', 'Load', 'Tester', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, false, null);

-- Administrador para Pruebas
INSERT INTO stockuser (username, email, first_name, last_name, hashed_password, verified, admin, confirmation_code) VALUES ('admintest', 'admin@stocksim.com', 'Admin', 'Test', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, true, null);

-- Usuario No Verificado (para pruebas de verificación)
INSERT INTO stockuser (username, email, first_name, last_name, hashed_password, verified, admin, confirmation_code) VALUES ('unverified', 'unverified@stocksim.com', 'Unverified', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', false, false, 'TEST123');
