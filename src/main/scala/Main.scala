import javafx.application.Application
import javafx.scene.image.Image
import javafx.stage.Stage

import scala.collection.JavaConverters._
import scala.reflect.internal.util.ScalaClassLoader.URLClassLoader

object Main extends App {
  override def main(args: Array[String]): Unit = {
    Application.launch(classOf[MVSlideGenerator], args: _*)
  }

  class MVSlideGenerator extends Application {
    // We use JavaFX application procedures, since we need to initialize the graphics context
    override def start(primaryStage: Stage): Unit = {
      val logger = GlobalContext.logger
      val rc = RunConfig.gatherRuntimeConfiguration(getParameters.getRaw.asScala.toList)
      logger.debug(rc.toString)
      if(GlobalContext.conf.getBoolean("test.only.argv")) {
        System.exit(0)
      }

      val (engFilename, jpFilename) = (rc.englishFilename, rc.japaneseFilename)

      // First, we open each file and get the lines
      val (engLines, jpLines): (List[String], List[String]) = FileProcessor.getFileLines(engFilename, jpFilename)

      val japaneseImages = TextRenderer.convertJapaneseLinesToImages(jpLines, rc)
      val englishImages = TextRenderer.convertEnglishLinesToImages(engLines, rc)

      val engJpImages = englishImages.zip(japaneseImages)

      // Then, we take each English and Japanese image and paint them on top of a black canvas. Then we save the files sequentially
      var imgsCreated = 1
      engJpImages.foreach {
        case (engImage, jpImage) =>
          val jointImg = GraphicsRenderer.paintText(engImage, jpImage, rc.resolutionWidth, rc.resolutionHeight, rc.ppi)
          FileProcessor.saveImageToFile(jointImg, "image%04d.png".format(imgsCreated))
          imgsCreated += 1
      }

      System.exit(0)
    }
  }
}
