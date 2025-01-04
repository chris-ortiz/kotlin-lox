package com.github.chrisortiz.lox

import com.github.chrisortiz.lox.lexer.Scanner
import com.github.chrisortiz.lox.lexer.Token
import com.github.chrisortiz.lox.lexer.TokenType.EOF
import com.github.chrisortiz.lox.parser.AstPrinter
import com.github.chrisortiz.lox.parser.Parser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Lox {
    var hadError = false
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
    }

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        while (true) {
            print("> ")
            val line = reader.readLine() ?: break
            run(line)
            hadError = false
        }
    }

    fun run(source: String) {
        val scanner = Scanner(source)
        val tokens = scanner.scanTokens()
        val parser = Parser(tokens)
        val expression = parser.parse()

        if (hadError) return

        println(AstPrinter().print(expression!!))
    }


    fun report(line: Int, where: String, message: String) {
        System.err.println("[line $line] Error$where: $message")
        hadError = true
    }
}

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: jlox [script]")
        exitProcess(65)
    } else if (args.size == 1) {
        Lox.runFile(args[0])
    } else {
        Lox.runPrompt()
    }
}

