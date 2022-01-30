DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS company;

CREATE TABLE IF NOT EXISTS company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

INSERT INTO company (id, name) VALUES(1, 'Apple');
INSERT INTO company (id, name) VALUES(2, 'IBM');
INSERT INTO company (id, name) VALUES(3, 'Yandex');
INSERT INTO company (id, name) VALUES(4, 'Mail.ru');
INSERT INTO company (id, name) VALUES(5, 'Ozon');
INSERT INTO company (id, name) VALUES(6, 'Sber');

INSERT INTO person (id, name, company_id) VALUES(1, 'Computer Geniys', 1);
INSERT INTO person (id, name, company_id) VALUES(2, 'Programmer', 1);
INSERT INTO person (id, name, company_id) VALUES(3, 'Disigner', 2);
INSERT INTO person (id, name, company_id) VALUES(4, 'Soft Manager', 3);
INSERT INTO person (id, name, company_id) VALUES(5, 'Big Boy', 4);
INSERT INTO person (id, name, company_id) VALUES(6, 'Clever girl', 5);
INSERT INTO person (id, name, company_id) VALUES(7, 'Good news', 6);
INSERT INTO person (id, name, company_id) VALUES(8, 'Foxy smile', 6);
INSERT INTO person (id, name, company_id) VALUES(9, 'Child', 6);
INSERT INTO person (id, name, company_id) VALUES(10, 'Magican', 2);
INSERT INTO person (id, name, company_id) VALUES(11, 'Teacher', 2);


--  1. В одном запросе получить
-- - имена всех person, которые не состоят в компании с id = 5;
-- - название компании для каждого человека.

SELECT pers.id, pers.name, com.name AS company
FROM person AS pers
INNER JOIN company AS com
ON pers.company_id!=5 AND pers.company_id = com.id;

-- 2. Необходимо выбрать название компании с максимальным количеством человек + количество человек в этой компании
-- (нужно учесть, что таких компаний может быть несколько).

SELECT c.name, COUNT(*)
FROM person AS p
INNER JOIN company AS c
ON p.company_id = c.id
GROUP BY c.name
HAVING COUNT(*) = (SELECT MAX(per.cnt) FROM (SELECT COUNT(*) AS cnt FROM person GROUP BY company_id) AS per);


