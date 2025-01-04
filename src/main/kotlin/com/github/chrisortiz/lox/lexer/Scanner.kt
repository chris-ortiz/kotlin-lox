package com.github.chrisortiz.lox.lexer

import com.github.chrisortiz.lox.Lox
import java.lang.Character.MIN_VALUE as nullChar

class Scanner(private val source: String) {
    private val tokens: MutableList<Token> = mutableListOf()
    private var start = 0
    private var current = 0
    private var line = 1
    private val digits: CharRange = '0'..'9'

    fun scanTokens(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()

        when (c) {
            '(' -> addToken(TokenType.LEFT_PAREN)
            ')' -> addToken(TokenType.RIGHT_PAREN)
            '{' -> addToken(TokenType.LEFT_BRACE)
            '}' -> addToken(TokenType.RIGHT_BRACE)
            ',' -> addToken(TokenType.COMMA)
            '.' -> addToken(TokenType.DOT)
            '-' -> addToken(TokenType.MINUS)
            '+' -> addToken(TokenType.PLUS)
            ';' -> addToken(TokenType.SEMICOLON)
            '*' -> addToken(TokenType.STAR)
            '!' -> addToken(
                if (match('=')) {
                    TokenType.BANG_EQUAL
                } else {
                    TokenType.BANG
                }
            )

            '=' -> addToken(
                if (match('=')) {
                    TokenType.EQUAL_EQUAL
                } else {
                    TokenType.EQUAL
                }
            )

            '<' -> addToken(
                if (match('=')) {
                    TokenType.LESS_EQUAL
                } else {
                    TokenType.LESS
                }
            )

            '>' -> addToken(
                if (match('=')) {
                    TokenType.GREATER_EQUAL
                } else {
                    TokenType.GREATER
                }
            )

            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) {
                    advance()
                }
            } else {
                addToken(TokenType.SLASH)
            }

            ' ', '\r', '\t' -> Unit

            '\n' -> line++

            '"' -> string()

            in digits -> number()

            else -> {
                if (isAlpha(c)) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character")
                }
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)

        val type = keywords[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun isAlpha(c: Char) = when (c) {
        '_' -> true
        in 'A'..'Z' -> true
        in 'a'..'z' -> true
        else -> false
    }

    private fun isAlphaNumeric(c: Char) = isAlpha(c) || c in digits

    private fun number() {
        while (peek() in digits) advance()

        if (peek() == '.' && peekNext() in digits) {
            advance()

            while (peek() in digits) advance();
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"') {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.")
            return
        }

        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun peek(): Char {
        if (isAtEnd()) return nullChar
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return nullChar
        return source[current + 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false

        if (source[current] != expected) return false

        current++
        return true
    }

    private fun addToken(tokenType: TokenType) {
        addToken(tokenType, null)
    }

    private fun addToken(tokenType: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(tokenType, text, literal, line))
    }

    private fun advance(): Char {
        current++
        return source[current - 1]
    }

    private fun isAtEnd() = current >= source.length

}

val keywords = mapOf(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "for" to TokenType.FOR,
    "fun" to TokenType.FUN,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE,
)
