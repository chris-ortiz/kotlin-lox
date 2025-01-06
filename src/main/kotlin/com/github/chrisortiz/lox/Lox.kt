package com.github.chrisortiz.lox

import com.github.chrisortiz.lox.interpreter.Interpreter
import com.github.chrisortiz.lox.interpreter.RuntimeError
import com.github.chrisortiz.lox.lexer.Scanner
import com.github.chrisortiz.lox.lexer.Token
import com.github.chrisortiz.lox.lexer.TokenType.EOF
import com.github.chrisortiz.lox.parser.Parser
import com.github.chrisortiz.lox.parser.ParserError
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Lox {
    var hadError = false
    var hadRuntimeError = false
    val interpreter = Interpreter()

    fun error(line: Int, message: String) {
        report(line, "", message)
    }

    fun error(token: Token, errorMessage: String) {
        if (token.tokenType == EOF) {
            report(token.line, " at end", errorMessage)
        } else {
            report(token.line, " at '${token.lexeme}'", errorMessage)
        }
    }

    fun runFile(fileName: String) {
        run(Files.readString(Paths.get(fileName)))

        if (hadError) exitProcess(65)
        if (hadRuntimeError) exitProcess(70)
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            try {
                print("> ")
                val line = reader.readLine() ?: break
                run(line)
                hadError = false
            } catch (e: ParserError) {
                println(e.message)
            }
        }
    }

    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val statements = parser.parse()

        if (hadError) return

        interpreter.interpret(statements)
    }

    fun report(line: Int, where: String, message: String) {
        println("[line $line] Error$where: $message")
        hadError = true
    }

    fun runtimeError(error: RuntimeError) {
        println("${error.message}\n[line ${error.token.line}]")
        hadRuntimeError = true
    }
}
