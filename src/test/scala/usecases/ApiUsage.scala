package usecases

import java.awt.Color

import scalismo.geometry.Point
import scalismo.ui.StaticPointCloud
import scalismo.ui.api.SimpleAPI

import scala.collection.immutable

object ApiUsage {
  def main(a: Array[String]): Unit = {
    scalismo.initialize()
    val ui = SimpleAPI.ScalismoUI()

    val points = immutable.IndexedSeq(Point(0, 0, 0), Point(0, 100, 0), Point(100, 100, 0), Point(100, 0, 0), Point(0, 0, 100), Point(0, 100, 100), Point(100, 100, 100), Point(100, 0, 100))
    ui.showPointCloud(points, "PC")
    val pc = ui.scene.find[StaticPointCloud]().head
    pc.color.update(Color.BLUE)
  }
}
