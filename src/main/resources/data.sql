-- 1. Заполняем справочные таблицы (БЕЗ указания ID)
merge into rating (rating_name) KEY(rating_name) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');

merge into status (status_name) KEY(status_name) VALUES
('PENDING'),
('CONFIRMED');

merge into genre (genre_name) KEY(genre_name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');