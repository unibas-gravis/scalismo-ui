import de.heikoseeberger.sbtheader.{AutomateHeaderPlugin, HeaderPlugin, HeaderPattern}
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.headers
import de.heikoseeberger.sbtheader.license.GPLv3
import de.heikoseeberger.sbtheader.CommentStyleMapping._

headers := createFrom(GPLv3, "2016", "University of Basel, Graphics and Vision Research Group ")

lazy val sbtHeaderTest = project.in(file(".")).enablePlugins(AutomateHeaderPlugin)




