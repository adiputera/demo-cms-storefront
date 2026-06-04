package com.demo.cms.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.demo.cms.admin", "com.demo.cms.mapper"})
@EntityScan(basePackages = "com.demo.cms.entity")
@EnableJpaRepositories(basePackages = "com.demo.cms.admin.repository")
@EnableCaching
public class CmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CmsBackendApplication.class, args);
    }

}
