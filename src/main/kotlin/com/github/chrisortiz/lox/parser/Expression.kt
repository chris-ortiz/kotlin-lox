package com.github.chrisortiz.lox.parser

import com.github.chrisortiz.lox.lexer.Token

interface Expression {
    fun <T> accept(visitor: ExpVisitor<T>): T
}

interface ExpVisitor<T> {
    fun visitBinary(binary: Binary): T
    fun visitGrouping(grouping: Grouping): T
    fun visitLiteral(literal: Literal): T
    fun visitUnary(unary: Unary): T
}

data class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression {
    override fun <T> accept(visitor: ExpVisitor<T>) = visitor.visitBinary(this)
}

data class Grouping(val expression: Expression) : Expression {
    override fun <T> accept(visitor: ExpVisitor<T>) = visitor.visitGrouping(this)
}

data class Literal(val value: Any?) : Expression {
    override fun <T> accept(visitor: ExpVisitor<T>) = visitor.visitLiteral(this)
}

data class Unary(val operator: Token, val right: Expression) : Expression {
    override fun <T> accept(visitor: ExpVisitor<T>) = visitor.visitUnary(this)
}