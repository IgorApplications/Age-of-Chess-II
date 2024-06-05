package com.iapp.ageofchess.server.controllers;

import org.springframework.boot.devtools.restart.Restarter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/server")
public class SysAdminController {

    public void restart() {
        Restarter.getInstance().restart();
    }

    public byte[] readData(String absPath) throws IOException {

        try (BufferedInputStream byteInput = new BufferedInputStream(new FileInputStream(absPath))) {
            return byteInput.readAllBytes();
        }
    }
}
