-- Inserção de livros para a biblioteca
INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('Dom Casmurro', 'Machado de Assis', 'Companhia das Letras', 1899, '9788574801711', 5, 5);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('O Pequeno Príncipe', 'Antoine de Saint-Exupéry', 'Agir', 1943, '9788574801728', 3, 3);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('1984', 'George Orwell', 'Companhia Editora Nacional', 1949, '9788574801735', 2, 2);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('Cem Anos de Solidão', 'Gabriel García Márquez', 'Record', 1967, '9788574801742', 4, 4);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('Harry Potter e a Pedra Filosofal', 'J.K. Rowling', 'Rocco', 1997, '9788574801759', 6, 6);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('O Senhor dos Anéis', 'J.R.R. Tolkien', 'Martins Fontes', 1954, '9788574801766', 3, 3);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('A Metamorfose', 'Franz Kafka', 'Companhia das Letras', 1915, '9788574801773', 4, 4);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('Crime e Castigo', 'Fiódor Dostoiévski', 'Editora 34', 1866, '9788574801780', 2, 2);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('Orgulho e Preconceito', 'Jane Austen', 'Martin Claret', 1813, '9788574801797', 5, 5);

INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('O Alquimista', 'Paulo Coelho', 'Planeta', 1988, '9788574801803', 7, 7);

-- Livro sem cópias disponíveis para testar o filtro
INSERT INTO books (title, author, publisher, year, isbn, copies, available_copies)
VALUES ('Livro Indisponível', 'Autor Teste', 'Editora Teste', 2023, '9788574801810', 2, 0);
