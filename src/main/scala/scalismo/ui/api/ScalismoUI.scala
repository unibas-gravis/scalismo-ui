package scalismo.ui.api

import java.awt.Color
import java.io.File

import scalismo.geometry.{Point, _3D}
import scalismo.io.{ImageIO, StatismoIO, MeshIO}
import scalismo.ui.util.EdtUtil
import scalismo.ui.view.ScalismoFrame

import scala.swing.Button


class ScalismoUI extends ScalismoFrame() with SimpleAPI {

  override def setup(args: Array[String]): Unit = {
    super.setup(args)
  }


}

object ScalismoUI {

  def apply(): ScalismoUI = {
    scalismo.initialize()
    val ui = EdtUtil.onEdtWait{
      val theui = new ScalismoUI()
      theui.setup(Array[String]())
      theui.pack()
      theui.visible = true

      theui
    }
    ui
  }
}



object ScalismoUITest {
  def main(args: Array[String]): Unit = {


    val ui = ScalismoUI()

    val meshGroup = ui.createGroup("group1")
    ui.onNodeAdded(meshGroup, (v : LandmarkView) => v.color = Color.GREEN)

    val mesh = MeshIO.readMesh(new File("/tmp/mesh.stl")).get
    ui.show(mesh, "abc", meshGroup)
//
//    ui.addTransformation(meshGroup, (p : Point[_3D]) => p + scalismo.geometry.Vector(1f, 20f, 30f), name = "tmy trans")

//    val meshView = ui.show(mesh, name = "abc", group = meshGroup)
//    meshView.color = Color.RED
//
//    val modelGroup = ui.createGroup("modelGroup")
//    val model = StatismoIO.readStatismoMeshModel(new File("/tmp/model.h5")).get
//    val smView = ui.show(model, "abc", modelGroup)
//
//    smView.shapeTransformation.coefficients = smView.shapeTransformation.coefficients + 1.0f
//
//    val mesh2 = ui.find((p : TriangleMeshView) => p.color == Color.RED).get
//    mesh2.remove()


//
//    ui.onNodeRemoved(meshGroup, (v : TriangleMeshView) => println(s"node with name ${v.name} removed"))
//
//    val meshView2 = ui.show(mesh, "cde", meshGroup)
//
//    meshView2.remove()
//

    //    val meshView = ui.show(mesh, "a mesh", meshGroup)
    //    meshView.color= Color.RED
    //
    //
    //    val modelGroup = ui.createGroup("modelGroup")
    //
    //    val zeroMean = VectorField(RealSpace[_3D], (_ : Point[_3D]) => Vector3D(0,0,0))
    //    val gp = GaussianProcess(zeroMean, DiagonalKernel[_3D](GaussianKernel(100) * 100))
    //    val lowRankGP = LowRankGaussianProcess.approximateGP(gp, UniformMeshSampler3D(mesh, 500, 45), 100)
    //    ui.addTransformation(modelGroup, lowRankGP.discretize(mesh), "gptransform")
    //    val group = ui.find[Group](_ => true).get
    //  //
    //    //  println(group.name)
    //
    //ui.addTransformation(meshGroup, (p : Point[_3D]) => p + p.toVector * 0.1, "add")


    //    val model = StatismoIO.readStatismoMeshModel(new File("/tmp/model.h5")).get
    //    val smView = ui.show(model, "abc", modelGroup)

    ui.addToToolbar(new Button("my button"))
    //

    //    val smViewFound =ui.find[DiscreteLowRankGPTransformationView](_ => true).get
    //
    //    smViewFound.coefficients = DenseVector.ones[Float](model.rank)
    //

    //    val tview = ui.addTransformation(group, t)
    //    tview.coefficients =


    //    val scalarArrayData = mesh.points.map(p => if 1(p.x < 0) 1.toShort else 0.toShort).toArray
    //    val smf = ScalarMeshField(mesh, ScalarArray[Short](scalarArrayData))
    //    val aGroup = ui.createGroup("a Group")
    //    val meshNode2 = ui.show(smf, "name", aGroup)
    //
    //
    //    val s = ui.find[TriangleMeshView]((s : TriangleMeshView) => true).get
    //    s.color = Color.RED
    //
    //
    //    val lms = Seq(Landmark("abc", Point3D(1f, 1f, 1f)), Landmark("cde", Point3D(2f, 2f, 2f)))
    //
    //    val g = ui.find[Group]((g : Group) => g.name == "abc")
    //
    //    for (lm <- lms) ui.show(lm, lm.id, aGroup)
    //
    //    val lmNodes = ui.filter[LandmarkView]((_ : LandmarkView) => true, aGroup)
    //    for (lmNode <- lmNodes) {lmNode.color = Color.BLUE}
    //    ui.message("abc")
    //    ui.message("xxx")

  }
}
