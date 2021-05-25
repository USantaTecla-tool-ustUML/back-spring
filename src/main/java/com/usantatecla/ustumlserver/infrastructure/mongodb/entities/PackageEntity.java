package com.usantatecla.ustumlserver.infrastructure.mongodb.entities;

import com.usantatecla.ustumlserver.domain.model.Member;
import com.usantatecla.ustumlserver.domain.model.Package;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
@Document
public class PackageEntity extends MemberEntity {

    @DBRef(lazy = true)
    protected List<MemberEntity> memberEntities;

    public PackageEntity(Package pakage) {
        super(pakage.getId(), pakage.getName());
        this.memberEntities = new ArrayList<>();
    }

    public Package toPackage() {
        Package pakage = new Package();
        BeanUtils.copyProperties(this, pakage);
        List<Member> members = new ArrayList<>();
        if (Objects.nonNull(this.getMemberEntities())) {
            for (MemberEntity memberEntity : this.getMemberEntities()) {
                members.add(memberEntity.toMember());
            }
        }
        pakage.setMembers(members);
        return pakage;
    }

    public void add(MemberEntity memberEntity) {
        if (this.memberEntities == null) {
            this.memberEntities = new ArrayList<>();
        }
        this.memberEntities.add(memberEntity);
    }

    @Override
    protected Member toMember() {
        return this.toPackage();
    }

}
