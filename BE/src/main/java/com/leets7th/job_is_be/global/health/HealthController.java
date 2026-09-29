package com.leets7th.job_is_be.global.health;

import com.leets7th.job_is_be.global.health.docs.HealthControllerDocs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthControllerDocs {

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

}
