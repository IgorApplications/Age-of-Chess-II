package com.iapp.ageofchess.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.devtools.restart.Restarter;

@SpringBootApplication
public class ChessServer {

    public static void main(String[] args) {
        SpringApplication.run(ChessServer.class, args);
    }
}
