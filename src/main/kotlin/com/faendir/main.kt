package com.faendir

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import java.io.File

fun main(args: Array<String>) {
    args.forEach { handle(File(it)) }
}

fun handle(file: File) {
    if (file.exists()) {
        if (file.isDirectory) {
            file.list()?.forEach { handle(File(file, it)) }
        } else if (file.extension == "html") {
            println("Transforming $file")
            separate(file)
        }
    } else {
        println("Warn: $file does not exist")
    }
}

fun separate(file: File) {
    val document = Jsoup.parse(file, Charsets.UTF_8.name())
    val visitor = CssVisitor()
    NodeTraversor.traverse(visitor, document)
    if (visitor.css.isNotEmpty()) {
        val cssFile = file.nameWithoutExtension + ".css"
        document.head().appendElement("link").attr("rel", "stylesheet").attr("type", "text/css").attr("href", cssFile)
        File(file.parent, cssFile).writeText(visitor.css)
    }
    file.writeText(document.outerHtml())
}

class CssVisitor : NodeVisitor {
    var css = ""
        private set

    override fun tail(node: Node, depth: Int) {
    }

    override fun head(node: Node, depth: Int) {
        if (node is Element && node.hasAttr("style")) {
            val style = node.attr("style")
            node.removeAttr("style")
            css += "${node.cssSelector()}{$style}"
        }
    }

}