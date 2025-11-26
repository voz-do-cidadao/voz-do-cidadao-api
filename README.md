# voz-do-cidadao-api

API para o projeto Voz do Cidadão, que tem como objetivo fornecer uma plataforma para que cidadãos possam registrar suas opiniões, sugestões e reclamações sobre serviços públicos.

### 🚀 Tecnologias
- Kotlin
- Spring Boot
- MongoDB
- Docker

### 📦 execução local

#### execute o docker-compose
* docker-compose up

 Rodar o projeto
* ./gradlew bootRun

### 📧 Configuração de E-mail
Para que o envio de e-mails funcione corretamente, é necessário criar uma chave de acesso (API Key) e adicioná-la nas variáveis de ambiente do projeto.

1. Crie sua chave

Gere uma API Key no serviço de e-mail utilizado (ex.: SendGrid, Gmail SMTP, Mailtrap, etc.).

2. Adicione a chave na API

No backend, crie uma variável de ambiente:

EMAIL_API_KEY=sua_chave_aqui


Ou, se estiver usando o application.yml:

email:
apiKey: ${EMAIL_API_KEY}

3. Use a variável no serviço de envio

Certifique-se de que o serviço que envia os e-mails está lendo essa chave para autenticação.
Exemplo:

val apiKey = env.getProperty("email.apiKey")

📌 Importante

Sem essa chave, nenhum e-mail será enviado.
O aplicativo irá funcionar normalmente, mas as notificações por e-mail não serão disparadas.