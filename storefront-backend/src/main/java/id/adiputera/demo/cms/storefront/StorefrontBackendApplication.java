package id.adiputera.demo.cms.storefront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@EntityScan(basePackages = "id.adiputera.demo.cms.entity")
@EnableJpaRepositories(basePackages = "id.adiputera.demo.cms.storefront.repository")
@ComponentScan(basePackages = {"id.adiputera.demo.cms.storefront", "id.adiputera.demo.cms.mapper"})
public class StorefrontBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StorefrontBackendApplication.class, args);
    }

}
