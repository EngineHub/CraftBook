package com.sk89q.craftbook.ic.set;

import com.sk89q.craftbook.ic.core.ICFamily;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ICSetTrigger {
    String name();
    String title();
    String signTitle();
    Class<? extends ICFamily> family();
}
