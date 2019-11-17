package ui.utils

import java.io.File

import javafx.beans.property.{ObjectProperty, StringProperty}
import javafx.collections.ObservableList
import javafx.stage.{FileChooser, Window}
import javafx.stage.FileChooser.ExtensionFilter

import scala.collection.JavaConverters._

/**
  * A utility wrapper for the JavaFX FileChooser class which, among other convenience functions, makes it easy to
  * synchronize file choosers to show the most recently opened file on open
  * @param lastAccessedFileSource A re-usable provider of the file most recently accessed by file choosers
  */
class StickyFileChooser(lastAccessedFileSource: LastAccessedFileProvider) {
  protected val _delegate = new FileChooser

  def getExtensionFilters: ObservableList[ExtensionFilter] = {
    _delegate.getExtensionFilters
  }

  def getSelectedExtensionFilter: ExtensionFilter = {
    _delegate.getSelectedExtensionFilter
  }

  def setSelectedExtensionFilter(value: ExtensionFilter): Unit = {
    _delegate.setSelectedExtensionFilter(value)
  }

  def withSelectedExtensionFilter(value: ExtensionFilter): StickyFileChooser = {
    _delegate.setSelectedExtensionFilter(value)
    this
  }

  def getTitle: String = {
    _delegate.getTitle
  }

  def setTitle(value: String): Unit = {
    _delegate.setTitle(value)
  }

  def withTitle(value: String): StickyFileChooser = {
    _delegate.setTitle(value)
    this
  }

  def initialDirectoryProperty(): ObjectProperty[File] = {
    _delegate.initialDirectoryProperty()
  }

  def initialFileNameProperty(): ObjectProperty[String] = {
    _delegate.initialFileNameProperty()
  }

  def selectedExtensionFilterProperty(): ObjectProperty[ExtensionFilter] = {
    _delegate.selectedExtensionFilterProperty()
  }

  private def populateLastAccessedFile(): Unit = {
    lastAccessedFileSource.getLastAccessedFile match {
      case Some(file) if file.exists() =>
        if (!file.isDirectory) {
          println(file.getName)
          _delegate.setInitialFileName(file.getName)
        } else {
          _delegate.setInitialFileName("")
        }
        var fileToUse = file
        while(!fileToUse.isDirectory) {
          fileToUse = fileToUse.getParentFile
        }
        _delegate.setInitialDirectory(fileToUse)
      case _ => _delegate.setInitialDirectory(null)
    }
  }

  def showOpenDialog(window: Window): Option[File] = {
    populateLastAccessedFile()
    val delegationResult = _delegate.showOpenDialog(window)
    if(delegationResult == null || !delegationResult.exists()) {
      None
    } else {
      lastAccessedFileSource.updateLastAccessedFile(delegationResult)
      Some(delegationResult)
    }
  }

  def showOpenMultipleDialog(window: Window): List[File] = {
    populateLastAccessedFile()
    val delegationResult = _delegate.showOpenMultipleDialog(window).asScala.toList
    if(delegationResult == null) {
      List()
    } else {
      lastAccessedFileSource.updateLastAccessedFile(delegationResult.last)
      delegationResult
    }
  }

  def showSaveDialog(window: Window): Option[File] = {
    populateLastAccessedFile()
    val delegationResult = _delegate.showSaveDialog(window)
    if(delegationResult == null || !delegationResult.exists()) {
      None
    } else {
      lastAccessedFileSource.updateLastAccessedFile(delegationResult)
      Some(delegationResult)
    }
  }

  def titleProperty(): StringProperty = {
    _delegate.titleProperty()
  }
}
