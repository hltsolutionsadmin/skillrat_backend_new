package com.skillrat.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.skillrat.user.domain.User;
import com.skillrat.user.domain.Employee;
import com.skillrat.user.domain.Role;
import com.skillrat.user.domain.ProfileExperience;
import com.skillrat.user.domain.Education;
import com.skillrat.user.domain.UserSkill;
import com.skillrat.user.domain.TitleRecord;
import com.skillrat.user.repo.UserRepository;
import com.skillrat.user.repo.RoleRepository;
import com.skillrat.user.repo.ProfileExperienceRepository;
import com.skillrat.user.repo.UserSkillRepository;
import com.skillrat.user.repo.EducationRepository;
import com.skillrat.user.repo.TitleRecordRepository;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.skillrat.user.client")
@SpringBootApplication
@ConfigurationPropertiesScan
@EntityScan(basePackageClasses = {
        User.class, Employee.class, Role.class, ProfileExperience.class,
        Education.class, UserSkill.class, TitleRecord.class
})
@EnableJpaRepositories(basePackageClasses = {
        UserRepository.class, RoleRepository.class, ProfileExperienceRepository.class,
        UserSkillRepository.class, EducationRepository.class, TitleRecordRepository.class
})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
