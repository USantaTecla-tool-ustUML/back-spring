package com.usantatecla.ustumlserver.domain.model;

import com.usantatecla.ustumlserver.domain.model.generators.DirectoryTreeGenerator;
import com.usantatecla.ustumlserver.domain.model.generators.Generator;
import com.usantatecla.ustumlserver.infrastructure.api.dtos.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@SuperBuilder
@Data
@AllArgsConstructor
public class Account extends Member {

    private static final String UST_NAME = "account:";
    private String email;
    private String password;
    private Role role;
    private List<Project> projects;

    public Account() {
        this.projects = new ArrayList<>();
    }

    public void add(Project project) {
        if (this.find(project.getName()) != null) {
            throw new ModelException(ErrorMessage.MEMBER_ALREADY_EXISTS, project.getName());
        }
        this.projects.add(project);
    }

    public Project find(String name) {
        for (Project project : this.projects) {
            if (project.getName().equals(name)) {
                return project;
            }
        }
        return null;
    }

    public Member findRoute(Stack<String> route) {
        Project project = this.find(route.pop());
        if (!route.isEmpty() && project != null) {
            return project.findRoute(route);
        }
        return project;
    }

    @Override
    public String accept(Generator generator) {
        return generator.visit(this);
    }

    @Override
    public String accept(DirectoryTreeGenerator directoryTreeGenerator) {
        return directoryTreeGenerator.visit(this);
    }

    @Override
    public void accept(MemberVisitor memberVisitor) {
        memberVisitor.visit(this);
    }

    @Override
    public String getUstName() {
        return Account.UST_NAME;
    }

}
