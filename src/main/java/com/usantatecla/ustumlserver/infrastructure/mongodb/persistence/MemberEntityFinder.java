package com.usantatecla.ustumlserver.infrastructure.mongodb.persistence;

import com.usantatecla.ustumlserver.domain.model.Class;
import com.usantatecla.ustumlserver.domain.model.Package;
import com.usantatecla.ustumlserver.domain.model.*;
import com.usantatecla.ustumlserver.infrastructure.api.dtos.ErrorMessage;
import com.usantatecla.ustumlserver.infrastructure.mongodb.daos.AccountDao;
import com.usantatecla.ustumlserver.infrastructure.mongodb.daos.ClassDao;
import com.usantatecla.ustumlserver.infrastructure.mongodb.daos.PackageDao;
import com.usantatecla.ustumlserver.infrastructure.mongodb.entities.AccountEntity;
import com.usantatecla.ustumlserver.infrastructure.mongodb.entities.ClassEntity;
import com.usantatecla.ustumlserver.infrastructure.mongodb.entities.MemberEntity;
import com.usantatecla.ustumlserver.infrastructure.mongodb.entities.PackageEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

class MemberEntityFinder extends WithDaosPersistence implements MemberVisitor {

    private MemberEntity memberEntity;

    MemberEntityFinder(AccountDao accountDao, PackageDao packageDao, ClassDao classDao) {
        super(accountDao, packageDao, classDao);
    }

    MemberEntity find(Member member) {
        member.accept(this);
        return this.memberEntity;
    }

    @Override
    public void visit(Account account) {
        Optional<AccountEntity> accountEntity = this.accountDao.findById(account.getId());
        if (accountEntity.isEmpty()) {
            throw new PersistenceException(ErrorMessage.MEMBER_NOT_FOUND, account.getName());
        }
        this.memberEntity = accountEntity.get();
    }

    @Override
    public void visit(Package pakage) {
        Optional<PackageEntity> packageEntity = this.packageDao.findById(pakage.getId());
        if (packageEntity.isEmpty()) {
            throw new PersistenceException(ErrorMessage.MEMBER_NOT_FOUND, pakage.getName());
        }
        this.memberEntity = packageEntity.get();
    }

    @Override
    public void visit(Class clazz) {
        Optional<ClassEntity> classEntity = this.classDao.findById(clazz.getId());
        if (classEntity.isEmpty()) {
            throw new PersistenceException(ErrorMessage.MEMBER_NOT_FOUND, clazz.getName());
        }
        this.memberEntity = classEntity.get();
    }

}
