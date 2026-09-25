-- Roles table
CREATE TABLE tb_role (
                         role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name VARCHAR(255) NOT NULL
);

-- User table
CREATE TABLE tb_user (
                         user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255) NOT NULL,
                         password VARCHAR(255) NOT NULL,
                         creation_date TIMESTAMP NOT NULL,
                         update_date TIMESTAMP NOT NULL,
                         first_login BOOLEAN DEFAULT TRUE,
                         CONSTRAINT uk_user_email UNIQUE (email)
);

-- Join table User <-> Role (ManyToMany)
CREATE TABLE user_role (
                           user_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           PRIMARY KEY (user_id, role_id),
                           CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES tb_user (user_id) ON DELETE CASCADE,
                           CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES tb_role (role_id) ON DELETE CASCADE
);

-- Wallet table
CREATE TABLE tb_wallet (
                           wallet_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                           balance NUMERIC(19, 2) NOT NULL,
                           user_id BIGINT NOT NULL,
                           CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

-- Movement table
CREATE TABLE tb_movement (
                             movement_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             description VARCHAR(255),
                             moved_amount NUMERIC(19, 2) NOT NULL,
                             execution_date TIMESTAMP,
                             movement_type VARCHAR(20) NOT NULL,
                             movement_category VARCHAR(30) NOT NULL,
                             wallet_id BIGINT,
                             CONSTRAINT fk_movement_wallet FOREIGN KEY (wallet_id) REFERENCES tb_wallet (wallet_id),
                             CONSTRAINT ck_movement_type CHECK (movement_type IN ('ENTRADA', 'RETIRADA')),
                             CONSTRAINT ck_movement_category CHECK (movement_category IN (
                                                                                          'ALIMENTACAO', 'MERCADO', 'TRANSPORTE', 'MORADIA', 'CONTAS_FIXAS',
                                                                                          'SAUDE', 'EDUCACAO', 'LAZER', 'VESTUARIO', 'ASSINATURAS',
                                                                                          'VIAGEM', 'PET', 'PRESENTES', 'INVESTIMENTOS',
                                                                                          'SALARIO', 'RENDIMENTOS', 'TRANSFERENCIA','DEPOSITO' ,
                                                                                          'OUTROS'
                                 ))
);

CREATE INDEX idx_movement_wallet ON tb_movement (wallet_id);