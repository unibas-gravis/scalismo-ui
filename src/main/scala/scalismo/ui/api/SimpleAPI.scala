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

package scalismo.ui.api

trait SimpleAPI {

  def createGroup(groupName: String): Group

  def show[A](a: A, name: String)(implicit showInScene: ShowInScene[A]): showInScene.View

  def show[A](group: Group, a: A, name: String)(implicit showInScene: ShowInScene[A]): showInScene.View

  def visibility[V <: ObjectView](view: V, visibleViewports: Seq[Viewport]): Unit

  def addTransformation[T](g: Group, t: T, name: String)(implicit showInScene: ShowInScene[T]): showInScene.View

  def filter[V <: ObjectView: FindInScene](pred: V => Boolean): Seq[V]

  def filter[V <: ObjectView: FindInScene](group: Group, pred: V => Boolean): Seq[V]

  def find[V <: ObjectView: FindInScene](pred: V => Boolean): Option[V]

  def find[V <: ObjectView: FindInScene](group: Group, pred: V => Boolean): Option[V]

  def onNodeAdded[A <: ObjectView: HandleCallback, R](g: Group, f: A => R): Unit

  def onNodeRemoved[A <: ObjectView: HandleCallback, R](g: Group, f: A => R): Unit

  def onGroupAdded[R](f: Group => R): Unit

  def onGroupRemoved[R](f: Group => R): Unit

}

