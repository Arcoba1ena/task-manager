Руководство пользователя

(Предварительные требования)
Для запуска проекта необходимо установить:
Docker (версия 20.10+)
Docker Compose (версия 2.0+)
Java 17 (для разработки)
Maven (для сборки проекта)

Архитектура проекта

Проект состоит из двух основных компонентов:
Spring Boot приложение (task-manager)
PostgreSQL база данных (в Docker контейнере)

Конфигурация базы данных
Docker Compose конфигурация
# docker-compose.yml
services:
  postgres:
    image: postgres:15
    container_name: taskmanager-postgres
    environment:
      POSTGRES_DB: taskmanager
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
      POSTGRES_HOST_AUTH_METHOD: trust
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
      
Параметры подключения к БД:
Хост: localhost
Порт: 5432
База данных: taskmanager
Пользователь: admin
Пароль: password

Инструкция по запуску
1. Запуск базы данных
# Запуск контейнеров в фоновом режиме
docker-compose up -d

# Проверка статуса контейнеров
docker-compose ps

# Просмотр логов PostgreSQL
docker-compose logs postgres

2. Настройка приложения
Убедитесь, что в application.properties или application.yml указаны корректные настройки подключения к БД:
# Рекомендуемые настройки для application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager
spring.datasource.username=admin
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

3. Сборка и запуск приложения
# Сборка проекта
mvn clean package

# Запуск приложения
mvn spring-boot:run

# Или запуск собранного JAR файла
java -jar target/task-manager-0.0.1-SNAPSHOT.jar

4. Проверка работоспособности
После запуска приложение будет доступно по адресу:
Основное приложение: http://localhost:8080
База данных: localhost:5432
Рекомендуемое приложения для визуального отображения БД (dbeaver)

Тестовые пользователи
После инициализации БД создаются тестовые пользователи:

Логин	Пароль	Роль	Описание
admin	123	ADMIN	Администратор системы
manager	123	MANAGER	Менеджер проектов
developer	123	EXECUTOR	Старший разработчик
qa	123	EXECUTOR	Главный тестировщик

Управление контейнерами:
# Остановка контейнеров с удалением volumes (данные БД будут удалены)
docker-compose down -v

# Остановка контейнеров без удаления данных
docker-compose down

# Перезапуск контейнеров
docker-compose restart

Резервное копирование данных:
# Создание дампа базы данных
docker exec taskmanager-postgres pg_dump -U admin taskmanager > backup.sql

# Восстановление из дампа
docker exec -i taskmanager-postgres psql -U admin taskmanager < backup.sql

Структура базы данных
При инициализации создаются следующие таблицы:
users - пользователи системы
projects - проекты
tasks - задачи
comments - комментарии к задачам
attachments - вложения к задачам
notifications - уведомления

Мониторинг и отладка
Просмотр логов
bash
# Логи приложения
tail -f logs/application.log

# Локи базы данных
docker-compose logs -f postgres

Проверка подключения к БД
bash
# Подключение к БД через psql
docker exec -it taskmanager-postgres psql -U admin -d taskmanager

# Проверка списка таблиц
\dt

Устранение неполадок
Проблема: Порт 5432 занят
bash
# Поиск процесса, использующего порт
sudo lsof -i :5432

# Остановка локального PostgreSQL (если запущен)
sudo systemctl stop postgresql

Проблема: Контейнер не запускается
bash
# Проверка логов контейнера
docker-compose logs postgres

# Принудительная пересборка
docker-compose down -v
docker-compose up --build -d

Проблема: Миграции не применяются
bash
# Принудительный перезапуск с очисткой данных
docker-compose down -v
docker-compose up -d

Дополнительные команды для разработки
bash
# Просмотр используемых ресурсов
docker stats

# Очистка неиспользуемых образов и контейнеров
docker system prune

# Проверка здоровья БД
docker exec taskmanager-postgres pg_isready -U admin

После выполнения всех шагов приложение будет доступно по адресу http://localhost:8080 с предустановленными тестовыми данными.