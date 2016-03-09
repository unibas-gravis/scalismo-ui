package scalismo.ui.rendering.actor

import scalismo.ui.model.Renderable
import scalismo.ui.view.ViewportPanel

import scala.reflect.ClassTag

object ActorsFactory {
  val BuiltinFactories: List[ActorsFactory] = List(TriangleMeshActor)

  var _factories: Map[Class[_ <: Renderable], ActorsFactory] = Map.empty

  BuiltinFactories.foreach(f => addFactory(f))

  def factories: Map[Class[_ <: Renderable], ActorsFactory] = _factories

  def addFactory(factory: ActorsFactory): Unit = {
    _factories ++= factory.supportedClasses.map(c => (c, factory))
  }

  def removeFactory(factory: ActorsFactory): Unit = {
    _factories = _factories.filterNot(_._2 == factory)
  }

  def removeFactory(clazz: Class[_ <: Renderable]): Unit = {
    _factories = _factories.filterNot(_._1 == clazz)
  }

  def factoryFor(renderable: Renderable): Option[ActorsFactory] = factories.get(renderable.getClass)
}

trait ActorsFactory {
  def supportedClasses: List[Class[_ <: Renderable]]
  def untypedActorsFor(renderable: Renderable, viewport: ViewportPanel): Option[Actors]
}

abstract class SimpleActorsFactory[T <: Renderable: ClassTag] extends ActorsFactory {
  final override def supportedClasses: List[Class[_ <: Renderable]] = {
    List(implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Renderable]])
  }

  final override def untypedActorsFor(renderable: Renderable, viewport: ViewportPanel): Option[Actors] = {
    actorsFor(renderable.asInstanceOf[T], viewport)
  }

  def actorsFor(renderable: T, viewport: ViewportPanel): Option[Actors]
}