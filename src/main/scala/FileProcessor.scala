package main.scala

import java.io.File
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javax.imageio.ImageIO

import scala.io.Source

object FileProcessor {
  def getFileLines(filePath1: String, filePath2: String): (List[String], List[String]) = {
    val lines1 = Source.fromFile(filePath1, "UTF-8").getLines().toList
    val lines2 = Source.fromFile(filePath2, "UTF-8").getLines().toList
    (lines1, lines2)
  }

  def saveImageToFile(img: Image, filename: String): Unit = {
    val filePath = new File(System.getProperty("user.dir") + "\\output_files\\" + filename)
    filePath.getParentFile.mkdirs
    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", filePath)
  }

}

