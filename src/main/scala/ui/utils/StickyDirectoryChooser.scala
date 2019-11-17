package ui.utils

import java.io.File

import javafx.beans.property.{ObjectProperty, StringProperty}
import javafx.stage.FileChooser.ExtensionFilter
import javafx.stage.{DirectoryChooser, FileChooser, Window}

class StickyDirectoryChooser(lastAccessedFileProvider: LastAccessedFileProvider) {
  protected val _delegate = new DirectoryChooser

  def getTitle: String = {
    _delegate.getTitle
  }

  def setTitle(value: String): Unit = {
    _delegate.setTitle(value)
  }

  def withTitle(value: String): StickyDirectoryChooser = {
    _delegate.setTitle(value)
    this
  }

  def initialDirectoryProperty(): ObjectProperty[File] = {
    _delegate.initialDirectoryProperty()
  }

  private def populateLastAccessedFile(): Unit = {
    lastAccessedFileProvider.getLastAccessedFile match {
      case Some(file) if file.exists() =>
        var fileToUse = file
        while(!fileToUse.isDirectory) {
          fileToUse = fileToUse.getParentFile
        }
        _delegate.setInitialDirectory(fileToUse)
      case _ => _delegate.setInitialDirectory(null)
    }
  }

  def showDialog(window: Window): Option[File] = {
    populateLastAccessedFile()
    val delegationResult = _delegate.showDialog(window)
    if(delegationResult == null || !delegationResult.exists()) {
      None
    } else {
      lastAccessedFileProvider.updateLastAccessedFile(delegationResult)
      Some(delegationResult)
    }
  }

  def titleProperty(): StringProperty = {
    _delegate.titleProperty()
  }}
