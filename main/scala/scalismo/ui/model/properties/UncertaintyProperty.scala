package scalismo.ui.model.properties

class UncertaintyProperty(initialValue: Uncertainty) extends NodeProperty[Uncertainty](initialValue) {
  def this() = this(Uncertainty.DefaultUncertainty)
}

trait HasUncertainty {
  def uncertainty: UncertaintyProperty
}