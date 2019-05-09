import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import ui.ApplicationScene


import com.typesafe.scalalogging.LazyLogging
import model.RunConfig

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[MVSlideGenerator], args: _*)
  }

  class MVSlideGenerator extends Application with LazyLogging {
    // We use JavaFX application procedures, since we need to initialize the graphics context
    override def start(primaryStage: Stage): Unit = {
      implicit val mainStage: Stage = primaryStage
      implicit val rc: RunConfig = model.RunConfig.getUserConfig

      val defaultScene: Scene = new Scene(new ApplicationScene, 1920, 1080)
      primaryStage.setScene(defaultScene)
      primaryStage.setTitle("MV Slide Generator")
      primaryStage.setMaximized(true)
      primaryStage.setResizable(true)
      primaryStage.show()

      logger.debug(rc.toString)
    }
  }
}
