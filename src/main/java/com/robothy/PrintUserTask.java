package com.robothy;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

public abstract class PrintUserTask extends DefaultTask {

    @Input
    public abstract Property<User> getUser();

    @TaskAction
    public void print() {
        User user = getUser().get();
        System.out.println("User[" + user.getName().get() + ", " + user.getCountry().get() + "]");
    }

}
