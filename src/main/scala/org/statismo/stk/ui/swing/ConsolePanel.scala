package org.statismo.stk.ui.swing

import scala.swing.BorderPanel
import de.sciss.scalainterpreter._
import scala.swing.Component
import org.statismo.stk.ui.StatismoFrame
import javax.swing.JSplitPane
import javax.swing.SwingConstants
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import org.statismo.stk.ui.swing.util.MultiOutputStream
import java.io.PrintStream

class ConsolePanel(implicit frame: StatismoFrame) extends BorderPanel {
  val icfg = Interpreter.Config()
  icfg.imports :+= "org.statismo.stk.ui._"
  icfg.bindings ++= Seq(NamedParam("frame", frame)).toIndexedSeq

  val codeCfg = CodePane.Config()
  codeCfg.style = Style.Light
  val split = MSplitPane(InterpreterPane.Config().build, icfg.build, codeCfg.build)
  split.component.setResizeWeight(0.5)
  layout(Component.wrap(split.component)) = BorderPanel.Position.Center
}

object MSplitPane {
  def apply(paneConfig: InterpreterPane.Config = InterpreterPane.Config().build,
            interpreterConfig: Interpreter.Config = Interpreter.Config().build,
            codePaneConfig: CodePane.Config = CodePane.Config().build): MSplitPane = {
    val fac = new ThreadFactory {
      def newThread(r: Runnable): Thread = {
        new Thread(r) {
          setDaemon(true)
        }
      }
    }
    val exec = ExecutionContext fromExecutorService Executors.newSingleThreadExecutor(fac)

    // FIXME: There must be a better way to do this.
    val sysout = System.out
    val syserr = System.err

    val lp = LogPane().makeDefault(error = true)

    val mout = new MultiOutputStream(System.out, sysout)
    val merr = new MultiOutputStream(System.err, syserr)
    System.setOut(new PrintStream(mout))
    System.setErr(new PrintStream(merr))

    val intCfg = Interpreter.ConfigBuilder(interpreterConfig)
    intCfg.out = Some(lp.writer)
    val ip = InterpreterPane(paneConfig, intCfg.build, codePaneConfig)(exec)
    val sp = new JSplitPane(SwingConstants.HORIZONTAL)
    sp.setTopComponent(ip.component)
    sp.setBottomComponent(lp.component)
    new Impl(sp, ip)
  }

  private final class Impl(val component: JSplitPane, val interpreter: InterpreterPane)
    extends MSplitPane {
    override def toString = "SplitPane@" + hashCode.toHexString
  }

}

sealed trait MSplitPane {
  def component: JSplitPane

  def interpreter: InterpreterPane
}