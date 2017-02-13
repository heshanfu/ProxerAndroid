package com.proxerme.app.stream.resolver

import android.content.Intent
import com.proxerme.app.stream.StreamResolver
import com.proxerme.app.task.StreamResolutionTask.StreamResolutionResult

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class CodeResolver : StreamResolver() {

    override val name = "Code"
    private val alternativeName = "Nachricht"

    override fun appliesTo(name: String): Boolean {
        return name.contains(this.name, false) || name.contains(alternativeName, false)
    }

    override fun resolve(url: String): StreamResolutionResult {
        return StreamResolutionResult(Intent("Code").apply {
            putExtra(StreamResolutionResult.MESSAGE, url.replace(Regex("</?h1>"), ""))
        })
    }
}