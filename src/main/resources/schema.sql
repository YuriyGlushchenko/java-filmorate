-- Сначала создаем простые справочные таблицы
CREATE TABLE IF NOT EXISTS rating (
    rating_id SERIAL PRIMARY KEY,
    rating_name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS status (
    status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genre (
    genre_id SERIAL PRIMARY KEY,
    genre_name VARCHAR(100) NOT NULL
);

-- Затем создаем основную таблицу users
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE
);

-- Потом создаем film (она ссылается на rating)
CREATE TABLE IF NOT EXISTS film (
    film_id SERIAL PRIMARY KEY,
    film_name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INTEGER,
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES rating(rating_id)
);

-- И только потом создаем таблицы, которые ссылаются на users и film
CREATE TABLE IF NOT EXISTS likes (
    film_id INTEGER,
    user_id INTEGER,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS films_genre (
    film_id INTEGER,
    genre_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friendship (
    friendship_id SERIAL PRIMARY KEY,
    user_id INTEGER,
    friend_id INTEGER,
    status_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES status(status_id),
    UNIQUE (user_id, friend_id)
);