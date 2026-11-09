-- Endereços de entrega salvos pelo cliente (tela "Meus Endereços").
CREATE TABLE endereco_cliente (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id    BIGINT       NOT NULL,
    nome          VARCHAR(60)  NOT NULL,
    cep           VARCHAR(9)   NOT NULL,
    rua           VARCHAR(150) NOT NULL,
    numero        VARCHAR(20)  NOT NULL,
    complemento   VARCHAR(100),
    bairro        VARCHAR(100) NOT NULL,
    cidade        VARCHAR(100) NOT NULL,
    estado        VARCHAR(2)   NOT NULL,
    padrao        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_endereco_cliente_usuario FOREIGN KEY (cliente_id) REFERENCES usuario(id) ON DELETE CASCADE,
    INDEX idx_endereco_cliente_cliente (cliente_id)
);
