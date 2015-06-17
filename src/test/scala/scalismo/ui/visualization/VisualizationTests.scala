package scalismo.ui.visualization

import org.scalatest._

class VisualizationTests extends FunSpec with Matchers {

  object TestProp {
    val DefaultValue: String = "???"
  }
  class TestProp extends VisualizationProperty[String, TestProp] {
    val defaultValue: String = TestProp.DefaultValue
    def newInstance() = new TestProp
  }

  /* This method *is* time-sensitive. Make sure you give the garbage collector some time.
  * Something like (at least) 100 ms seems to be sensible.
  */
  def garbageCollectAnd[R](f: => R, gracePeriodMs: Long = 1000): R = {
    System.gc()
    Thread.sleep(gracePeriodMs)
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

      grandgrandchild.derived(true).length shouldBe 0
      grandchild2.derived(true).length shouldBe 1
      grandchild1.derived(true).length shouldBe 0
      child.derived(true).length shouldBe 2
      parent.derived(true).length shouldBe 1

      grandchild1 = null
      garbageCollectAnd(child.derived(true)).length shouldBe 1
      grandchild2.derived(true).length shouldBe 1

      grandchild2 = null
      // this should also garbage-collect the grand-grandchild,
      // even if it could still be referenced somewhere else
      garbageCollectAnd(child.derived(true)).length shouldBe 0

      garbageCollectAnd(parent.derived(true)).length shouldBe 1
      child = null
      garbageCollectAnd(parent.derived(true)).length shouldBe 0
    }
  }
}
