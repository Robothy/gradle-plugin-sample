package com.robothy;

import org.gradle.api.provider.Property;

public interface User {

    Property<String> getName();

    Property<String> getCountry();

}
