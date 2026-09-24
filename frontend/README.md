# Frontend Space Mining

Painel em HTML, CSS e JavaScript, com servidor Node.js e sem dependências externas.
Requer Node.js 22 ou superior. Nesta pasta:

```sh
npm start
```

Acesse http://localhost:3000. Os serviços Java devem estar em execução.
Selecione um dos seis comandos e clique em enviar. Os botões ficam desabilitados
durante o envio, e as contagens são consultadas a cada três segundos. O histórico
mostra os últimos 20 envios desta sessão e é apagado ao recarregar a página.

O servidor encaminha `POST /api/command` para `http://localhost:8080/command` e
`GET /api/counts` para `http://localhost:8082/commands/counts`, evitando CORS.
As URLs podem ser configuradas com `COMMAND_SERVICE_URL` e `MINING_SERVICE_URL`.
`PORT` altera a porta do frontend (padrão 3000).

Não abra `public/index.html` diretamente: use o servidor para acessar as APIs.
O status online se refere à leitura das contagens, não à disponibilidade de toda
a operação. “Na fila” indica aceite do envio, não execução individual do comando.

Para subir junto dos serviços via Docker, execute na raiz do repositório:

```sh
./gradlew bootJar
docker compose up -d --build
```
