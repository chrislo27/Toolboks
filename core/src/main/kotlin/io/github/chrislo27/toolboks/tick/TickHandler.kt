package io.github.chrislo27.toolboks.tick

@FunctionalInterface
interface TickHandler {

    fun tickUpdate(tickController: TickController)

}