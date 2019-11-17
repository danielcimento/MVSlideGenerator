import java.io.{File, FileOutputStream, PrintStream}

import javafx.application.{Application, Platform}
import javafx.scene.Scene
import javafx.stage.Stage
import ui.ApplicationScene
import com.typesafe.scalalogging.LazyLogging
import model.RunConfig
import model.RunConfig.getUserAppDataFolder
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

import javafx.scene.image.{Image, ImageView}
import ui.error.ErrorController


object Main extends App with LazyLogging {
  val oldErr = System.err

  override def main(args: Array[String]): Unit = {
    logger.info("Starting program.")

    Application.launch(classOf[MVSlideGenerator], args: _*)
  }

  class MVSlideGenerator extends Application with LazyLogging {
    val versionNumber = "1.0.0"

    // We use JavaFX application procedures, since we need to initialize the graphics context
    override def start(primaryStage: Stage): Unit = {
      Thread.setDefaultUncaughtExceptionHandler(MVSlideGenerator.showError)

      implicit val mainStage: Stage = primaryStage
      implicit val rc: RunConfig = model.RunConfig.getUserConfig

      val defaultScene: Scene = new Scene(new ApplicationScene, 1920, 1080)
      primaryStage.setScene(defaultScene)
      primaryStage.setTitle(s"MV Slide Generator v$versionNumber")
      primaryStage.setMaximized(true)
      primaryStage.setResizable(true)
      primaryStage.getIcons.add(new Image(getClass.getClassLoader.getResourceAsStream("icon.png"), 256, 256, true, true))
      primaryStage.show()

      logger.info(s"Using RunConfig: ${rc.toString}")
    }
  }

  object MVSlideGenerator {
    private def showError(t: Thread, e: Throwable): Unit = {
      System.err.println("***Default exception handler***")
      if (Platform.isFxApplicationThread) showErrorDialog(e)
      else System.err.println("An unexpected error occurred in " + t)
    }

    private def showErrorDialog(e: Throwable): Unit = {
      val errorMsg = new StringWriter
      e.printStackTrace(new PrintWriter(errorMsg))
      val dialog = new Stage
      dialog.initModality(Modality.APPLICATION_MODAL)
      val loader = new FXMLLoader(classOf[MVSlideGenerator].getResource("Error.fxml"))
      try {
        val root = loader.load
        loader.getController.asInstanceOf[ErrorController].setErrorText(errorMsg.toString)
        dialog.setScene(new Scene(root, 250, 400))
        dialog.show()
      } catch {
        case exc: IOException =>
          exc.printStackTrace()
      }
    }
  }
}
