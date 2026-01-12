-- Script de Datos de Prueba - Usuarios
-- ISO/IEC 29119 - Test Data Specification
-- Password para todos los usuarios de prueba: "test123"
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu

-- Usuario de Prueba 1
MERGE INTO stockuser KEY(username) VALUES ('testuser1', 'test1@stocksim.com', 'Test', 'User1', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, false, null);

-- Usuario de Prueba 2
MERGE INTO stockuser KEY(username) VALUES ('testuser2', 'test2@stocksim.com', 'Test', 'User2', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, false, null);

-- Usuario para Pruebas de Carga
MERGE INTO stockuser KEY(username) VALUES ('loadtest', 'load@stocksim.com', 'Load', 'Tester', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, false, null);

-- Administrador para Pruebas
MERGE INTO stockuser KEY(username) VALUES ('admintest', 'admin@stocksim.com', 'Admin', 'Test', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', true, true, null);

-- Usuario No Verificado (para pruebas de verificación)
MERGE INTO stockuser KEY(username) VALUES ('unverified', 'unverified@stocksim.com', 'Unverified', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6OQDFpMprkGLM6z4vZQDWNvYLXHGu', false, false, 'TEST123');
