package br.com.sistema.bot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bot WhatsApp ASB Telecom API - Orquestrador de Atendimento")
                        .version("1.0.0")
                        .description("Backend que substitui o n8n como orquestrador do bot de atendimento via WhatsApp para a ASB Telecom. " +
                                "Recebe eventos do Chatwoot, processa a lógica de atendimento e executa ações nas APIs externas (WhatsApp Cloud API, Chatwoot, Hubsoft).")
                        .contact(new Contact()
                                .name("César Augusto")
                                .email("cesar.augusto.rj1@gmail.com")
                                .url("https://portfolio.cesaraugusto.dev.br/"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
