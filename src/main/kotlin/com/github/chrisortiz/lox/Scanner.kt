package com.github.chrisortiz.lox

import com.github.chrisortiz.lox.TokenType.*
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

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()

        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(
                if (match('=')) {
                    BANG_EQUAL
                } else {
                    BANG
                }
            )

            '=' -> addToken(
                if (match('=')) {
                    EQUAL_EQUAL
                } else {
                    EQUAL
                }
            )

            '<' -> addToken(
                if (match('=')) {
                    LESS_EQUAL
                } else {
                    LESS
                }
            )

            '>' -> addToken(
                if (match('=')) {
                    GREATER_EQUAL
                } else {
                    GREATER
                }
            )

            '/' -> if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) {
                    advance()
                }
            } else {
                addToken(SLASH)
            }

            ' ', '\r', '\t' -> Unit

            '\n' -> line++

            '"' -> string()

            in digits -> number()

            else -> {
                if (isAlpha(c)) {
                    identifier()
                } else {
                    error(line, "Unexpected character")
                }
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumeric(peek())) advance()
        val text = source.substring(start, current)

        val type = keywords[text] ?: IDENTIFIER
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

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() {
        while (peek() != '"') {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unterminated string.")
            return
        }

        advance()
        val value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
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
    "and" to AND,
    "class" to CLASS,
    "else" to ELSE,
    "false" to FALSE,
    "for" to FOR,
    "fun" to FUN,
    "if" to IF,
    "nil" to NIL,
    "or" to OR,
    "print" to PRINT,
    "return" to RETURN,
    "super" to SUPER,
    "this" to THIS,
    "true" to TRUE,
    "var" to VAR,
    "while" to WHILE,
)
