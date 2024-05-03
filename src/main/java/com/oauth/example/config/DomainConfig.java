package com.oauth.example.config;

import com.oauth.example.repository.base.BaseRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Configuration
@EntityScan("com.oauth.example")
@EnableJpaRepositories(basePackages = "com.oauth.example", repositoryBaseClass = BaseRepositoryImpl.class)
@EnableTransactionManagement
public class DomainConfig {
}
