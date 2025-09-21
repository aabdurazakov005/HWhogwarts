
SELECT s.name as student_name,
       s.age as student_age,
       f.name as faculty_name,
       f.color as faculty_color
FROM student s
LEFT JOIN faculty f ON s.faculty_id = f.id
ORDER BY s.name;

SELECT s.name as student_name,
       s.age as student_age,
       a.file_path as avatar_path,
       a.file_size as avatar_size,
       a.media_type as avatar_type
FROM student s
INNER JOIN avatar a ON s.id = a.student_id
ORDER BY s.name;

SELECT s.name as student_name,
       s.age as student_age
FROM student s
LEFT JOIN avatar a ON s.id = a.student_id
WHERE a.id IS NULL
ORDER BY s.name;

SELECT f.name as faculty_name,
       f.color as faculty_color,
       COUNT(s.id) as student_count
FROM faculty f
LEFT JOIN student s ON f.id = s.faculty_id
GROUP BY f.id, f.name, f.color
ORDER BY student_count DESC;

SELECT f.name as faculty_name,
       AVG(s.age) as average_age,
       COUNT(s.id) as student_count
FROM faculty f
LEFT JOIN student s ON f.id = s.faculty_id
GROUP BY f.id, f.name
HAVING COUNT(s.id) > 0
ORDER BY average_age DESC;