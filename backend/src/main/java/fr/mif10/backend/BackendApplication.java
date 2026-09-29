package fr.mif10.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée de l'application backend.
 * Lance un serveur HTTP (Tomcat) et expose les endpoints REST.
 */
@SpringBootApplication
public class BackendApplication {

    /**
     * Démarrage de l'application.
     * @param args arguments de ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
