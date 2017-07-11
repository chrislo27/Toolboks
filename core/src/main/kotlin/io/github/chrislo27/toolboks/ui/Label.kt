package io.github.chrislo27.toolboks.ui

import io.github.chrislo27.toolboks.ToolboksScreen


abstract class Label<S : ToolboksScreen<*, *>>(override var palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : UIElement<S>(parent, stage), Palettable {

}