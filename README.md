# MVSlideGenerator
A "command line" application built to facilitate the making of slides for lyric videos. More specifically, English/Japanese lyric videos.

## Download and Installation

This project can be compiled to a .jar file using the `sbt assembly` command, which the project depends on. This .jar can then be run normally, but I prefer to move the package to a bin folder and add the following line to my .bashrc:

`alias lyric-create="java -jar ~/Documents/Code/bin/mv-slide-generator.jar"`

After doing so, the command can be run simply by typing:

`lyric-create english_in_file japanese_in_file`

## Usage

This tool also features a number of additional parameters and flags. The following is an attempt at a comprehensive list:

'''
Usage: lyric-create <english-input-file> <japanese-input-file>
   Command line arguments:
     -e <font-min> <font-max> : Sets the English font size range (Default: 80 to 100)
     -j <font-min> <font-max> : Sets the Japanese font size range (Default: 70 to 100)
     -r <width> <height> : Sets the resulting image resolution (Default: 1920 by 1080)
     -p <padding> : Sets the horizontal padding around lines (Default: 50 pixels)
     -P <ppi> : Sets the ppi (Default: 72ppi)
     -f <typeface> : Sets the typeface. See note 1 (Default: Meiryo)
     -l <spacing> : Sets the line spacing for English text (Default: 50 pixels)
     -F <spacing> : Sets the spacing between Kanji and Furigana (Default: 15 pixels)
   Note 1: This application produced bolded text on slides, so if the font given does not have a bold counterpart
   then errors or unexpected behavior may occur during rendering.
'''
