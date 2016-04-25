### Code quality and formatting ###

To ensure consistent code quality and formatting across all users and commits, please do the following before every commit:

1. In Idea, right-click on the project sources, and select "Analyze -> Inspect Code". Fix all warnings you deem relevant.
2. In Idea, right-click on the project, and select "Reformat Code". Make sure "Optimize imports" is selected, and "Rearrange entries" is not selected.
3. Once the Idea reformatting is done, run ``` sbt clean compile test ``` on the console.
4. Commit.

The above assumes that Idea simply uses the built-in default settings for analyzing and formatting.

### Publishing instructions ###


1. ``` sbt +assembly ```
2. ``` scp target/scala-2.11/statismo-ui.jar statismo@statismo.cs.unibas.ch:/var/www/statismo.cs.unibas.ch/viewer/[versionnumber]/  ```
3. Edit the [Statismo Wiki](https://github.com/statismo/statismo/wiki/Statismo%20Viewer) accordingly.