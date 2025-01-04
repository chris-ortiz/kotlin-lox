package com.github.chrisortiz.lox.interpreter

import com.github.chrisortiz.lox.Lox
import com.github.chrisortiz.lox.lexer.TokenType.*
import com.github.chrisortiz.lox.parser.*

class Interpreter : ExpVisitor<Any?> {
    override fun visitBinary(binary: Binary): Any {
        val left = evaluate(binary.left)
        val right = evaluate(binary.right)

        return when (binary.operator.tokenType) {
            MINUS -> left as Double - right as Double
            PLUS -> when {
                left is Double && right is Double -> left + right
                left is String && right is String -> left + right
                else -> Lox.error(binary.operator.line, "invalid binary operation")
            }

            SLASH -> left as Double / right as Double
            STAR -> left as Double * right as Double

            GREATER -> left as Double > right as Double
            GREATER_EQUAL -> left as Double >= right as Double
            LESS -> (left as Double) < right as Double
            LESS_EQUAL -> (left as Double) <= right as Double

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
            MINUS -> -(right as Double)
            else -> Lox.error(unary.operator.line, "invalid unary operator")
        }
    }

    private fun isTruthy(any: Any?): Boolean = when (any) {
        null -> false
        is Boolean -> any
        else -> true
    }

    private fun evaluate(expression: Expression) = expression.accept(this)
}