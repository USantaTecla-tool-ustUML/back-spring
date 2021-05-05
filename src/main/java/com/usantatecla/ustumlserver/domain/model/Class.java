package com.usantatecla.ustumlserver.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Class extends Member {

    private List<Modifier> modifiers;
    private List<Attribute> attributes;
    private List<Method> methods;

    public Class(String name, List<Modifier> modifiers, List<Attribute> attributes) {
        super(name);
        if (modifiers.isEmpty()) {
            this.modifiers = Collections.singletonList(Modifier.PACKAGE);
        } else this.modifiers = modifiers;
        this.attributes = attributes;
        this.methods = new ArrayList<>();
    }

    @Override
    public void accept(Generator generator) {
        generator.visit(this);
    }

}
