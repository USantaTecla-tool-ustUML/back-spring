package com.usantatecla.ustumlserver.domain.persistence;

import com.usantatecla.ustumlserver.domain.model.Member;
import com.usantatecla.ustumlserver.domain.model.MemberVisitor;
import com.usantatecla.ustumlserver.domain.services.Error;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionPersistence extends MemberVisitor {
    List<Member> read(String sessionId);

    Error update(String sessionId, List<Member> members);

    void delete(String sessionId);
}
