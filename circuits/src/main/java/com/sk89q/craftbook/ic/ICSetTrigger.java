package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.ic.families.FamilySISO;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ICSetTrigger {
    String name();
    String title();
    String signTitle();
    Class<? extends ICFamily> family();
}
