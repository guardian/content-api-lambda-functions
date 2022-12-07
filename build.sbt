lazy val awsSdk = "1.12.359"
lazy val commonSettings = Seq(
  organization := "com.gu",
  scalaVersion := "2.12.17",
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies ++= Seq(
    "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
    "com.squareup.okhttp" % "okhttp" % "2.5.0",
    "com.google.guava" % "guava" % "18.0",
    "org.scalatest" %% "scalatest" % "3.2.14" % "test"
  )
)

lazy val `crossword-xml-uploader` = (project in file("crossword-xml-uploader"))
  .settings(commonSettings)
  .settings(
    description := "AWS Lambda to upload crossword xml files.",
    assemblyJarName := "crossword-xml-uploader.jar",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdk, // FIXME upgrade and merge with pdf-uploader's dependency declaration
      "com.amazonaws" % "aws-java-sdk-kinesis" % awsSdk,
      "org.scala-lang.modules" %% "scala-xml" % "1.3.0"
    ),
    assembly / test := (Test / test).value,
  )

lazy val `crossword-pdf-uploader` = (project in file("crossword-pdf-uploader"))
  .settings(commonSettings)
  .settings(
    description := "AWS Lambda to upload crossword pdf files.",
    assemblyJarName := "crossword-pdf-uploader.jar",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdk
    ),
    assembly / test := (Test / test).value,
  )


lazy val root = (project in file("."))
  .aggregate(`crossword-xml-uploader`, `crossword-pdf-uploader`)
