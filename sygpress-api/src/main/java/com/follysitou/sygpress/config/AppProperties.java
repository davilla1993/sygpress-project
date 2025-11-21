package com.follysitou.sygpress.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propriétés de configuration de l'application.
 * Ces propriétés sont injectées depuis les variables d'environnement
 * et peuvent être utilisées partout dans l'application.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    /**
     * URL de base de l'application (frontend).
     * Exemple: https://sygpress.votredomaine.com
     * Utilisé pour générer des liens absolus dans les emails, PDFs, etc.
     */
    private String baseUrl = "http://localhost:8080";

    /**
     * URL du serveur API (backend).
     * En mode monorepo, c'est généralement la même que baseUrl.
     * Exemple: https://sygpress.votredomaine.com
     */
    private String serverUrl = "http://localhost:8080";
}
