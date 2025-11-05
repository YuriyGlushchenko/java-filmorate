# java-filmorate

![ER-диаграмма filmorate](/filmorate_ER_diagram.jpg)
## Пояснения: 
- В таблице films_genre составной первиный ключ, чтобы не дублировались жанры для одного фильма
- Аналогично в таблице likes составной первичный ключ, чтобы один пользователь мог поставить только один лайк
- В таблице friendship сочетания полей user_id и friend_id должны быть уникальными ( чтобы не было задвоения друзей), эти поля помечены U1



# Примеры запросов
- Получение пользователя по id:
```sql
SELECT *
FROM users
WHERE id = 1;
```

- Получение списка всех друзей пользователя по id.    
Если для каждой пары хранится по 2 записи (т.е. учитывается направление дружбы)
```sql
SELECT 
	f.user_id AS user_id,
	f.friend_id AS friend_id,
	u."name" AS friend_name, 
	u.email AS frien_email ,
	u.login AS friend_login,
	s."name" AS friendship_status 
FROM friendship f
JOIN users u ON f.friend_id = u.user_id 
JOIN status s ON f.status_id = s.status_id 
WHERE f.user_id = 1 AND s.name = 'CONFIRMED'
```  
Если для каждой пары друзей будет храниться только одна запись в таблице friendship:

```SQL
SELECT 
	f.user_id AS user_id,
	f.friend_id AS friend_id,
	u."name" AS friend_name, 
	u.email AS frien_email ,
	u.login AS friend_login,
	s."name" AS friendship_status 
FROM friendship f
JOIN users u ON f.friend_id = u.user_id 
JOIN status s ON f.status_id = s.status_id 
WHERE f.user_id = 1 AND s.name = 'CONFIRMED'

UNION

SELECT 
	f.friend_id  AS user_id,
	f.user_id AS friend_id,
	u."name" AS friend_name, 
	u.email AS frien_email ,
	u.login AS friend_login,
	s."name" AS friendship_status
FROM friendship f
JOIN users u ON f.user_id  = u.user_id 
JOIN status s ON f.status_id = s.status_id 
WHERE f.friend_id  = 1 AND s.name = 'CONFIRMED'
```
- Получение общих друзей двух пользователей.
```SQL
WITH user1_friends AS (
    SELECT friend_id 
    FROM friendship f
    JOIN status s ON f.status_id = s.id
    WHERE f.user_id = 1 AND s.name = 'CONFIRMED'
    UNION
    SELECT user_id as friend_id
    FROM friendship f
    JOIN status s ON f.status_id = s.id
    WHERE f.friend_id = 1 AND s.name = 'CONFIRMED'
),
user2_friends AS (
    SELECT friend_id 
    FROM friendship f
    JOIN status s ON f.status_id = s.id
    WHERE f.user_id = 2 AND s.name = 'CONFIRMED'
    UNION
    SELECT user_id as friend_id
    FROM friendship f
    JOIN status s ON f.status_id = s.id
    WHERE f.friend_id = 2 AND s.name = 'CONFIRMED'
)
SELECT 
    u.id AS common_friend_id,
    u.name AS common_friend_name, 
    u.email AS common_friend_email,
    u.login AS common_friend_login
FROM users u
JOIN user1_friends f1 ON u.id = f1.friend_id
JOIN user2_friends f2 ON u.id = f2.friend_id
WHERE u.id NOT IN (1, 2);
```
- Получить 10 самых популярных фильмов:
```SQL
SELECT f.name AS film_name,
	COUNT (l.user_id) AS likes_count
FROM film AS f
JOIN likes AS l ON f.film_id = l.film_id
GROUP BY f.name
ORDER BY likes_count DESC
LIMIT 10
```