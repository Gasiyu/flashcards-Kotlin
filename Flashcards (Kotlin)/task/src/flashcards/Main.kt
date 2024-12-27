package flashcards

import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

val flashcards = mutableListOf<Flashcard>()
val log = mutableListOf<String>()

data class Flashcard(val question: String, val answer: String, var wrongCount: Int = 0)

fun addCard() {
    println("The card:")
    val card = readln().trim().also {
        log.add(it)
    }
    if (flashcards.any { it.question == card }) {
        println("The card \"$card\" already exists.")
        return
    }

    println("The definition of the card:")
    val definition = readln().trim().also {
        log.add(it)
    }
    if (flashcards.any { it.question == definition }) {
        println("The definition \"$definition\" already exists.")
        return
    }

    val flashcard = Flashcard(card, definition)
    flashcards.add(flashcard)
    println("The pair (\"$card\":\"$definition\") has been added.")
}

fun removeCard() {
    println("Which card?")
    val card = readln().trim().also {
        log.add(it)
    }

    if (flashcards.any { it.question == card }) {
        flashcards.removeIf { it.question == card }
        println("The card has been removed.")
    } else {
        println("Can't remove \"$card\": there is no such card.")
    }
}

fun importFromFile(file: File): Int {
    if (!file.exists()) return -1

    var loadedCount = 0
    val lines = file.readLines()

    for (i in lines.indices step 3) {
        if (i + 3 > lines.size) break
        val question = lines[i]
        val answer = lines[i + 1]
        val wrongAnswer = lines[i + 2].toInt()

        val existingIndex = flashcards.indexOfFirst { it.question == question }
        if (existingIndex != -1) {
            flashcards[existingIndex] = Flashcard(question, answer, wrongAnswer)
        } else {
            flashcards.add(Flashcard(question, answer, wrongAnswer))
        }
        loadedCount++
    }

    return loadedCount
}

fun exportToFile(file: File): Int {
    val content = flashcards.joinToString("\n") { flashcard ->
        "${flashcard.question}\n${flashcard.answer}\n${flashcard.wrongCount}"
    }
    file.writeText(content)
    return flashcards.size
}

fun importCard() {
    println("File name:")
    val fileName = readln().trim().also {
        log.add(it)
    }

    val loadedCount = importFromFile(File(fileName))
    if (loadedCount >= 0) {
        println("$loadedCount cards have been loaded.")
    } else {
        println("File not found.")
    }
}

fun exportCard() {
    println("File name:")
    val fileName = readln().trim().also {
        log.add(it)
    }

    val savedCount = exportToFile(File(fileName))
    println("$savedCount cards have been saved.")
}

fun ask() {
    println("How many times to ask?")
    repeat(readln().toInt()) {
        val randomNumber = Random.nextInt(0, flashcards.size)
        val randomCard = flashcards[randomNumber]

        println("Print the definition of \"${randomCard.question}\":")
        val answer = readln().trim().also {
            log.add(it)
        }
        println(if (answer == randomCard.answer) {
            "Correct!"
        } else if (flashcards.any { it.answer == answer }) {
            randomCard.wrongCount += 1

            "Wrong. The right answer is \"${randomCard.answer}\", but your definition is correct for \"${flashcards.first { it.answer == answer }.question}\"."
        } else {
            randomCard.wrongCount += 1
            "Wrong. The right answer is \"${randomCard.answer}\"."
        })
    }
}

fun log() {
    println("File name:")
    val fileName = readln().trim().also { log.add(it) }
    val file = File(fileName)
    log.forEach { file.appendText(it + System.lineSeparator()) }
    println("The log has been saved.")
}

fun resetStats() {
    flashcards.forEach { it.wrongCount = 0 }
    println("Card statistics have been reset.")
}

fun getHardestCard() {
    if (flashcards.filter { it.wrongCount != 0 }.isEmpty()) {
        println("There are no cards with errors.")
        return
    }

    val maxWrongCount = flashcards.maxByOrNull { it.wrongCount }?.wrongCount ?: 0
    val hardestCards = flashcards.filter { it.wrongCount == maxWrongCount }
    println(
        "The hardest ${if (hardestCards.size == 1) "card is" else "cards are"} " +
        "\"${hardestCards.joinToString ("\", \"") { it.question }}\"." +
        " You have $maxWrongCount errors answering ${if (hardestCards.size == 1) "it" else "them"}.\n"
    )
}

fun println(str: String = "") {
    kotlin.io.println(str)
    log.add(str)
}

fun main(args: Array<String>) {
    var importFile: String? = null
    var exportFile: String? = null

    for (i in args.indices) {
        when (args[i]) {
            "-import" -> if (i + 1 < args.size) importFile = args[i + 1]
            "-export" -> if (i + 1 < args.size) exportFile = args[i + 1]
        }
    }

    importFile?.let { fileName ->
        val loadedCount = importFromFile(File(fileName))
        if (loadedCount >= 0) {
            println("$loadedCount cards have been loaded.")
        }
    }

    while (true) {
        println("\nInput the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        val input = readln().trim().also {
            log.add(it)
        }

        when (input) {
            "add" -> addCard()
            "remove" -> removeCard()
            "import" -> importCard()
            "ask" -> ask()
            "export" -> exportCard()
            "log" -> log()
            "hardest card" -> getHardestCard()
            "reset stats" -> resetStats()
            "exit" -> {
                println("Bye bye!")
                exportFile?.let { fileName ->
                    val savedCount = exportToFile(File(fileName))
                    println("$savedCount cards have been saved.")
                }
                exitProcess(0)
            }
        }
    }
}
