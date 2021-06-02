package com.usantatecla.ustumlserver.domain.model;

import java.util.StringJoiner;

public class PlantUMLGenerator extends Generator {

    private int deepLevel = 0;

    @Override
    String visit(Account account) {
        StringJoiner stringJoiner = new StringJoiner(Generator.EOL_CHAR);
        if(++this.deepLevel == 1) {
            for (Member member : account.getProjects()) {
                stringJoiner.add(this.tabulate(member.accept(this)));
                for (Relation relation : member.getRelations()) {
                    stringJoiner.add(this.tabulate(relation.accept(this, member)));
                }
            }
        }
        return stringJoiner.toString();
    }

    @Override
    String visit(Package pakage) {
        StringJoiner stringJoiner = new StringJoiner(Generator.EOL_CHAR);
        stringJoiner.merge(new StringJoiner(" ").add("package").add(pakage.getName()).add("{"));
        if(++this.deepLevel == 1) {
            for (Member member : pakage.getMembers()) {
                stringJoiner.add(Generator.TAB_CHAR + this.tabulate(member.accept(this)));
                for (Relation relation : member.getRelations()) {
                    stringJoiner.add(this.tabulate(relation.accept(this, member)));
                }
            }
        }
        return stringJoiner.add("}").toString();
    }

    @Override
    String visit(Class clazz) {
        StringJoiner stringJoiner = new StringJoiner(Generator.EOL_CHAR);
        StringJoiner classHeaderJoiner = new StringJoiner(" ");
        for (Modifier modifier : clazz.getModifiers()) {
            if (!modifier.isVisibility()) {
                classHeaderJoiner.add(modifier.getPlantUML());
            }
        }
        classHeaderJoiner.add("class").add(clazz.getName()).add("{");
        stringJoiner.merge(classHeaderJoiner);
        for (Attribute attribute : clazz.getAttributes()) {
            stringJoiner.add(Generator.TAB_CHAR + this.getUML(attribute));
        }
        for (Method method : clazz.getMethods()) {
            stringJoiner.add(Generator.TAB_CHAR + this.getUML(method));
        }
        return stringJoiner.add("}").toString();
    }

    @Override
    String visit(Use use, Member origin) {
        String plantUMLUse = origin.getName() + " .down.> " + use.getTarget().getName();
        if(!use.getRole().equals("")) {
            plantUMLUse += " : " + use.getRole();
        }
        return plantUMLUse;
    }

    @Override
    String getUML(Modifier modifier) {
        if (modifier != Modifier.FINAL) {
            if (!modifier.isVisibility()) {
                return "{" + modifier.getPlantUML() + "}";
            }
            return modifier.getPlantUML();
        }
        return "";
    }

    @Override
    String getUML(String name, String type) {
        return new StringJoiner(" ").add(name + ":").add(type).toString();
    }

    @Override
    String getUML(Method method) {
        return new StringJoiner(" ")
                .add(this.getModifiersUML(method.getModifiers()))
                .add(method.getName()).toString() +
                this.getParametersUML(method.getParameters()) + ": " +
                method.getType();
    }

}
