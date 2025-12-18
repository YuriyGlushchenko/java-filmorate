-- Сначала создаем простые справочные таблицы
create TABLE IF NOT EXISTS rating (
    rating_id SERIAL PRIMARY KEY,
    rating_name VARCHAR(50) NOT NULL
);

create TABLE IF NOT EXISTS status (
    status_id SERIAL PRIMARY KEY,
    status_name VARCHAR(50) NOT NULL
);

create TABLE IF NOT EXISTS genre (
    genre_id SERIAL PRIMARY KEY,
    genre_name VARCHAR(100) NOT NULL
);

create TABLE IF NOT EXISTS director (
    director_id SERIAL PRIMARY KEY,
    director_name VARCHAR(255) NOT NULL UNIQUE
);

-- Затем создаем основную таблицу users
create TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255),
    birthday DATE
);

-- Потом создаем film (она ссылается на rating)
create TABLE IF NOT EXISTS film (
    film_id SERIAL PRIMARY KEY,
    film_name VARCHAR(255) NOT NULL,
    description TEXT,
    release_date DATE,
    duration INTEGER,
    rating_id INTEGER,
    FOREIGN KEY (rating_id) REFERENCES rating(rating_id)
);

-- И только потом создаем таблицы, которые ссылаются на users и film
create TABLE IF NOT EXISTS likes (
    film_id INTEGER,
    user_id INTEGER,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON delete CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS films_genre (
    film_id INTEGER,
    genre_id INTEGER,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON delete CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS friendship (
    friendship_id SERIAL PRIMARY KEY,
    user_id INTEGER,
    friend_id INTEGER,
    status_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users(user_id) ON delete CASCADE,
    FOREIGN KEY (status_id) REFERENCES status(status_id),
    UNIQUE (user_id, friend_id)
);

create TABLE IF NOT EXISTS films_directors (
    film_id INTEGER,
    director_id INTEGER,
    PRIMARY KEY (film_id, director_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON delete CASCADE,
    FOREIGN KEY (director_id) REFERENCES director(director_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS reviews (
    review_id SERIAL PRIMARY KEY,
    content VARCHAR NOT NULL,
    is_positive BOOLEAN NOT NULL DEFAULT false,
    user_id INTEGER NOT NULL,
    film_id INTEGER NOT NULL,
    useful INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE,
    FOREIGN KEY (film_id) REFERENCES film(film_id) ON delete CASCADE
);

create TABLE IF NOT EXISTS review_likes (
    review_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    is_positive BOOLEAN NOT NULL,
    FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON delete CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE,
    PRIMARY KEY (review_id, user_id)
);

create TABLE IF NOT EXISTS feeds
(
    event_id SERIAL PRIMARY KEY,
    create_time BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    user_id INTEGER NOT NULL,
    entity_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON delete CASCADE
)