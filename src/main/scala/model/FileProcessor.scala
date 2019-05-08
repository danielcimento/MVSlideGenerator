package model

import java.io.File

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javax.imageio.ImageIO

object FileProcessor {
  def saveImageToFile(img: Image, outputPath: String, filename: String): Unit = {
    val filePath = new File(new File(outputPath), filename)
    filePath.getParentFile.mkdirs
    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", filePath)
  }
}
