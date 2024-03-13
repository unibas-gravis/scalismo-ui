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

package scalismo.ui.view

import scalismo.ui.util.EdtUtil

import scala.swing.SimpleSwingApplication

/**
 * A Scalismo application is a `scala.swing.SimpleSwingApplication`, having a [[ScalismoFrame]] as the top component, and using the [[ScalismoLookAndFeel]].
 *
 * This class takes care of initializing the Look and Feel, and setting up the frame with the command-line arguments.
 *
 * @param frame                the main scalismo frame. This value is passed "by name", so instantiation happens on demand, and on the correct thread (Swing EDT)
 * @param lookAndFeelClassName Swing Look and Feel. By default, the value of [[ScalismoLookAndFeel.DefaultLookAndFeelClassName]] is used.
 *
 */
class ScalismoApplication(frame: => ScalismoFrame,
                          lookAndFeelClassName: String = ScalismoLookAndFeel.DefaultLookAndFeelClassName)
    extends SimpleSwingApplication {

  override lazy val top: ScalismoFrame = EdtUtil.onEdtWait(frame)

  override def main(args: Array[String]): Unit = {
    scalismo.vtk.initialize()
    ScalismoLookAndFeel.initializeWith(lookAndFeelClassName)
    super.main(args)
  }

  override def startup(args: Array[String]): Unit = {
    top.setup(args)
    super.startup(args)
  }
}

object ScalismoApplication {

  /**
   * ScalismoApplication factory method.
   *
   * All arguments have sensible defaults, and can selectively be overridden.
   *
   * @param frame                the top-level frame. This value is passed "by name", so instantiation happens on demand, and on the correct thread (Swing EDT).
   * @param args                 command-line arguments. Default value: an empty array.
   * @param lookAndFeelClassName Swing Look and Feel. By default, the value of [[ScalismoLookAndFeel.DefaultLookAndFeelClassName]] is used.
   * @return
   */
  def apply(frame: => ScalismoFrame = new ScalismoFrame,
            args: Array[String] = new Array(0),
            lookAndFeelClassName: String = ScalismoLookAndFeel.DefaultLookAndFeelClassName): ScalismoApplication = {
    val application = new ScalismoApplication(frame, lookAndFeelClassName)
    application.main(args)
    application
  }

  // only meant for the trivial Java wrapper
  def run(args: Array[String]): Unit = apply(args = args)
}
