# MVSlideGenerator
A GUI application built to facilitate the making of slides for lyric videos. More specifically, English/Japanese lyric videos.

I built this tool after making two lyric videos for songs I enjoyed ([オーダーメイド](https://www.youtube.com/watch?v=ApZc9MyTsi4) and [つじつま合わせに生まれた僕等](https://www.youtube.com/watch?v=RL7arkNfnuA))

An example of a lyric video made with this tool can be found here:

[amazarashi - 古いSF映画 (英訳＋歌詞) || "Old Sci-Fi Movie" + English Lyrics/Furigana](https://www.youtube.com/watch?v=ucESadsZxpw)

(Note: Unfortunately, due to copyright reasons, this video is not available in Japan)

This tool doesn't only have to be used for Japanese with furigana, as long as you want to position smaller text over other text.

## Download and Installation

This project can be compiled to a .jar file using the `sbt assembly` command, which the project depends on. 

In the command's output, it will tell you where it's put the file. In my case, it was `target\scala-2.12\MVSlideGenerator-assembly-0.1.jar`

If you don't know how to run `sbt` programs, you can download a pre-assembled version of the jar [here](https://drive.google.com/open?id=1fwNmWAa9OaZGlzbWeUAf1Qjl8yTcu7cs), though it will always be most up-to-date when compiled manually, and the pre-assembled jar isn't guaranteed to run on all systems (though I would expect it to)

After acquiring the .jar, it can be run normally to open the application. If you have the Java Runtime Environment installed, you should be able to just double-click the file.

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

All that's left is to specify the output path where you want the images to be saved. This can be entered manually or chosen using the directory selector. You must input something, or the app will refuse to create the images.

![directories](https://imgur.com/bo43v50.png)

Once that's done, just click create slides, and it will create all the images. There is a short delay of lag (~3 seconds) when creating the image. I've tried to remove the lag by creating the images in the background but there are technical reasons why that isn't possible.

For each line in the input files, the app will create a black slide with the text formatted over it. Here is an example slide:

![slide_example](https://imgur.com/LH7Emic.png)
