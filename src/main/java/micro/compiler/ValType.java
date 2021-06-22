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

import java.util.ArrayList;
import java.util.List;

public class ValType {

    private static final List<Class<? extends Node>> VAL_CLASSES = new ArrayList<Class<? extends Node>>() {{
        add(com.github.javaparser.ast.expr.LiteralExpr.class);
        add(com.github.javaparser.ast.expr.LiteralStringValueExpr.class);
        add(com.github.javaparser.ast.expr.StringLiteralExpr.class);
        add(com.github.javaparser.ast.expr.BooleanLiteralExpr.class);
        add(com.github.javaparser.ast.expr.CharLiteralExpr.class);
        add(com.github.javaparser.ast.expr.DoubleLiteralExpr.class);
        add(com.github.javaparser.ast.expr.IntegerLiteralExpr.class);
        add(com.github.javaparser.ast.expr.LongLiteralExpr.class);
        add(com.github.javaparser.ast.expr.NullLiteralExpr.class);
        add(com.github.javaparser.ast.expr.TextBlockLiteralExpr.class);
    }};

}
