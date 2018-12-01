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


Then, for each line, it will create a black slide with the text formatted over it. Here is an example slide:

![slide_example](https://i.imgur.com/XUOIEQg.png)

In order to add furigana to text, just wrap any kanji like so `[Kanji|Reading]`

For example, the example song's Japanese lyric file contained the line:

`[明|あ]け[渡|わた]してはいけない[場所|ばしょ] それを[心|こころ]と[呼|よ]ぶんでしょ`

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
