-- Tabela de Roles
CREATE TABLE tb_role (
                         id_role BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         nome VARCHAR(255) NOT NULL
);

-- Tabela de Usuário
CREATE TABLE tb_usuario (
                            id_usuario BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            nome VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            senha VARCHAR(255) NOT NULL,
                            CONSTRAINT uk_usuario_email UNIQUE (email)
);

-- Tabela de junção Usuario <-> Role (ManyToMany)
CREATE TABLE id_usuario_role (
                                 id_usuario BIGINT NOT NULL,
                                 id_role BIGINT NOT NULL,
                                 PRIMARY KEY (id_usuario, id_role),
                                 CONSTRAINT fk_usuario_role_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario (id_usuario) ON DELETE CASCADE,
                                 CONSTRAINT fk_usuario_role_role FOREIGN KEY (id_role) REFERENCES tb_role (id_role) ON DELETE CASCADE
);

-- Tabela de Carteira
CREATE TABLE tb_carteira (
                             id_carteira BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             saldo NUMERIC(19, 2) NOT NULL,
                             id_usuario BIGINT NOT NULL,
                             CONSTRAINT fk_carteira_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuario(id_usuario)
);

-- Tabela de Movimentação
CREATE TABLE tb_movimentacao (
                                 id_movimentacao BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 valor_movimentado NUMERIC(19, 2) NOT NULL,
                                 data_de_execucao TIMESTAMP,
                                 tipo_movimentacao VARCHAR(20) NOT NULL,
                                 categoria_movimentacao VARCHAR(30) NOT NULL,
                                 id_carteira BIGINT,
                                 CONSTRAINT fk_movimentacao_carteira FOREIGN KEY (id_carteira) REFERENCES tb_carteira (id_carteira),
                                 CONSTRAINT ck_tipo_movimentacao CHECK (tipo_movimentacao IN ('ENTRADA', 'RETIRADA')),
                                 CONSTRAINT ck_categoria_movimentacao CHECK (categoria_movimentacao IN (
                                                                                                        'ALIMENTACAO', 'MERCADO', 'TRANSPORTE', 'MORADIA', 'CONTAS_FIXAS',
                                                                                                        'SAUDE', 'EDUCACAO', 'LAZER', 'VESTUARIO', 'ASSINATURAS',
                                                                                                        'VIAGEM', 'PET', 'PRESENTES', 'IMPOSTOS', 'INVESTIMENTOS',
                                                                                                        'SALARIO', 'FREELANCE', 'RENDIMENTOS', 'TRANSFERENCIA','DEPOSITO' ,
                                                                                                        'OUTROS'
                                     ))
);

CREATE INDEX idx_movimentacao_carteira ON tb_movimentacao (id_carteira);