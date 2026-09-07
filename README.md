# Trabalho Qualidade e Teste - Grupo 2
Sistema de ERP web desenvolvido em Java com Spring Framework com testes automatizados

## Alunos:
- Lucas Sodré
- Italo Ferreira
- Filipe dos Santos
- Caio Felipe

# Recursos
- Cadastro produtos/clientes/fornecedor
- Controle de estoque
- Gerenciar comandas
- Realizar venda
- Controle de fluxo de caixa
- Controle de pagar e receber
- Venda com cartões
- Gerenciar permissões de usuários por grupos
- Cadastrar novas formas de pagamentos
- Relatórios

# Setup
- Clone o projeto em uma pasta vazia
- Instale Maven no seu computador
- Instale Java JDK8 (Pode ser a versão Temurin JDK 8) e adicione ao seu JAVA_HOME 
- Com o VScode (ou algum terminal similar aberto), rode os seguintes comandos em sequência em um terminal:
    - ``docker-compose down``
    - ``mvn clean``
    - ``export MAVEN_OPTS="-Xmx512m -XX:MaxPermSize=256m"``
    - ``mvn clean package -DskipTests``
    - ``docker-compose up --build`` (Aguarde até o app pdv iniciar)
- Abra um terminal adicional e rode:
    - ``mvn test -DforkCount=0``

# Login administrador
Para logar no sistema, use o usuário "gerente" e a senha "123".

# Tecnologias utilizadas
- Spring Framework 5
- Thymeleaf 3
- MySQL
- Hibernate
- FlyWay
- Docker