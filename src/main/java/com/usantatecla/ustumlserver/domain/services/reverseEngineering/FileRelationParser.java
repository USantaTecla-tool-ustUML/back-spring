package com.usantatecla.ustumlserver.domain.services.reverseEngineering;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.usantatecla.ustumlserver.domain.model.Package;
import com.usantatecla.ustumlserver.domain.model.*;
import com.usantatecla.ustumlserver.domain.services.ServiceException;
import com.usantatecla.ustumlserver.infrastructure.api.dtos.ErrorMessage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class FileRelationParser extends VoidVisitorAdapter<Void> {

    private List<Relation> relations;
    private Set<String> useRelationsTargetNames;
    private List<String> imports;
    private Project project;
    private String pakageRoute;

    FileRelationParser() {
        this.relations = new ArrayList<>();
        this.useRelationsTargetNames = new HashSet<>();
        this.imports = new ArrayList<>();
    }

    List<Relation> get(Project project, File file) {
        this.project = project;
        CompilationUnit compilationUnit;
        try {
            compilationUnit = StaticJavaParser.parse(file);
        } catch (FileNotFoundException e) {
            throw new ServiceException(ErrorMessage.FILE_NOT_FOUND, file.getName());
        }
        this.imports = compilationUnit.getImports().stream()
                .map(ImportDeclaration::getNameAsString)
                .collect(Collectors.toList());
        this.setPackageRoute(compilationUnit);
        this.visit(compilationUnit, null);
        this.addUseRelations();
        return this.relations;
    }

    private void setPackageRoute(CompilationUnit compilationUnit) {
        Optional<PackageDeclaration> packageDeclaration = compilationUnit.getPackageDeclaration();
        if (packageDeclaration.isPresent()) {
            this.pakageRoute = packageDeclaration.get().getNameAsString();
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
        this.addInheritanceRelations(declaration.getExtendedTypes());
        this.addInheritanceRelations(declaration.getImplementedTypes());
        this.addRelations(declaration);
    }

    @Override
    public void visit(EnumDeclaration declaration, Void arg) {
        super.visit(declaration, arg);
        this.addInheritanceRelations(declaration.getImplementedTypes());
        this.addRelations(declaration);
    }

    private void addInheritanceRelations(List<ClassOrInterfaceType> targetDeclarations) {
        for (ClassOrInterfaceType declaration : targetDeclarations) {
            Member target = this.getTarget(declaration.getName().toString());
            if (target != null) {
                this.relations.add(new Inheritance(target, ""));
            }
        }
    }

    private Member getTarget(String targetName) {
        Member target;
        if (targetName.contains("\\.")) {
            target = this.project.findRoute(targetName);
        } else {
            String _import = this.getImport(targetName);
            if (_import != null) {
                target = this.project.findRoute(_import);
            } else {
                Package pakage = (Package) this.project.findRoute(this.pakageRoute);
                target = pakage.find(targetName);
                if (target == null) {
                    target = this.getOutsideTarget(targetName);
                }
            }
        }
        return target;
    }

    private String getImport(String name) {
        for (String _import : this.imports) {
            String[] splitImport = _import.split("\\.");
            if (splitImport[splitImport.length - 1].equals(name)) {
                return _import;
            }
        }
        return null;
    }

    private Member getOutsideTarget(String typeName) {
        for (String _import : this.imports) {
            Member member = this.project.findRoute(_import);
            if (member != null && member.isPackage()) {
                Member target = ((Package) member).find(typeName);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    private void addRelations(TypeDeclaration<?> declaration) {
        for (VariableDeclarator variable : this.getVariables(declaration)) {
            if (!this.isInConstructorParams(variable, declaration.getConstructors())) {
                if (this.isInitialized(declaration)) {
                    this.addCompositionRelations(variable);
                }
            } else {
                Type type = variable.getType();
                List<Type> targetTypes = this.getListType(type);
                if (!targetTypes.isEmpty()) {
                    this.addAggregationRelations(targetTypes);
                } else {
                    this.addAssociationRelation(type);
                }
            }
        }
    }

    private List<VariableDeclarator> getVariables(TypeDeclaration<?> declaration) {
        List<VariableDeclarator> variables = new ArrayList<>();
        for (FieldDeclaration field : declaration.getFields()) {
            variables.addAll(field.getVariables());
        }
        return variables;
    }

    private boolean isInConstructorParams(VariableDeclarator variable, List<ConstructorDeclaration> constructors) {
        for (ConstructorDeclaration constructor : constructors) {
            for (Parameter parameter : constructor.getParameters()) {
                if (variable.getTypeAsString().equals(parameter.getTypeAsString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInitialized(TypeDeclaration<?> declaration) {
        for (ConstructorDeclaration constructorDeclaration : declaration.getConstructors()) {
            for (Statement statement : constructorDeclaration.getBody().getStatements()) {
                for (VariableDeclarator variable : this.getVariables(declaration)) {
                    Pattern pattern = Pattern.compile(variable.getNameAsString() + "( )*=(?!=)");
                    if (pattern.matcher(statement.toString()).find()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addCompositionRelations(VariableDeclarator variable) {
        Type type = variable.getType();
        List<Type> targetTypes = this.getListType(type);
        if (!targetTypes.isEmpty()) {
            for (Type targetType : targetTypes) {
                this.addCompositionRelation(targetType);
            }
        } else {
            this.addCompositionRelation(type);
        }
    }

    private List<Type> getListType(Type type) {
        List<Type> types = new ArrayList<>();
        if(type.isArrayType()){
            types.add(type.asArrayType().getComponentType());
        }
        if (type.isClassOrInterfaceType()) {
            Optional<NodeList<Type>> variableTypes = type.asClassOrInterfaceType().getTypeArguments();
            if (variableTypes.isPresent()) {
                types.addAll(variableTypes.get());
            }
        }
        return types;
    }

    private void addCompositionRelation(Type type) {
        if (!type.isPrimitiveType()) {
            Member target = this.getTarget(type.asString());
            if (target != null) {
                this.relations.add(new Composition(target, ""));
            }
        }
    }

    private void addAggregationRelations(List<Type> targetTypes) {
        for (Type targetType : targetTypes) {
            Member target = this.getTarget(targetType.asString());
            if (target != null) {
                this.relations.add(new Aggregation(target, ""));
            }
        }
    }

    private void addAssociationRelation(Type type) {
        Member target = this.getTarget(type.asString());
        if (target != null) {
            this.relations.add(new Association(target, ""));
        }
    }

    @Override
    public void visit(VariableDeclarator variable, Void arg) {
        super.visit(variable, arg);
        this.addUseRelationsTargetNames(variable.getType());
    }

    @Override
    public void visit(Parameter parameter, Void arg) {
        super.visit(parameter, arg);
        this.addUseRelationsTargetNames(parameter.getType());
    }

    @Override
    public void visit(ObjectCreationExpr object, Void arg) {
        super.visit(object, arg);
        this.addUseRelationsTargetNames(object.getType());
    }

    private void addUseRelationsTargetNames(Type type) {
        List<Type> types = this.getListType(type);
        if (!types.isEmpty()) {
            for (Type targetType : types) {
                this.useRelationsTargetNames.add(targetType.asString());
            }
        } else {
            this.useRelationsTargetNames.add(type.asString());
        }
    }

    private boolean isInRelations(String targetName) {
        for (Relation relation : this.relations) {
            if (targetName.equals(relation.getTargetName())) {
                return true;
            }
        }
        return false;
    }

    private void addUseRelations() {
        Set<String> targetNames = new HashSet<>();
        for (String targetName : this.useRelationsTargetNames) {
            if(!this.isInRelations(targetName)){
                targetNames.add(targetName);
            }
        }
        for (String targetName : targetNames) {
            Member target = this.getTarget(targetName);
            if(target != null) {
                this.relations.add(new Use(target, ""));
            }
        }
    }

}
