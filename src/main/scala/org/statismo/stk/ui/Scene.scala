package org.statismo.stk.ui

import scala.collection.immutable.List
import scala.swing.Publisher
import scala.swing.Reactor
import scala.swing.event.Event

case class SceneChanged extends Event

class Scene extends Publisher {
	org.statismo.stk.core.initialize
	
	var objects: List[SceneObject] = Nil
	def addObjects(list: List[SceneObject]): List[SceneObject] = {
	  if (list.length > 0) {
		  objects ++= list
		  publish(SceneChanged())
	  }
	  list
	}
	
	def removeAllObjects() = {
	  if (objects.length > 0) {
		  objects = Nil;
		  publish(SceneChanged())
	  }
	}
	
	def loadObjects(paths: String*): List[SceneObject] = {
	  loadObjects(List(paths).flatten)
	}
	
	def loadObjects(paths: List[String], factories: Seq[Loadable[SceneObject]] = Loadable.defaultFactories): List[SceneObject] = {
	  val tries = paths map(fn => Loadable.load(fn, factories))
	  val ok = tries filter(_.isSuccess) map(_.get)
	  addObjects(ok)
	}
	
	
}
