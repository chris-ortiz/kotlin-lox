package com.github.chrisortiz.lox.interpreter

import com.github.chrisortiz.lox.Environment
import com.github.chrisortiz.lox.Lox
import com.github.chrisortiz.lox.lexer.Token
import com.github.chrisortiz.lox.lexer.TokenType.*
import com.github.chrisortiz.lox.parser.*

class Interpreter : ExpVisitor<Any?>, StmtVisitor<Unit> {
    var environment = Environment()

    fun interpret(statements: List<Statement>) {
        try {
            statements.forEach { execute(it) }
        } catch (e: RuntimeError) {
            Lox.runtimeError(e)
        }
    }

    private fun execute(statement: Statement) {
        statement.accept(this)
    }

    private fun stringify(any: Any?) = when (any) {
        null -> "nil"
        is Double -> {
            var text = any.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }

            text
        }

        else -> {
            any.toString()
        }
    }

    override fun visitBinary(binary: Binary): Any {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)

        return when (binary.operator.tokenType) {
            MINUS -> {
                checkNumberOperand(binary.operator, left, right)
                left as Double - right as Double
            }

            PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> throw RuntimeError(binary.operator, "Operands must be two numbers or two strings.")
            }

            SLASH -> {
                checkNumberOperand(binary.operator, left, right)
                left as Double / right as Double
            }

            STAR -> {
                checkNumberOperand(binary.operator, left, right)
                left as Double * right as Double
            }

            GREATER -> {
                checkNumberOperand(binary.operator, left, right)
                left as Double > right as Double
            }

            GREATER_EQUAL -> {
                checkNumberOperand(binary.operator, left, right)
                left as Double >= right as Double
            }

            LESS -> {
                checkNumberOperand(binary.operator, left, right)
                (left as Double) < right as Double
            }

            LESS_EQUAL -> {
                checkNumberOperand(binary.operator, left, right)
                (left as Double) <= right as Double
            }

            BANG_EQUAL -> left != right
            EQUAL_EQUAL -> left == right

            else -> Lox.error(binary.operator.line, "invalid binary operation")
        }
    }

    override fun visitGrouping(grouping: Grouping) = evaluate(grouping.expression)

    override fun visitLiteral(literal: Literal) = literal.value

    override fun visitUnary(unary: Unary): Any {
        val right = evaluate(unary.right)
        return when (unary.operator.tokenType) {
            BANG -> !isTruthy(right)
            MINUS -> {
                checkNumberOperand(unary.operator, right)
                -(right as Double)
            }

            else -> Lox.error(unary.operator.line, "invalid unary operator")
        }
    }

    override fun visitVariable(variable: Variable) = environment.get(variable.name)
    override fun visitAssign(assign: Assign): Any? {
        val value = evaluate(assign.value)
        environment.assign(assign.name, value)
        return value
    }

    override fun visitLogical(logical: Logical): Any? {
        val left = evaluate(logical.left)

        if (logical.operator.tokenType == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(logical.right)
    }

    private fun checkNumberOperand(operator: Token, vararg operands: Any?) {
        operands.forEach {
            if (it !is Double) {
                throw RuntimeError(operator, "Operand must be a number.")
            }
        }
    }


    private fun isTruthy(any: Any?): Boolean = when (any) {
        null -> false
        is Boolean -> any
        else -> true
    }

    private fun evaluate(expression: Expression) = expression.accept(this)

    override fun visitExpressionStmt(expression: ExpressionStmt) {
        evaluate(expression.expression)
    }

    override fun visitPrintStmt(stmt: PrintStmt) {
        val v = evaluate(stmt.expression)
        println(stringify(v))
    }

    override fun visitVarStmt(stmt: VarStmt) {
        var value: Any? = null
        stmt.initializer?.let {
            value = evaluate(it)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitBlock(block: Block) {
        executeBlock(block.statements, Environment(environment))
    }

    override fun visitIf(stmt: If) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else {
            stmt.elseBranch?.let {
                execute(it)
            }
        }
    }

    override fun visitWhile(stmt: While) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    private fun executeBlock(statements: List<Statement>, env: Environment) {
        val previous = environment
        try {
            environment = env
            statements.forEach { execute(it) }
        } finally {
            environment = previous
        }
    }
}