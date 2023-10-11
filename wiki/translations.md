[Home](home.md)

# How to Add Translations to Project MMO
Project MMO now uses Data Generation to create language files.  this means that the files are generated from code into the final JSON files you see in the jar.  This means that contributing requires editing a code file.  However, this is much easier than it sounds.

## The `LangProvider` file.
To start, here is [The Full File](../src/main/java/harmonised/pmmo/setup/datagen/LangProvider.java).

Each translatable line will have:
```java
public static Translation TRANSLATION_NAME = Translation.Builder.start("pmmo.something.something")
    .addLocale(Locale.EN_US, "The translated text").build();
```
to add your own translation, add your locale with a new `.addLocale` line.  example:
```java
public static Translation TRANSLATION_NAME = Translation.Builder.start("pmmo.something.something")
    .addLocale(Locale.XX_XX, "The text in your language") //XX_XX is your language code, like "EN_US" is US English.
    .addLocale(Locale.EN_US, "The translated text").build();
```
And that's it!  repeat for each translation you want to add and then submit.

*There is a section at the top of the file with existing `Locale` values if you need to reference them.  You can add your locale if it does not exist.*

## How to provide your translations once finished
There are three ways.  you can provide the raw `LangProvider.java` text file in discord, attached to an issue on GitHub, or you can submit a PR.

### <u>Providing Raw Files</u>
If you save a copy of the `LangProvider.java` file, you can edit it with any text editor.  Once saved, you can ping @PMMODev in discord and attach your file, or you can attach your file as an issue in GitHub.  The developers will copy your changes over and validate them before adding to the mod.

### <u>Submitting via PR</u>
Another alternative is to "fork" the pmmo repository on GitHub.  This will create a copy of code on your account, which you can then edit.  Github has a pretty nice in-browser editor which you can use to add your translations.  At the bottom of the edit page is a "commit" button, which you can use to save your progress.  Once you are satisfied with all of your changes, GitHub should prompt you to make a pull request.  Click this button, fill out the required fields and submit.  Your PR will be reviewed by the developers and merged once validated.

### <u>Validating Translations</u>
Because there are trolls and other kinds of bad actors out there, all translations have to be validated by another person who speaks the language.  This can take some time and may result in your translations not being added for a while.  We expect that most contributors are acting in good faith, but we have to be sure so as not to offend anyone.  Especially since there are specific cultural nuances that we as non-speakers of the language may not be aware of.  I am sure you would not want to play with a mod that used a slur or slang term that was inappropriate, so I hope you understand why we take this extra precautionary step.

[Home](home.md)