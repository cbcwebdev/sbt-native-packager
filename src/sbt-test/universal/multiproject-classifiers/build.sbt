import com.typesafe.sbt.packager.Compat._

lazy val appVersion = "1.0"

lazy val mySettings: Seq[Setting[_]] =
  Seq(organization := "org.test", version := appVersion, TaskKey[Unit]("showFiles") := {
    System.out.synchronized {
      println("Files in [" + name.value + "]")
      val files = (target.value / "universal/stage").**(AllPassFilter).get
      files foreach println
    }
  })

lazy val assets = config("assets")

lazy val sub = project
  .in(file("sub"))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .settings(
    ivyConfigurations += assets,
    artifact in assets := artifact.value.withClassifier(classifier = Some("assets")),
    packagedArtifacts += {
      val file = target.value / "assets.jar"
      val assetsDir = baseDirectory.value / "src" / "main" / "assets"
      val sources = assetsDir.**(AllPassFilter).filter(_.isFile) pair (file => IO.relativize(assetsDir, file))
      IO.zip(sources, file)
      (artifact in assets).value -> file
    },
    exportedProducts in assets := {
      Seq(
	Attributed
	  .blank(baseDirectory.value / "src" / "main" / "assets")
	  .put(artifact.key, (artifact in assets).value)
	  .put(AttributeKey[ModuleID]("module-id"), projectID.value)
      )
    }
  )

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(mySettings)
  .dependsOn(sub % "compile->compile;compile->assets")
