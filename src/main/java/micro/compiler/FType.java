/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2019 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package micro.compiler;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.LiteralExpr;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum FType {

    TMethodDeclaration(com.github.javaparser.ast.body.MethodDeclaration.class),
    TUnaryExpr(com.github.javaparser.ast.expr.UnaryExpr.class),
    TBinaryExpr(com.github.javaparser.ast.expr.BinaryExpr.class),
    TBlockStmt(com.github.javaparser.ast.stmt.BlockStmt.class),
    TIfStmt(com.github.javaparser.ast.stmt.IfStmt.class),
    TConditionalExpr(com.github.javaparser.ast.expr.ConditionalExpr.class),
    TMethodCallExpr(com.github.javaparser.ast.expr.MethodCallExpr.class),
    TLiteralExpr(com.github.javaparser.ast.expr.LiteralExpr.class),
    TUnknown(null);

    public final Class<? extends Node> type;

    FType(Class<? extends Node> type) {
        this.type = type;
    }

    public static FType decode(Class<? extends Node> c){
        return Arrays.stream(FType.values())
                .filter(i -> c.equals(i.type))
                .findAny()
                .orElse(maybeLiteralExpr(c));
    }

    private static FType maybeLiteralExpr(Class<? extends Node> c) {
        if (LiteralExpr.class.isAssignableFrom(c))
            return TLiteralExpr;
        else
            return TUnknown;
    }

    private static final List<Class<? extends Node>> ALL_NODE_CLASSES = new ArrayList<Class<? extends Node>>() {{

        add(com.github.javaparser.ast.Node.class);

        add(com.github.javaparser.ast.body.BodyDeclaration.class);
        add(com.github.javaparser.ast.body.CallableDeclaration.class);
        add(com.github.javaparser.ast.expr.Expression.class);
        add(com.github.javaparser.ast.stmt.Statement.class);
        add(com.github.javaparser.ast.type.Type.class);

        add(com.github.javaparser.ast.expr.AnnotationExpr.class);
        add(com.github.javaparser.ast.type.ReferenceType.class);
        add(com.github.javaparser.ast.body.TypeDeclaration.class);

        add(com.github.javaparser.ast.expr.LiteralExpr.class);
        add(com.github.javaparser.ast.expr.LiteralStringValueExpr.class);
        add(com.github.javaparser.ast.expr.StringLiteralExpr.class);

        add(com.github.javaparser.ast.modules.ModuleDeclaration.class);
        add(com.github.javaparser.ast.modules.ModuleDirective.class);

        //
        add(com.github.javaparser.ast.ArrayCreationLevel.class);
        add(com.github.javaparser.ast.CompilationUnit.class);
        add(com.github.javaparser.ast.ImportDeclaration.class);
        add(com.github.javaparser.ast.Modifier.class);
        add(com.github.javaparser.ast.PackageDeclaration.class);

        //
        add(com.github.javaparser.ast.body.AnnotationDeclaration.class);
        add(com.github.javaparser.ast.body.AnnotationMemberDeclaration.class);
        add(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class);
        add(com.github.javaparser.ast.body.ConstructorDeclaration.class);
        add(com.github.javaparser.ast.body.EnumConstantDeclaration.class);
        add(com.github.javaparser.ast.body.EnumDeclaration.class);
        add(com.github.javaparser.ast.body.FieldDeclaration.class);
        add(com.github.javaparser.ast.body.InitializerDeclaration.class);
        add(com.github.javaparser.ast.body.MethodDeclaration.class);
        add(com.github.javaparser.ast.body.Parameter.class);
        add(com.github.javaparser.ast.body.ReceiverParameter.class);
        add(com.github.javaparser.ast.body.RecordDeclaration.class);

        add(com.github.javaparser.ast.body.VariableDeclarator.class);

        add(com.github.javaparser.ast.comments.Comment.class); // First, as it is the base of other comment types
        add(com.github.javaparser.ast.comments.BlockComment.class);
        add(com.github.javaparser.ast.comments.JavadocComment.class);
        add(com.github.javaparser.ast.comments.LineComment.class);

        add(com.github.javaparser.ast.expr.ArrayAccessExpr.class);
        add(com.github.javaparser.ast.expr.ArrayCreationExpr.class);
        add(com.github.javaparser.ast.expr.ArrayInitializerExpr.class);
        add(com.github.javaparser.ast.expr.AssignExpr.class);
        add(com.github.javaparser.ast.expr.BinaryExpr.class);
        add(com.github.javaparser.ast.expr.BooleanLiteralExpr.class);
        add(com.github.javaparser.ast.expr.CastExpr.class);
        add(com.github.javaparser.ast.expr.CharLiteralExpr.class);
        add(com.github.javaparser.ast.expr.ClassExpr.class);
        add(com.github.javaparser.ast.expr.ConditionalExpr.class);
        add(com.github.javaparser.ast.expr.DoubleLiteralExpr.class);
        add(com.github.javaparser.ast.expr.EnclosedExpr.class);
        add(com.github.javaparser.ast.expr.FieldAccessExpr.class);
        add(com.github.javaparser.ast.expr.InstanceOfExpr.class);
        add(com.github.javaparser.ast.expr.IntegerLiteralExpr.class);
        add(com.github.javaparser.ast.expr.LambdaExpr.class);
        add(com.github.javaparser.ast.expr.LongLiteralExpr.class);
        add(com.github.javaparser.ast.expr.MarkerAnnotationExpr.class);
        add(com.github.javaparser.ast.expr.MemberValuePair.class);
        add(com.github.javaparser.ast.expr.MethodCallExpr.class);
        add(com.github.javaparser.ast.expr.MethodReferenceExpr.class);
        add(com.github.javaparser.ast.expr.NameExpr.class);
        add(com.github.javaparser.ast.expr.Name.class);
        add(com.github.javaparser.ast.expr.NormalAnnotationExpr.class);
        add(com.github.javaparser.ast.expr.NullLiteralExpr.class);
        add(com.github.javaparser.ast.expr.ObjectCreationExpr.class);
        add(com.github.javaparser.ast.expr.PatternExpr.class);
        add(com.github.javaparser.ast.expr.SingleMemberAnnotationExpr.class);
        add(com.github.javaparser.ast.expr.SimpleName.class);
        add(com.github.javaparser.ast.expr.SuperExpr.class);
        add(com.github.javaparser.ast.expr.SwitchExpr.class);
        add(com.github.javaparser.ast.expr.TextBlockLiteralExpr.class);
        add(com.github.javaparser.ast.expr.ThisExpr.class);
        add(com.github.javaparser.ast.expr.TypeExpr.class);
        add(com.github.javaparser.ast.expr.UnaryExpr.class);
        add(com.github.javaparser.ast.expr.VariableDeclarationExpr.class);

        add(com.github.javaparser.ast.stmt.AssertStmt.class);
        add(com.github.javaparser.ast.stmt.BlockStmt.class);
        add(com.github.javaparser.ast.stmt.BreakStmt.class);
        add(com.github.javaparser.ast.stmt.CatchClause.class);
        add(com.github.javaparser.ast.stmt.ContinueStmt.class);
        add(com.github.javaparser.ast.stmt.DoStmt.class);
        add(com.github.javaparser.ast.stmt.EmptyStmt.class);
        add(com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt.class);
        add(com.github.javaparser.ast.stmt.ExpressionStmt.class);
        add(com.github.javaparser.ast.stmt.ForEachStmt.class);
        add(com.github.javaparser.ast.stmt.ForStmt.class);
        add(com.github.javaparser.ast.stmt.IfStmt.class);
        add(com.github.javaparser.ast.stmt.LabeledStmt.class);
        add(com.github.javaparser.ast.stmt.LocalClassDeclarationStmt.class);
        add(com.github.javaparser.ast.stmt.LocalRecordDeclarationStmt.class);
        add(com.github.javaparser.ast.stmt.ReturnStmt.class);
        add(com.github.javaparser.ast.stmt.SwitchEntry.class);
        add(com.github.javaparser.ast.stmt.SwitchStmt.class);
        add(com.github.javaparser.ast.stmt.SynchronizedStmt.class);
        add(com.github.javaparser.ast.stmt.ThrowStmt.class);
        add(com.github.javaparser.ast.stmt.TryStmt.class);
        add(com.github.javaparser.ast.stmt.UnparsableStmt.class);
        add(com.github.javaparser.ast.stmt.WhileStmt.class);
        add(com.github.javaparser.ast.stmt.YieldStmt.class);

        add(com.github.javaparser.ast.type.ArrayType.class);
        add(com.github.javaparser.ast.type.ClassOrInterfaceType.class);
        add(com.github.javaparser.ast.type.IntersectionType.class);
        add(com.github.javaparser.ast.type.PrimitiveType.class);
        add(com.github.javaparser.ast.type.TypeParameter.class);
        add(com.github.javaparser.ast.type.UnionType.class);
        add(com.github.javaparser.ast.type.UnknownType.class);
        add(com.github.javaparser.ast.type.VarType.class);
        add(com.github.javaparser.ast.type.VoidType.class);
        add(com.github.javaparser.ast.type.WildcardType.class);

        add(com.github.javaparser.ast.modules.ModuleExportsDirective.class);
        add(com.github.javaparser.ast.modules.ModuleOpensDirective.class);
        add(com.github.javaparser.ast.modules.ModuleProvidesDirective.class);
        add(com.github.javaparser.ast.modules.ModuleRequiresDirective.class);
        add(com.github.javaparser.ast.modules.ModuleUsesDirective.class);
    }};

}
