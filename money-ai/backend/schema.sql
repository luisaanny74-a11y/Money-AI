-- Opcional: o Hibernate cria a tabela automaticamente (ddl-auto=update).
CREATE DATABASE IF NOT EXISTS money_ai CHARACTER SET utf8mb4;
USE money_ai;
CREATE TABLE IF NOT EXISTS transactions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(120)  NOT NULL,
    amount      DECIMAL(12,2) NOT NULL,
    type        VARCHAR(10)   NOT NULL,
    category    VARCHAR(60)   NOT NULL,
    date        DATE          NOT NULL,
    created_at  DATETIME      NOT NULL
);
