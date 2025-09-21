
CREATE TABLE car (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    price NUMERIC(10, 2) NOT NULL CHECK (price > 0),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE person (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 0),
    has_license BOOLEAN DEFAULT FALSE,
    car_id BIGINT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_person_car
        FOREIGN KEY (car_id)
        REFERENCES car(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_person_name ON person(name);
CREATE INDEX idx_person_car_id ON person(car_id);
CREATE INDEX idx_car_brand_model ON car(brand, model);

INSERT INTO car (brand, model, price) VALUES
('Toyota', 'Camry', 25000.00),
('Honda', 'Civic', 22000.00),
('BMW', 'X5', 60000.00),
('Audi', 'A4', 45000.00),
('Ford', 'Focus', 20000.00);

INSERT INTO person (name, age, has_license, car_id) VALUES
('Артур Пирожков', 25, TRUE, 1),
('Дмитрий Носков', 30, TRUE, 2),
('Джонни Дэпп', 22, FALSE, NULL),
('Кира Найтли', 28, TRUE, 3),
('Петр Первый', 35, TRUE, 1),
('Иван Грозный', 26, TRUE, 4),
('Хабиб Нурмагомедов', 19, FALSE, NULL);

SELECT p.name as person_name, p.age, p.has_license,
       c.brand, c.model, c.price
FROM person p
LEFT JOIN car c ON p.car_id = c.id
ORDER BY p.name;