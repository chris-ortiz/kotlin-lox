package com.github.chrisortiz.lox.parser

import com.github.chrisortiz.lox.Lox
import com.github.chrisortiz.lox.lexer.Token
import com.github.chrisortiz.lox.lexer.TokenType
import com.github.chrisortiz.lox.lexer.TokenType.*

/**
 * BNF grammar:
 * expression → equality ;
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary
 * | primary ;
 * primary → NUMBER | STRING | "true" | "false" | "nil"
 * | "(" expression ")" ;
 */
class Parser(val tokens: List<Token>) {
    private var current: Int = 0

    fun parse(): Expression? {
        return try {
            expression()
        } catch (_: Exception) {
            null
        }
    }

    private fun expression() = equality()

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
            match(LEFT_PAREN) -> {
                val expression = expression()
                consume(RIGHT_PAREN, "Expect ')' after expression.")
                Grouping(expression)
            }

            else -> throw error(peek(), "Expect expression.")
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