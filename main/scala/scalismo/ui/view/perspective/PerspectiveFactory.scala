package scalismo.ui.view.perspective

import scalismo.ui.settings.GlobalSettings
import scalismo.ui.view.ScalismoFrame

/**
 * Global singleton containing data related to available perspectives.
 * To allow for customization, available perspectives can be added (or removed)
 * by calling the addFactory and removeFactory methods. If needed, the defaultPerspective
 * can also be overwritten (that's why it's a var).
 *
 * It is advisable to do any necessary modifications right at the beginning of the program,
 * before a frame is actually created.
 */
object PerspectiveFactory {
  final val BuiltinFactories: List[PerspectiveFactory] = List(OrthogonalPerspective, Single3DViewportPerspective, Two3DViewportsPerspective)

  private var _factories: List[PerspectiveFactory] = BuiltinFactories

  def factories: List[PerspectiveFactory] = _factories

  def addFactory(factory: PerspectiveFactory) = {
    _factories ++= List(factory)
  }

  def removeFactory(factory: PerspectiveFactory) = {
    _factories = factories.filterNot(_ == factory)
  }

  var defaultPerspective: PerspectiveFactory = {
    val userPreferred = GlobalSettings.get[String](GlobalSettings.Keys.PerspectiveName).flatMap { name => factories.find(_.perspectiveName == name) }
    userPreferred.getOrElse(factories.head)
  }

}

trait PerspectiveFactory {
  /**
   * Name of the perspective. This *MUST* be globally unique.
   * @return the unique, human-readable name of the perspective.
   */
  def perspectiveName: String
  def instantiate(frame: ScalismoFrame): Perspective
}
