-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EXECUTOR')),
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Создание таблицы проектов
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id)
);

-- Создание таблицы задач
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('TO_DO', 'IN_PROGRESS', 'UNDER_REVIEW', 'DONE')),
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    deadline TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    project_id BIGINT REFERENCES projects(id),
    author_id BIGINT REFERENCES users(id),
    executor_id BIGINT REFERENCES users(id)
);

-- Создание таблицы комментариев
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users(id)
);

-- Создание таблицы вложений
CREATE TABLE IF NOT EXISTS attachments (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    file_path VARCHAR(500),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    task_id BIGINT REFERENCES tasks(id) ON DELETE CASCADE,
    uploaded_by BIGINT REFERENCES users(id)
);

-- Создание таблицы уведомлений
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT REFERENCES tasks(id) ON DELETE SET NULL
);

-- Очистка старых данных
TRUNCATE TABLE tasks CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE projects CASCADE;
TRUNCATE TABLE comments CASCADE;
TRUNCATE TABLE attachments CASCADE;
TRUNCATE TABLE notifications CASCADE;

-- Вставка тестовых данных (для тестирования)
INSERT INTO users (username, password, email, role, full_name) VALUES
('admin', '123', 'admin@company.ru', 'ADMIN', 'Администратор Системы'),
('manager', '123', 'manager@company.ru', 'MANAGER', 'Менеджер Проектов'),
('qa', '123', 'executor2@company.ru', 'EXECUTOR', 'Главный тестировщик'),
('developer', '123', 'executor1@company.ru', 'EXECUTOR', 'Старший разработчик');

INSERT INTO projects (name, description, created_by) VALUES
('Менеджеры', 'Группа управления', 2),
('Обеспечение качества', 'Группа тестирования', 2),
('Техническая поддержка', 'Обслуживание клиентов', 2),
('Обеспечение безопасности', 'Группа инфо-безов', 2),
('Разработка проекта 1', 'Создание системы управления задачами 1', 2),
('Разработка проекта 2', 'Создание системы управления задачами 2', 2);

INSERT INTO tasks (title, description, status, priority, deadline, project_id, author_id, executor_id) VALUES
('Настроить базу данных', 'Настроить базу данных', 'TO_DO', 'LOW', '2026-01-01 12:00:00', 1, 2, 3),
('Разработать интерфейс', 'Создать пользовательский интерфейс', 'IN_PROGRESS', 'MEDIUM', '2026-02-02 18:00:00', 1, 2, 3),
('Протестировать приложение', 'Провести тестирование всех функций', 'TO_DO', 'LOW', '2026-03-03 18:30:00', 1, 2, 4),
('Разработать модальные окна', 'Разработать модальные окна для приложения', 'DONE', 'HIGH', '2025-12-01 19:30:00', 1, 2, 4),
('Анализ деятельности внешних систем', 'Настройка интеграции с внешними системами', 'DONE', 'HIGH', '2025-11-01 20:30:00', 1, 2, 4);