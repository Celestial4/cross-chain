package com.crosschain;

import com.crosschain.service.MmServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication()
public class CrossChainGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrossChainGatewayApplication.class, args);
        MmServer.init();
    }

    @Component
    public static class AppConfig implements CommandLineRunner {

        @Value("${application.version}")
        private String applicationVersion;

        @Override
        public void run(String... args) throws Exception {
            System.out.println("Application Version: " + applicationVersion);
        }
    }

}