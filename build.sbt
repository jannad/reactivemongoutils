name := "reactivemongoutils"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies ++= Seq(
	"org.reactivemongo" %% "reactivemongo" % "0.11.10" withSources,
	"org.typelevel" %% "cats" % "0.4.1" withSources,
	"org.scalatest" %% "scalatest" % "3.0.0" % "test"
)