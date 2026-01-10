-- Script de Datos de Prueba - Acciones
-- ISO/IEC 29119 - Test Data Specification

-- Apple Inc.
INSERT INTO stock (ticker, name, description)
VALUES ('AAPL', 'Apple Inc.', 'Compañía de tecnología que diseña, fabrica y comercializa productos electrónicos de consumo, software y servicios en línea')
ON CONFLICT (ticker) DO NOTHING;

-- Alphabet Inc. (Google)
INSERT INTO stock (ticker, name, description)
VALUES ('GOOGL', 'Alphabet Inc.', 'Empresa multinacional de tecnología especializada en servicios y productos relacionados con Internet, búsqueda y publicidad')
ON CONFLICT (ticker) DO NOTHING;

-- Microsoft Corporation
INSERT INTO stock (ticker, name, description)
VALUES ('MSFT', 'Microsoft Corporation', 'Corporación tecnológica multinacional que desarrolla, fabrica, licencia, apoya y vende software, servicios y dispositivos')
ON CONFLICT (ticker) DO NOTHING;

-- Amazon.com Inc.
INSERT INTO stock (ticker, name, description)
VALUES ('AMZN', 'Amazon.com Inc.', 'Empresa de comercio electrónico y servicios de computación en la nube con presencia global')
ON CONFLICT (ticker) DO NOTHING;

-- Tesla Inc.
INSERT INTO stock (ticker, name, description)
VALUES ('TSLA', 'Tesla Inc.', 'Compañía automotriz y de almacenamiento de energía que diseña, fabrica y vende vehículos eléctricos')
ON CONFLICT (ticker) DO NOTHING;

-- NVIDIA Corporation
INSERT INTO stock (ticker, name, description)
VALUES ('NVDA', 'NVIDIA Corporation', 'Empresa tecnológica de semiconductores especializada en unidades de procesamiento gráfico (GPU) y tecnología de inteligencia artificial')
ON CONFLICT (ticker) DO NOTHING;

-- Meta Platforms Inc. (Facebook)
INSERT INTO stock (ticker, name, description)
VALUES ('META', 'Meta Platforms Inc.', 'Empresa de tecnología enfocada en redes sociales, realidad virtual y metaverso')
ON CONFLICT (ticker) DO NOTHING;

-- Netflix Inc.
INSERT INTO stock (ticker, name, description)
VALUES ('NFLX', 'Netflix Inc.', 'Servicio de streaming de entretenimiento que ofrece películas, series y contenido original')
ON CONFLICT (ticker) DO NOTHING;

-- PayPal Holdings Inc.
INSERT INTO stock (ticker, name, description)
VALUES ('PYPL', 'PayPal Holdings Inc.', 'Plataforma de pagos digitales que permite transferencias electrónicas de dinero a través de Internet')
ON CONFLICT (ticker) DO NOTHING;

-- Adobe Inc.
INSERT INTO stock (ticker, name, description)
VALUES ('ADBE', 'Adobe Inc.', 'Empresa de software multinacional especializada en soluciones de creatividad digital, marketing y gestión de documentos')
ON CONFLICT (ticker) DO NOTHING;
