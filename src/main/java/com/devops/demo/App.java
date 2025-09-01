package com.devops.demo;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class App {

    @RequestMapping("/")
    String home() {
        return "Hello Abdallah, Welcome to DevOps Industry 🚀 ... Wish U all the best in yourlife learning♾️";
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

