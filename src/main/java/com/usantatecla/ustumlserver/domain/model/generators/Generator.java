package com.usantatecla.ustumlserver.domain.model.generators;

import com.usantatecla.ustumlserver.domain.model.Account;
import com.usantatecla.ustumlserver.domain.model.Member;
import com.usantatecla.ustumlserver.domain.model.Package;
import com.usantatecla.ustumlserver.domain.model.classDiagram.Class;

public abstract class Generator {

    public String generate(Member member) {
        return member.accept(this);
    }

    public abstract String visit(Account account);

    public abstract String visit(Package pakage);

    public abstract String visit(Class clazz);

}
