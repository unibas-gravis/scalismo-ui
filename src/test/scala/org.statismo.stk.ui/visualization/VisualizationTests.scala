package org.statismo.stk.ui.visualization

import org.scalatest._
import org.statismo.stk.ui.{Scene, Viewport}
import scala.collection.immutable
import scala.util.{Failure, Success}

class VisualizationTests  extends FunSpec with Matchers {

  object TestProp {
    val DefaultValue: String = "???"
  }
  class TestProp extends VisualizationProperty[String, TestProp] {
    val defaultValue: String = TestProp.DefaultValue
    def newInstance() = new TestProp
  }

  class DummyViewport extends Viewport {
    override def scene: Scene = ???
  }

  object TestVisualizableFactory extends SimpleVisualizationFactory[TestVisualizable] {
    def put(kv: (String, immutable.Seq[Visualization[TestVisualizable]])) = visualizations += kv
  }

  class TestVisualizable extends VisualizableSceneTreeObject[TestVisualizable] {
    override def parent = new Scene()
    override def visualizationProvider: VisualizationProvider[TestVisualizable] = TestVisualizableFactory
  }

  class TestViewport1 extends DummyViewport
  class TestViewport2 extends DummyViewport

  /* This method *is* time-sensitive. Make sure you give the garbage collector some time.
  * Something like (at least) 100 ms seems to be sensible.
  */
  def garbageCollectAnd[R] (f: => R, gracePeriodMs: Long = 500) : R = {
    System.gc()
    Thread.sleep(gracePeriodMs)
    System.gc()
    val r: R = f
    r
  }

  describe("A VisualizationProperty") {

    it("returns its default value after instantiation") {
      val t = new TestProp
      t.value should equal(TestProp.DefaultValue)
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

    it("is correctly garbage-collected when not referenced") {
      val parentValue = "PARENT"
      val childValue = "CHILD"
      val grandChildValue = "GRANDCHILD"
      val parent = new TestProp
      var child = parent.derive()
      var grandchild1 = child.derive()
      var grandchild2 = child.derive()
      val grandgrandchild = grandchild2.derive()
      parent.value = parentValue
      child.value = childValue
      grandchild1.value = grandChildValue

      grandgrandchild.derived.length shouldBe 0
      grandchild2.derived.length shouldBe 1
      grandchild1.derived.length shouldBe 0
      child.derived.length shouldBe 2
      parent.derived.length shouldBe 1

      grandchild1 = null
      garbageCollectAnd(child.derived).length shouldBe 1
      grandchild2.derived.length shouldBe 1

      grandchild2 = null
      // this should also garbage-collect the grand-grandchild,
      // even if it could still be referenced somewhere else
      garbageCollectAnd(child.derived).length shouldBe 0

      garbageCollectAnd(parent.derived).length shouldBe 1
      child = null
      garbageCollectAnd(parent.derived).length shouldBe 0
    }
  }

  describe("A Visualizations object") {

    it("returns visualizations only for registered viewport classes") {
      class TestVisualization extends Visualization[TestVisualizable] {
        override protected def createDerived()  = new TestVisualization

        override def instantiateRenderables(target: TestVisualizable) = Nil
      }

      val target = new TestVisualizable
      val vis = new Visualizations
      val vp1 = new TestViewport1
      val vp2 = new TestViewport2
      TestVisualizableFactory.put((vp1.getClass.getCanonicalName, immutable.Seq(new TestVisualization)))
      vis.tryGet(target, vp1) shouldBe a [Success[_]]
      vis.tryGet(target, vp2) shouldBe a [Failure[_]]

      val check: TestVisualization = vis.getUnsafe(target, vp1)
      check should not be null

      val generic = target.asInstanceOf[Visualizable[_]]
      vis.tryGet(generic, vp1.getClass.getCanonicalName) shouldBe a [Success[_]]
    }
  }
}
