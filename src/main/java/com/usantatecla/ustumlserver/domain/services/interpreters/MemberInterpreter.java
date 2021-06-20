package com.usantatecla.ustumlserver.domain.services.interpreters;

import com.usantatecla.ustumlserver.domain.model.Account;
import com.usantatecla.ustumlserver.domain.model.Member;
import com.usantatecla.ustumlserver.domain.model.Relation;
import com.usantatecla.ustumlserver.domain.services.ServiceException;
import com.usantatecla.ustumlserver.domain.services.parsers.ParserException;
import com.usantatecla.ustumlserver.domain.services.parsers.RelationParser;
import com.usantatecla.ustumlserver.infrastructure.api.dtos.Command;
import com.usantatecla.ustumlserver.infrastructure.api.dtos.ErrorMessage;

import java.util.ArrayList;
import java.util.List;

public abstract class MemberInterpreter {

    protected Account account;
    protected Member member;

    protected MemberInterpreter(Account account, Member member) {
        this.account = account;
        this.member = member;
    }

    public void add(Command command) {
        if (isInvalidAddKeys(command)) {
            throw new ParserException(ErrorMessage.INVALID_COMMAND_KEYS);
        }
    }

    protected boolean isInvalidAddKeys(Command command) {
        return !command.has(Command.MEMBERS) && !command.has(Command.RELATIONS);
    }

    public void delete(Command command) {
        if (isInvalidDeleteKeys(command)) {
            throw new ParserException(ErrorMessage.INVALID_COMMAND_KEYS);
        }
    }

    protected boolean isInvalidDeleteKeys(Command command) {
        return !command.has(Command.MEMBERS) && !command.has(Command.RELATIONS);
    }

    public void modify(Command command) {
        if (isInvalidModifyKeys(command)) {
            throw new ParserException(ErrorMessage.INVALID_COMMAND_KEYS);
        }
    }

    protected boolean isInvalidModifyKeys(Command command) {
        return !command.has(Command.MEMBERS) && !command.has(Command.RELATIONS);
    }

    public void _import(Command command) {
        throw new ServiceException(ErrorMessage.IMPORT_NOT_ALLOWED);
    }

    public Member open(Command command) {
        throw new ServiceException(ErrorMessage.OPEN_NOT_ALLOWED);
    }

    protected void addRelations(Command command) {
        for (Command relationCommand : command.getCommands(Command.RELATIONS)) {
            this.member.add(new RelationParser().get(this.account, relationCommand));
        }
    }

    protected void modifyRelations(Command command) {
        for (Command relationCommand : command.getCommands(Command.RELATIONS)) {
            this.member.modify(new RelationParser().get(this.account, relationCommand),
                    new RelationParser().getModifiedRelation(this.account, relationCommand));
        }
    }

    protected List<Relation> deleteRelations(Command command) {
        List<Relation> relations = new ArrayList<>();
        for (Command relationCommand : command.getCommands(Command.RELATIONS)) {
            String targetRoute = relationCommand.getTargetName();
            Member target = this.account.findRoute(targetRoute);
            if (target == null) {
                throw new ServiceException(ErrorMessage.INVALID_ROUTE, targetRoute);
            }
            relations.add(this.member.deleteRelation(target));
        }
        return relations;
    }

    public Member getMember() {
        return this.member;
    }
}
