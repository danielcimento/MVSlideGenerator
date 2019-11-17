package ui.utils

import java.io.File

trait LastAccessedFileProvider {
  def getLastAccessedFile: Option[File]

  def updateLastAccessedFile(file: File): Unit
}
