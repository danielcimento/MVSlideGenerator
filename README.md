# MVSlideGenerator
A GUI application built to facilitate the making of slides for lyric videos. More specifically, English/Japanese lyric videos.

I built this tool after making two lyric videos for songs I enjoyed ([オーダーメイド](https://www.youtube.com/watch?v=ApZc9MyTsi4) and [つじつま合わせに生まれた僕等](https://www.youtube.com/watch?v=RL7arkNfnuA))

An example of a lyric video made with this tool can be found here:

[amazarashi - 古いSF映画 (英訳＋歌詞) || "Old Sci-Fi Movie" + English Lyrics/Furigana](https://www.youtube.com/watch?v=ucESadsZxpw)

(Note: Unfortunately, due to copyright reasons, this video is not available in Japan)

This tool doesn't only have to be used for Japanese with furigana, as long as you want to position smaller text over other text.

## Download and Installation

This project can be compiled to a .jar file using the `sbt assembly` command, which the project depends on. 

After building the jar, it can be run normally to open the application.

When the application runs, it will save a settings file in your appdata directory. These settings can be manually edited, but a settings menu is on the way soon.

## Usage

When the app is run, it will open a window that looks like this:

![new_app](https://imgur.com/gIks3YA.png)

Clicking one of the "Load File" buttons on the left will allow you to pick a file containing the lines that go on the top and bottom of the image.

![opening_text](https://imgur.com/NHvoes5.png)

Once you've selected the lyric file, it will fill the text view on the left.

![text_view](https://imgur.com/X1BEx6f.png)

The same is true for the bottom section, but the bottom text area will allow you to add furigana to the line.

If you want furigana to Japanese text, in the input file, wrap any kanji in this way: `[Kanji|Reading]`

For example, the example song's Japanese lyric file contained the line:

`アダムにとって[知恵|ちえ]の[樹|き]の[実|み]とは`

When you load the file, you'll know you have succeeded, as the furigana formatting will not be shown, and the words which will have furigana are highlighted.

![text_view_2](https://imgur.com/TeNZhSv.png)

Once you've loaded both files, clicking any of the lines will show a preview of the slide that will be created for it.

![previews](https://imgur.com/r4xsnxS.png)

By default, the images will use the largest font that keeps the text within the specified boundaries, but you can reduce the font size for aesthetic reasons using the font sliders below each file area.

![fonts](https://imgur.com/si3G9js.png)

All that's left is to type in the output path where you want the images to be saved. This can be entered manually or chosen using the directory selector.

![directories](https://imgur.com/bo43v50.png)

Once that's done, just click create slides, and it will create all the images (there is a short delay of lag (~3 seconds) when creating the image. I've tried to remove the lag by creating the images in the background but there are technical reasons why that isn't possible.

For each line in the input files, the app will create a black slide with the text formatted over it. Here is an example slide:

![slide_example](https://i.imgur.com/XUOIEQg.png)

This tool also features a number of additional parameters and flags. The following is an attempt at a comprehensive list:

```
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
```
