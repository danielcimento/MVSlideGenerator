import javafx.application.Application
import javafx.scene.control.Button
import javafx.scene.{Group, Scene}
import javafx.scene.image.Image
import javafx.scene.layout.GridPane
import javafx.stage.Stage
import ui.ApplicationScene
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment

import scala.collection.JavaConverters._
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

import model.RunConfig.Keys._

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[MVSlideGenerator], args: _*)
  }

  class MVSlideGenerator extends Application {
    // We use JavaFX application procedures, since we need to initialize the graphics context
    override def start(primaryStage: Stage): Unit = {
      implicit val mainStage = primaryStage
      val rc = model.RunConfig.getUserConfig()

      val appScene = new ApplicationScene(rc)
      val defaultScene: Scene = new Scene(appScene, 1920, 1080)
      primaryStage.setScene(defaultScene)
      primaryStage.setTitle("MV Slide Generator")
      primaryStage.setMaximized(true)
      primaryStage.setResizable(true)
      primaryStage.show()

//      val logger = GlobalContext.logger
////      logger.debug(rc.toString)
//
//      val (engFilename, jpFilename) = ("src/eng_lyrics", "src/jp_lyrics")
//

////      System.exit(0)
    }
  }
}
