lazy val awsSdk = "1.12.359"
lazy val commonSettings = Seq(
  organization := "com.gu",
  scalaVersion := "2.13.10",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
    "com.amazonaws" % "aws-java-sdk-s3" % awsSdk,
    "com.squareup.okhttp3" % "okhttp" % "4.10.0",
    "com.google.guava" % "guava" % "31.1-jre",
    "org.scalatest" %% "scalatest" % "3.2.14" % "test"
  ),
  assembly / test := (Test / test).value,
  assembly / assemblyMergeStrategy := {
    case PathList(ps @ _*) if ps.last == "module-info.class" => MergeStrategy.discard
    case path => MergeStrategy.defaultMergeStrategy(path)
  }
)

lazy val `crossword-xml-uploader` = (project in file("crossword-xml-uploader"))
  .settings(commonSettings)
  .settings(
    description := "AWS Lambda to upload crossword xml files.",
    assemblyJarName := "crossword-xml-uploader.jar",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-kinesis" % awsSdk,
      "org.scala-lang.modules" %% "scala-xml" % "2.1.0"
    ),
  )

lazy val `crossword-pdf-uploader` = (project in file("crossword-pdf-uploader"))
  .settings(commonSettings)
  .settings(
    description := "AWS Lambda to upload crossword pdf files.",
    assemblyJarName := "crossword-pdf-uploader.jar",
    libraryDependencies ++= Seq(),
  )


lazy val root = (project in file("."))
  .aggregate(`crossword-xml-uploader`, `crossword-pdf-uploader`)
