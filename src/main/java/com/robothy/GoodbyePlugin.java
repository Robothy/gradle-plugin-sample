package com.robothy;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GoodbyePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("Message from goodbye plugin.");
        project.getExtensions().add("user", User.class);
        project.getTasks().register("printUser", PrintUserTask.class, task->{
            User user = (User) (project.getExtensions().getByName("user"));
            task.getUser().set(user);
        });
    }
}
