package com.studentteambeta.configserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        System.out.println("Working dir: " + System.getProperty("user.dir"));
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}