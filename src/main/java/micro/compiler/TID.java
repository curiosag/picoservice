package micro.compiler;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;

import java.util.Objects;

class TID {
    public final BinaryExpr.Operator op;
    public final ResolvedPrimitiveType t;

    private TID(BinaryExpr.Operator op, ResolvedPrimitiveType t) {
        this.op = op;
        this.t = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TID tid = (TID) o;
        return op == tid.op && t == tid.t;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, t);
    }

    public static TID tid(BinaryExpr.Operator op, ResolvedPrimitiveType t) {
        return new TID(op, t);
    }
}