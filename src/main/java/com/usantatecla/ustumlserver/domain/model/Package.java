package com.usantatecla.ustumlserver.domain.model;

import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class Package extends Member {

    private List<Member> members;

    public Package(String name, List<Member> members) {
        super(name);
        this.members = members;
    }

    public void add(Member member) {
        this.members.add(member);
    }

    public List<Member> getMembers() {
        return this.members;
    }

    @Override
    public void accept(Generator generator) {
        generator.visit(this);
    }

}
