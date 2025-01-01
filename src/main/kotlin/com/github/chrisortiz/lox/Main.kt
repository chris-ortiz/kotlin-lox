package com.github.chrisortiz.lox

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

object Lox {
    var hadError = false
}

fun main(args: Array<String>) {
    if (args.size > 1) {
        println("Usage: jlox [script]")
        exitProcess(65)
    } else if (args.size == 1) {
        runFile(args[0])
    } else {
        runPrompt()
    }
}

fun runFile(fileName: String) {
    run(Files.readString(Paths.get(fileName)))

    if (Lox.hadError) exitProcess(65)
}

fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")
        val line = reader.readLine() ?: break
        run(line)
        Lox.hadError = false
    }
}

fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    for (token in tokens) {
        println(token)
    }
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error$where: $message")
    Lox.hadError = true
}