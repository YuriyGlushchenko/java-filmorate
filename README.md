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

- Получение общих друзей двух пользователей.
```SQL
SELECT u.* 
FROM friendship f1
JOIN friendship f2 ON f1.friend_id = f2.friend_id
JOIN users u ON f1.friend_id = u.user_id
WHERE f1.user_id = ? 
  AND f2.user_id = ?
  AND f1.status_id = ?
  AND f2.status_id = ?
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