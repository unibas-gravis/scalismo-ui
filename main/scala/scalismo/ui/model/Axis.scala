package scalismo.ui.model

sealed trait Axis {
}

object Axis {
  case object X extends Axis
  case object Y extends Axis
  case object Z extends Axis
}
