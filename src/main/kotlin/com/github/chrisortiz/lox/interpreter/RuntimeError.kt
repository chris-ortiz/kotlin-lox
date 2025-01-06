package com.github.chrisortiz.lox.interpreter

import com.github.chrisortiz.lox.lexer.Token

class RuntimeError(val token: Token, override val message: String) : RuntimeException(message)