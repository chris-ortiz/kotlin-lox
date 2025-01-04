package com.github.chrisortiz.lox.parser

class AstPrinter : ExpVisitor<String> {
    fun print(expression: Expression) = expression.accept(this)

    override fun visitBinary(binary: Binary) =
        parenthesize(binary.operator.lexeme, binary.left, binary.right)


    override fun visitGrouping(grouping: Grouping) = parenthesize("group", grouping.expression)

    override fun visitLiteral(literal: Literal) = when (literal.value) {
        null -> "nil"
        else -> parenthesize(literal.value.toString())
    }

    override fun visitUnary(unary: Unary) = parenthesize(unary.operator.lexeme, unary.right)

    private fun parenthesize(name: String, vararg expressions: Expression): String {
        val builder = StringBuilder()
        builder.append("(").append(name)
        expressions.forEach {
            builder.append(" ").append(it.accept(this))
        }
        builder.append(")")

        return builder.toString()
    }
}