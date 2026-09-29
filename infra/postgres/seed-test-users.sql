-- =====================================================================
--  Usuarios de prueba de la red social (base de datos: auth_db)
-- ---------------------------------------------------------------------
--  Clave de todos los usuarios:  Password123*
--  (hash BCrypt, costo 10, compatible con BCryptPasswordEncoder de Spring)
--
--  El script es IDEMPOTENTE: si el usuario ya existe (p. ej. lo creó el seeder
--  de auth-service al arrancar) no se duplica ni se modifica.
--
--  Por cada usuario NUEVO se inserta también el evento UserRegistered(seeded=true)
--  en la tabla outbox_event. El Outbox Relay lo publica en Kafka y post-service crea
--  automáticamente la publicación inicial del usuario, que luego aparece en el feed.
--
--  Ejecución (PowerShell, con docker compose levantado):
--    Get-Content infra/postgres/seed-test-users.sql | docker exec -i social-postgres psql -U postgres -d auth_db
--  Ejecución (bash):
--    docker exec -i social-postgres psql -U postgres -d auth_db < infra/postgres/seed-test-users.sql
--  (Reemplace "postgres" por el valor de POSTGRES_USER de su .env)
-- =====================================================================

BEGIN;

WITH test_users (id, username, display_name) AS (
    VALUES
        ('11111111-1111-4111-8111-111111111111'::uuid, 'alice', 'Alice Gómez'),
        ('22222222-2222-4222-8222-222222222222'::uuid, 'bob',   'Bob Martínez'),
        ('33333333-3333-4333-8333-333333333333'::uuid, 'carol', 'Carol Rodríguez'),
        ('44444444-4444-4444-8444-444444444444'::uuid, 'david', 'David López'),
        ('55555555-5555-4555-8555-555555555555'::uuid, 'eva',   'Eva Ramírez'),
        ('66666666-6666-4666-8666-666666666666'::uuid, 'felipe','Felipe Torres')
),
inserted AS (
    INSERT INTO users (id, username, password_hash, display_name, created_at)
    SELECT id,
           username,
           '$2a$10$u2EafXN0AEjD5xOsHrfOwebpRQaXpZPaDnAPtHikPYv7veJfQnpSC', -- Password123*
           display_name,
           now()
    FROM test_users
    ON CONFLICT (username) DO NOTHING
    RETURNING id, username, display_name
),
events AS (
    SELECT gen_random_uuid() AS event_id, i.*
    FROM inserted i
)
INSERT INTO outbox_event (id, aggregate_id, event_type, topic, payload, created_at, attempts)
SELECT e.event_id,
       e.id::text,
       'UserRegistered',
       'social.user.registered.v1',
       json_build_object(
               'eventId', e.event_id,
               'occurredAt', to_char(now() AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.MS"Z"'),
               'userId', e.id,
               'username', e.username,
               'displayName', e.display_name,
               'seeded', true
       )::text,
       now(),
       0
FROM events e;

COMMIT;

-- Lista de usuarios de prueba
SELECT username    AS usuario,
       display_name AS nombre,
       'Password123*' AS clave,
       created_at  AS creado
FROM users
ORDER BY username;
