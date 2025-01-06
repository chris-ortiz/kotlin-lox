package com.github.chrisortiz.lox

import com.github.chrisortiz.lox.interpreter.RuntimeError
import com.github.chrisortiz.lox.lexer.Token

class Environment {
    private val enclosing: Environment?
    private val values = mutableMapOf<String, Any?>()

    constructor() {
        enclosing = null
    }

    constructor(enclosing: Environment) {
        this.enclosing = enclosing
    }

    fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        enclosing?.let {
            return enclosing.get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
    }

    fun define(name: String, value: Any?) = values.put(name, value)
    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value)
        } else if (enclosing != null) {
            enclosing.assign(name, value)
        } else {
            throw RuntimeError(name, "Undefined variable '${name.lexeme}'")
        }
    }

}