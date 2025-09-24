# Instruções para Configuração do application.properties

**Atenção, professor!**

Antes de rodar o projeto, substitua os valores abaixo no arquivo `src/main/resources/application.properties`:

- `spring.security.oauth2.client.registration.google.client-id`
- `spring.security.oauth2.client.registration.google.client-secret`
- `app.admin-emails`

Exemplo:

```ini
spring.security.oauth2.client.registration.google.client-id=SEU_CLIENT_ID_AQUI
spring.security.oauth2.client.registration.google.client-secret=SEU_CLIENT_SECRET_AQUI
app.admin-emails=seu.email@exemplo.com
