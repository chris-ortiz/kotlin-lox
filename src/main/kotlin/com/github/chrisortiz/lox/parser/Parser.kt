package com.github.chrisortiz.lox.parser

import com.github.chrisortiz.lox.Lox
import com.github.chrisortiz.lox.lexer.Token
import com.github.chrisortiz.lox.lexer.TokenType
import com.github.chrisortiz.lox.lexer.TokenType.*

/**
 * BNF grammar:
 * program -> declaration* EOF ;
 * declaration -> varDecl | statement ;
 * varDecl -> "var" IDENTIFIER ("=" expression)? ";" ;
 * statement -> exprStmt | ifStmt | printStmt | block ;
 * ifStmt -> "if" "(" expression ")" statement ("else" statement)? ;
 * block -> "{" declaration* "}" ;
 * exprStmt -> expression ";" ;
 * printStmt -> "print" expression ";";
 * expression → assignment ;
 * assignment -> IDENTIFIER "=" assignment | equality
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary
 * | primary ;
 * primary → NUMBER | STRING | "true" | "false" | "nil"
 * | "(" expression ")" | IDENTIFIER ;
 */
class Parser(val tokens: List<Token>) {
    private var current: Int = 0

    fun parse(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!isAtEnd()) {
            declaration()?.let {
                statements.add(it)
            }
        }
        return statements
    }

    private fun declaration(): Statement? {
        return try {
            when {
                match(VAR) -> varDeclaration()
                else -> statement()
            }
        } catch (_: ParserError) {
            synchronize()
            null
        }
    }

    private fun varDeclaration(): Statement {
        val name = consume(IDENTIFIER, "Expect variable name.")
        var initializer: Expression? = null

        if (match(EQUAL)) {
            initializer = expression()
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.")
        return VarStmt(name, initializer)
    }

    private fun statement() = when {
        match(IF) -> ifStatement()
        match(PRINT) -> printStatement()
        match(LEFT_BRACE) -> Block(block())
        else -> expressionStatement()
    }

    private fun ifStatement(): Statement {
        consume(LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(RIGHT_PAREN, "Expect ')' after if condition.")

        val thenBranch = statement()
        var elseBranch: Statement? = null

        if (match(ELSE)) {
            elseBranch = statement()
        }

        return If(condition, thenBranch, elseBranch)
    }

    private fun block(): List<Statement> {
        val statements = mutableListOf<Statement>()

        while (!check(RIGHT_BRACE)) {
            declaration()?.let {
                statements.add(it)
            }
        }
        consume(RIGHT_BRACE, "Expect '}' after block.")
        return statements
    }

    private fun printStatement(): Statement {
        val value = expression()
        consume(SEMICOLON, "Expect ';' after value.")
        return PrintStmt(value)
    }

    private fun expressionStatement(): Statement {
        val expr = expression()
        consume(SEMICOLON, "Expect ';' after expression.")
        return ExpressionStmt(expr)
    }

    private fun expression() = assignment()


    private fun assignment(): Expression {
        val expr = equality()

        if (match(EQUAL)) {
            val equals = previous()
            val value = assignment()


            if (expr is Variable) {
                val name = expr.name
                return Assign(name, value)
            }

            error(equals, "Invalid assignment target.")
        }

        return expr
    }

    private fun equality(): Expression {
        var expression = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expression = Binary(expression, operator, right)
        }

        return expression
    }

    private fun comparison(): Expression {
        var expression = term()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expression = Binary(expression, operator, right)
        }

        return expression
    }

    private fun term(): Expression {
        var expression = factor()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = factor()
            expression = Binary(expression, operator, right)
        }

        return expression
    }

    private fun factor(): Expression {
        var expression = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expression = Binary(expression, operator, right)
        }
        return expression
    }

    private fun unary(): Expression {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()

            return Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expression {
        return when {
            match(FALSE) -> Literal(false)
            match(TRUE) -> Literal(true)
            match(NIL) -> Literal(null)
            match(NUMBER, STRING) -> Literal(previous().literal)
            match(IDENTIFIER) -> Variable(previous())
            match(LEFT_PAREN) -> {
                val expression = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Grouping(expression)
            }

            else -> throw error(peek(), "Expected expression.")
        }
    }

    private fun consume(tokenType: TokenType, errorMessage: String): Token {
        if (check(tokenType)) {
            return advance()
        } else {
            throw error(peek(), errorMessage)
        }
    }

    private fun error(token: Token, errorMessage: String): ParserError {
        Lox.error(token, errorMessage)
        return ParserError()
    }

    private fun synchronize(): Unit {
        advance()

        while (!isAtEnd()) {
            if (previous().tokenType == SEMICOLON) return

            when (peek().tokenType) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }

        }
    }


    private fun match(vararg types: TokenType): Boolean {
        for (tokenType in types) {
            if (check(tokenType)) {
                advance()
                return true
            }

        }
        return false
    }


    private fun check(tokenType: TokenType): Boolean {
        if (isAtEnd()) return false

        return peek().tokenType == tokenType
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() = peek().tokenType == EOF
    private fun peek() = tokens[current]
    private fun previous() = tokens[current - 1]
}