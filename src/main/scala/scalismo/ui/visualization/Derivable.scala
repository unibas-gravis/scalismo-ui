package scalismo.ui.visualization

import scala.collection.immutable
import scala.ref.WeakReference

trait Derivable[A <: AnyRef] {
  protected val self: A = this.asInstanceOf[A]
  private var _derived: immutable.Seq[WeakReference[A]] = Nil

  protected[visualization] def derived(forceGarbageCollection: Boolean = false): immutable.Seq[A] = {
    derivedInUse(forceGarbageCollection).map(r => r.get).filter(_.isDefined).map(o => o.get)
  }

  //note: this has the side effect of checking for, and throwing away, stale references in 10% of the read-only invocations
  private def derivedInUse(forceGarbageCollection: Boolean): immutable.Seq[WeakReference[A]] = {
    if (forceGarbageCollection || Math.random() <= 0.1) {
      _derived = _derived.filter(_.get.isDefined)
    }
    _derived
  }

  protected[ui] final def derive(): A = {
    val child: A = createDerived()
    _derived = Seq(derivedInUse(true), Seq(new WeakReference[A](child))).flatten.to[immutable.Seq]
    child
  }

  protected def createDerived(): A
}

