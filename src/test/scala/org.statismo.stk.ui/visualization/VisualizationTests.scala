package org.statismo.stk.core.io

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.statismo.stk.ui.visualization._
import org.statismo.stk.ui.{Scene, Viewport}
import scala.collection.immutable.Seq
import scala.collection.immutable

class VisualizationTests  extends FunSpec with ShouldMatchers {

  class TestProp extends VisualizationProperty[String, TestProp] {
    val defaultValue: String = "DEFAULT"
    def newInstance() = new TestProp
  }

  class DummyViewport extends Viewport {
    override def scene: Scene = ???
    override def isMouseSensitive: Boolean = ???
  }

  class TestLandmarkVisualization1(template: Option[TestLandmarkVisualization1]) extends ConcreteVisualization[LM, TestLandmarkVisualization1] {
    def this() = this(None)
    def renderablesFor(target: LM) = Nil
    override protected def createDerived(): TestLandmarkVisualization1 = new TestLandmarkVisualization1(Some(this))
  }

  trait TestVisualizable extends Visualizable[TestVisualizable] {

  }

  class TestViewport1 extends DummyViewport
  class TestViewport2 extends DummyViewport

  describe("A VisualizationProperty") {

    it("returns its default value after instantiation") {
      val t = new TestProp
      t.value should equal("DEFAULT")
    }

    it("changes its value when requested") {
      val t = new TestProp
      val changed = "CHANGED"
      t.value = changed
      t.value should equal(changed)
    }

    it("is properly instantiated from a parent value") {
      val parentValue = "PARENT"
      val parent = new TestProp
      parent.value = parentValue
      val child = parent.derive()
      parent.value should equal(parentValue)
      child.value should equal(parentValue)
    }

    it("recursively propagates its value to children") {
      val parentValue = "PARENT"
      val parent = new TestProp
      val child = parent.derive()
      val grandchild = child.derive()
      parent.value = parentValue
      parent.value should equal(parentValue)
      child.value should equal(parentValue)
      grandchild.value should equal(parentValue)
    }

    it("can change its value independently") {
      val parentValue = "PARENT"
      val childValue = "CHILD"
      val grandChildValue = "GRANDCHILD"
      val parent = new TestProp
      val child = parent.derive()
      val grandchild = child.derive()
      parent.value = parentValue
      child.value = childValue
      grandchild.value = grandChildValue
      parent.value should equal(parentValue)
      child.value should equal(childValue)
      grandchild.value should equal(grandChildValue)
    }


    /*
    it("publishes change events") {
      //FIXME
      println("FIXME: don't know how to do this yet")
    }
    */
  }

  describe("A SimpleVisualizationFactory") {

    it("can be instantiated") {
      val context = new TestVisualizable {
        //FIXME
        override def parentVisualizationProvider: VisualizationProvider[VisualizationTests.this.type#TestVisualizable] = ???
      }
    }
  }
}
