package com.usantatecla.ustumlserver.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UstUMLGeneratorTest extends GeneratorTest {

    @BeforeEach
    void beforeEach() {
        this.generator = new UstUMLGenerator();
    }

    @Test
    void testGivenGeneratorWhenVisitClassThenReturn() {
        String uml = "class: Name\n" +
                "modifiers: public abstract\n" +
                "members:\n" +
                "  - member: private Type name\n" +
                "  - member: package static Type name\n" +
                "  - member: package Type name()\n" +
                "  - member: public Type name(Type name)\n" +
                "  - member: private abstract Type name(Type name, Type name)";
        assertThat(this.generator.visit(this.clazz), is(uml));
    }

    @Test
    void testGivenGeneratorWhenVisitPackageThenReturn() {
        String uml = "package: name\n" +
                "members:\n" +
                "  - class: Name\n" +
                "    modifiers: public abstract\n" +
                "    members:\n" +
                "      - member: private Type name\n" +
                "      - member: package static Type name\n" +
                "      - member: package Type name()\n" +
                "      - member: public Type name(Type name)\n" +
                "      - member: private abstract Type name(Type name, Type name)";
        assertThat(this.generator.visit(new Package("name", Collections.singletonList(this.clazz))), is(uml));
    }

}
