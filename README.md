# Comanda Digital · Backend (BAIANAr)

Backend em **Spring Boot 3 / Java 17** do sistema de pedidos para dark kitchen **BAIANAr**,
conforme a Especificação de Requisitos de Software (SRS v3.2). Substitui o Firebase/Firestore
usado anteriormente no front por uma API REST própria com MySQL.

## Stack

- Java 17, Spring Boot 3.3.4, Maven
- Spring Data JPA + Hibernate, Flyway (migrations versionadas em `db/migration`)
- Spring Security + JWT (jjwt 0.12.6), BCrypt
- Bean Validation, SpringDoc OpenAPI (`/swagger-ui.html`)
- MySQL 8+

## Estrutura de pacotes

```
controller/        endpoints REST (público em controller/, admin em controller/admin/)
service/            toda a lógica de negócio (nunca no controller — RNF04)
repository/         interfaces JPA
entity/ + enums/    classes @Entity e enums de domínio
dto/request|response DTOs — nunca expomos @Entity diretamente (RNF05)
security/           JWT, UserDetails, filtros
config/             SecurityConfig, OpenApiConfig (CORS, Swagger)
exception/          @ControllerAdvice + exceções de negócio
```

## Rodando localmente

1. Suba um MySQL 8 local (ou `docker run -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mysql:8`).
2. Variáveis de ambiente (todas têm default para rodar local sem configurar nada, ver `application.yml`):

   | Variável | Default local | Descrição |
   |---|---|---|
   | `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/comanda_digital?createDatabaseIfNotExist=true` | URL do banco |
   | `SPRING_DATASOURCE_USERNAME` | `root` | usuário do banco |
   | `SPRING_DATASOURCE_PASSWORD` | `root` | senha do banco |
   | `JWT_SECRET` | chave de exemplo (troque em produção) | segredo HS256 |
   | `JWT_EXPIRATION_MS` | `28800000` (8h) | expiração do token |
   | `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | origens liberadas (separar por vírgula) |
   | `PORT` | `8080` | porta do servidor |

3. `mvn spring-boot:run` — o Flyway cria as tabelas e popula o seed automaticamente no primeiro start.
4. Swagger: http://localhost:8080/swagger-ui.html

**⚠️ Não consegui rodar `mvn compile` neste ambiente** (o sandbox de execução bloqueia acesso ao
Maven Central por política de rede). Revisei o código linha a linha, mas **rode `mvn clean compile`
(ou `mvn spring-boot:run`) na sua máquina antes de subir/commitar**, para pegar qualquer erro de
compilação que eu não tenha visto.

## Credenciais de seed (Flyway `V2__seed_data.sql`)

Todas com senha **`senha123`**:

| Email | Perfil |
|---|---|
| `admin@email.com` | ADMIN |
| `gerente@email.com` | GERENTE |
| `cozinha@email.com` | COZINHEIRO |
| `cliente@email.com` | CLIENTE |

O seed cria o **cardápio completo do BAIANAr** (29 pratos, mesmos ids da tela do cliente), as fichas
técnicas de todos, **estoque inicial** de todos os ingredientes (sem isso a validação RN03 recusaria
qualquer pedido) e 2 pedidos de exemplo. Se você já subiu uma versão anterior do backend, apague o banco
antes (`DROP DATABASE comanda_digital;`) — o Flyway recria tudo no próximo start.

## Deploy (Railway)

| Camada | Onde | Observação |
|---|---|---|
| Back-end | Railway (build pelo `Dockerfile` desta pasta) | Deploy from GitHub repo |
| Banco | Railway (plugin MySQL, mesmo projeto) | nunca subir o banco "dentro" do back |
| Front-end | Vercel (já configurado em `vercel.json`) | `environment.prod.ts` deve apontar para a URL do back |

Passo a passo:

1. Railway → **New Project** → **Deploy from GitHub repo** → escolha este repositório (o `Dockerfile` é detectado sozinho).
2. No mesmo projeto: **+ New** → **Database** → **MySQL**.
3. No serviço do back-end → **Variables** → adicione referências ao MySQL (botão *Add Reference*):
   `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`. O `application.yml` já monta a URL
   JDBC a partir delas — não precisa converter nada à mão.
4. Defina também `JWT_SECRET` (string longa e aleatória) e `CORS_ALLOWED_ORIGINS` (URL do front no Vercel,
   ex.: `https://baianar.vercel.app`; separe várias por vírgula, sem espaços e sem barra final).
5. **Settings → Networking → Generate Domain**. Essa URL + `/api` é o `apiUrl` do `environment.prod.ts` do front.
6. Confira em `https://SEU-DOMINIO.up.railway.app/swagger-ui.html`.

A porta é lida da variável `PORT`, que o Railway injeta sozinho.

## Divisão do trabalho (dupla)

- **Claude**: esqueleto do projeto, migrations, segurança/JWT/RBAC, e os módulos administrativos —
  cardápio/ficha técnica/custo (RF-009 a RF-014), estoque (RF-027 a RF-033), fornecedores/compras
  (RF-021 a RF-026), pedidos do lado cozinha/admin com baixa automática e estorno (RF-015 a RF-020),
  dashboard (RF-034 a RF-037) e usuários internos (RF-041).
- **Isabel**: jornada do cliente — ela é quem conhece o front mobile em detalhe, então o ponto de
  partida funcional está em `controller/PedidoClienteController.java` (checkout, histórico,
  acompanhamento de status) e `controller/AuthController.java` + `controller/CardapioController.java`
  (login/cadastro/cardápio público, infraestrutura compartilhada). Esses três arquivos estão
  comentados indicando que são dela para revisar/ajustar — e depois ligar ao front Angular
  (interceptor JWT, telas de carrinho/checkout/status/histórico).

## Limitações conhecidas (para não travar o Sprint Final)

- RF-019 pede filtro por "canal", mas a tabela `pedido` (seção 6 do SRS) não tem essa coluna —
  implementei filtro por `status` e período, que são os campos existentes no modelo de dados.
- RF-020 menciona "timeline de status"; como o modelo de dados não tem uma tabela de histórico de
  status, o detalhe do pedido expõe o status atual + `createdAt`/`updatedAt`. Se sobrar tempo, dá
  pra criar uma tabela `pedido_status_historico` e registrar cada transição.
- Pagamento é 100% simulado (SRS §1.2) — não existe campo de pagamento no pedido.
