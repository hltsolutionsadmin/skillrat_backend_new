package com.skillrat.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "skillrat.points")
public class WalletPointsProperties {
    private Category education = new Category();
    private Category project = new Category();
    private Category internship = new Category();
    private Category skill = new Category();
    private Category title = new Category();

    public Category getEducation() { return education; }
    public void setEducation(Category education) { this.education = education; }
    public Category getProject() { return project; }
    public void setProject(Category project) { this.project = project; }
    public Category getInternship() { return internship; }
    public void setInternship(Category internship) { this.internship = internship; }
    public Category getSkill() { return skill; }
    public void setSkill(Category skill) { this.skill = skill; }
    public Category getTitle() { return title; }
    public void setTitle(Category title) { this.title = title; }

    public static class Category {
        private int perAction = 0;
        private int maxPerCategory = 0;
        public int getPerAction() { return perAction; }
        public void setPerAction(int perAction) { this.perAction = perAction; }
        public int getMaxPerCategory() { return maxPerCategory; }
        public void setMaxPerCategory(int maxPerCategory) { this.maxPerCategory = maxPerCategory; }
    }
}
