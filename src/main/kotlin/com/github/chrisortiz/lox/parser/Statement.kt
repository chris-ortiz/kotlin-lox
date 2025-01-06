package com.github.chrisortiz.lox.parser

import com.github.chrisortiz.lox.lexer.Token


interface Statement {
    fun <T> accept(visitor: StmtVisitor<T>): T
}

interface StmtVisitor<T> {
    fun visitExpressionStmt(expression: ExpressionStmt): T
    fun visitPrintStmt(stmt: PrintStmt): T
    fun visitVarStmt(stmt: VarStmt): T
    fun visitBlock(block: Block): T
}

data class ExpressionStmt(val expression: Expression) : Statement {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visitExpressionStmt(this)
}

data class PrintStmt(val expression: Expression) : Statement {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visitPrintStmt(this)
}

data class VarStmt(val name: Token, val initializer: Expression?) : Statement {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visitVarStmt(this)
}

data class Block(val statements: List<Statement>) : Statement {
    override fun <T> accept(visitor: StmtVisitor<T>) = visitor.visitBlock(this)
}