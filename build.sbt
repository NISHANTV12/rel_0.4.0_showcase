lazy val `sample-assembly` = project
  .settings(
    libraryDependencies ++= Dependencies.SampleAssembly
  )

lazy val `sample-hcd` = project
  .settings(
    libraryDependencies ++= Dependencies.SampleHcd
  )

lazy val `sample-deploy` = project
  .dependsOn(
    `sample-assembly`,
    `sample-hcd`
  )
  .enablePlugins(JavaAppPackaging, CswBuildInfo)
  .settings(
    libraryDependencies ++= Dependencies.SampleDeploy
  )
