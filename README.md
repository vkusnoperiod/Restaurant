# Отчет о разработке приложения "Ресторан"

## Общее описание

Приложение "Ресторан" - это программное решение, предназначенное для упрощения и автоматизации процессов заказа блюд в ресторанах. Приложение предоставляет интерфейс как для администраторов ресторана, так и для посетителей, позволяя эффективно управлять заказами, меню и пользователями.

## Классы приложения

### Actions

Класс `Actions` включает в себя основные функции, доступные пользователям приложения. Он содержит методы для просмотра меню, размещения заказов, управления заказами (для администраторов) и другие функции, необходимые для взаимодействия с приложением.

### Admin

Класс `Admin` описывает роль администратора ресторана и его возможности в приложении. Администраторы имеют доступ к расширенному набору функций, включая добавление, удаление и редактирование блюд в меню, управление заказами (подтверждение, отмена, изменение статуса заказа) и просмотр статистики по заказам и пользователям.

### Visitor

Класс `Visitor` описывает роль посетителя ресторана. Посетители могут просматривать текущее меню, размещать заказы на выбранные блюда и просматривать статус своих активных заказов. Также посетители могут оставлять отзывы о блюдах и сервисе ресторана.

### Order

Класс `Order` описывает заказ в ресторане. Каждый заказ содержит информацию о выбранных блюдах, их количестве, общей стоимости заказа, статусе (например, новый, в обработке, выполнен) и информации о посетителе, разместившем заказ.

### Dish

Класс `Dish` представляет собой блюдо из меню ресторана. Для каждого блюда указывается название, описание, цена и информация о доступности блюда для заказа.

### Restaurant

Класс `Restaurant` представляет основные данные о ресторане и меню. Также класс может содержать методы для управления данными ресторана и доступа к статистике.

### User

Класс `User` является общим для всех типов пользователей приложения и содержит основную информацию о пользователе, такую как имя, контактные данные и роль в системе (посетитель или администратор). Данный класс используется для аутентификации и авторизации в приложении.

## Интеграция с MySQL и настройка среды

Приложение использует сборку Gradle с прописанной зависимостью JDBC MySQL для взаимодействия с базой данных. Чтобы использовать приложение, необходимо предварительно загрузить и запустить сервер MySQL.

### Установка MySQL через Homebrew

Для пользователей macOS:

1. Откройте терминал.
2. Если у вас еще не установлен Homebrew, установите его, выполнив следующую команду:

   ```bash
   /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
3. Установите MySQL, используя Homebrew:

	```bash
	brew install mysql
4. Запустите сервер MySQL:

	```bash
	brew services start mysql

### Создание таблиц баз данных
После установки и запуска MySQL выполните следующие команды для создания необходимых таблиц в вашей базе данных:

```bash
CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS dishes (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title TEXT NOT NULL,
    amount INTEGER NOT NULL,
    price INTEGER NOT NULL,
    cook_time INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    total_price INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    dish_name VARCHAR(255),
    quantity INT,
    CONSTRAINT fk_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT UNSIGNED NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
```

