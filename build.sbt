scalaVersion in ThisBuild := "2.12.6"
scalacOptions in ThisBuild ++= Seq(
  "-language:_",
	"-Ypartial-unification",
	"-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.github.mpilquist" %% "simulacrum" % "0.13.0",
	"org.scalaz" %% "scalaz-core" % "7.2.26"
)

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies += {
	val version = scalaBinaryVersion.value match {
		case "2.10" => "1.0.3"
		case _ ⇒ "1.6.2"
	}
	"com.lihaoyi" % "ammonite" % version % "test" cross CrossVersion.full
}

sourceGenerators in Test += Def.task {
	val file = (sourceManaged in Test).value / "amm.scala"
	IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
	Seq(file)
}.taskValue

// Optional, required for the `source` command to work
(fullClasspath in Test) ++= {
	(updateClassifiers in Test).value
		.configurations
		.find(_.configuration == Test.name)
		.get
		.modules
		.flatMap(_.artifacts)
		.collect{case (a, f) if a.classifier == Some("sources") => f}
}
