/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.rendering.actor

import scalismo.ui.model.Renderable
import scalismo.ui.view.ViewportPanel

import scala.reflect.ClassTag

/**
 * The ActorsFactory creates VTK actors from something that is renderable.
 *
 * This object is a "super factory" in the sense that it maintains a
 * mapping of which classes of renderables should be fed to which
 * factories to produce the final result.
 *
 */
object ActorsFactory {
  val BuiltinFactories: List[ActorsFactory] = List(
    BoundingBoxActor,
    TriangleMeshActor,
    ScalarMeshFieldActor,
    ColorMeshActor,
    LineMeshActor,
    PointCloudActor,
    LandmarkActor,
    ImageActor,
    ScalarFieldActor,
    VectorFieldActor,
    TransformationGlyphActor
  )

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

  def factoryFor(renderable: Renderable): Option[ActorsFactory] = {
    val result = factories.get(renderable.getClass)
    if (result.isEmpty) {
      println("Warning: no ActorsFactory for " + renderable.getClass)
    }
    result
  }
}

/**
 * This is a low-level trait for factories that can map arbitrary
 * objects to actors. It should not normally be extended.
 *
 * See [[SimpleActorsFactory]] for a type-safe variant.
 */
trait ActorsFactory {
  def supportedClasses: List[Class[_ <: Renderable]]

  def untypedActorsFor(renderable: Renderable, viewport: ViewportPanel): Option[Actors]
}

/**
 * A SimpleActorsFactory is a clean and simple factory that produces actors for
 * a given type.
 *
 * It takes care of all the gory details of type erasure and runtime classes.
 * This is the class you'll normally want to extend.
 */
abstract class SimpleActorsFactory[T <: Renderable: ClassTag] extends ActorsFactory {
  final override def supportedClasses: List[Class[_ <: Renderable]] = {
    List(implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[_ <: Renderable]])
  }

  final override def untypedActorsFor(renderable: Renderable, viewport: ViewportPanel): Option[Actors] = {
    actorsFor(renderable.asInstanceOf[T], viewport)
  }

  def actorsFor(renderable: T, viewport: ViewportPanel): Option[Actors]
}
