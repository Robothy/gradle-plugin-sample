package com.robothy;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class HelloPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("Message from hello plugin.");
        project.getTasks().register("hello", SayHelloTask.class);
    }
}
