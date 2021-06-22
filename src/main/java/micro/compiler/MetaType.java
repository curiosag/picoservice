package micro.compiler;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;

import javax.management.ValueExp;

public enum MetaType {

    Unknown, TExpression, TExpressionStmt, TIfStmt, TVariableDeclarationExpr, TBlockStmt, TReturnStmt, TNameExpr, TValueExp,
    TBinaryExpr;

    public static MetaType decode(Object o) {
        if (o instanceof Expression)
            return TExpression;
        if (o instanceof ExpressionStmt)
            return TExpressionStmt;
        if (o instanceof IfStmt)
            return TIfStmt;
        if (o instanceof VariableDeclarationExpr)
            return TVariableDeclarationExpr;
        if (o instanceof BlockStmt)
            return TBlockStmt;
        if (o instanceof ReturnStmt)
            return TReturnStmt;
        if (o instanceof NameExpr)
            return TNameExpr;
        if (o instanceof ValueExp)
            return TValueExp;
        if (o instanceof BinaryExpr)
            return TBinaryExpr;
        return Unknown;
    }

}
