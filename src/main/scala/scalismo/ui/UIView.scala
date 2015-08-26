package scalismo.ui

/**
 * Trait signifying that the implementing class is a "view" on an underlying
 * scalismo core object
 * @tparam U the underlying object's type
 */
trait UIView[U] {
  def source: U
}
