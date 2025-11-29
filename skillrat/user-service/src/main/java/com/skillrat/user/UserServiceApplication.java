package com.skillrat.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import com.skillrat.user.domain.Education;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.ProfileExperience;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.TitleRecord;
import com.skillrat.user.domain.User;
import com.skillrat.user.domain.UserSkill;
import com.skillrat.user.organisation.domain.B2BUnit;
import com.skillrat.user.organisation.domain.B2BUnitStatus;
import com.skillrat.user.organisation.domain.B2BUnitType;
import com.skillrat.user.organisation.repo.B2BUnitRepository;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.skillrat.user.client")
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
@EntityScan(basePackageClasses = {
        User.class, Employee.class, Role.class, ProfileExperience.class,
        Education.class, UserSkill.class, TitleRecord.class, B2BUnit.class
})
@EnableJpaRepositories(basePackages = {
        "com.skillrat.user.repo",
        "com.skillrat.user.organisation.repo"
})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(B2BUnitRepository b2bUnitRepository) {
        return args -> {
            if (b2bUnitRepository.count() == 0) {
                B2BUnit defaultUnit = new B2BUnit();
                defaultUnit.setName("Default Organization");
                defaultUnit.setType(B2BUnitType.ORGANIZATION);
                defaultUnit.setStatus(B2BUnitStatus.APPROVED);
                b2bUnitRepository.save(defaultUnit);
                System.out.println("Created default B2BUnit with ID: " + defaultUnit.getId());
            }
        };
    }
}
