import cats.effect.{IO, IOApp}
import slick.cats.Database
import slick.jdbc.{DatabaseConfig, H2Profile}
import slick.jdbc.H2Profile.api._

// Demonstrates various ways of reading data
object QueryActions extends IOApp.Simple {

  // A simple dictionary table with keys and values
  class Dict(tag: Tag) extends Table[(Int, String)](tag, "INT_DICT") {
    def key = column[Int]("KEY", O.PrimaryKey)
    def value = column[String]("VALUE")
    def * = (key, value)
  }
  val dict = TableQuery[Dict]

  def run: IO[Unit] = Database.resource(DatabaseConfig.forProfileConfig(H2Profile, "h2mem1")).use { db =>

    //#upTo
    // Define a pre-compiled parameterized query for reading all key/value
    // pairs up to a given key.
    val upTo = Compiled { (k: Rep[Int]) =>
      dict.filter(_.key <= k).sortBy(_.key)
    }
    //#upTo

    // A second pre-compiled query which returns a Set[String]
    val upToSet = upTo.map(_.andThen(_.to[Set]))

    db.run(DBIO.seq(

      // Create the dictionary table and insert some data
      dict.schema.create,
      dict ++= Seq(1 -> "a", 2 -> "b", 3 -> "c", 4 -> "d", 5 -> "e"),

      upTo(3).result.map { r =>
        println("Seq (Vector) of k/v pairs up to 3")
        println("- " + r)
      },

      upToSet(3).result.map { r =>
        println("Set of k/v pairs up to 3")
        println("- " + r)
      },

      dict.map(_.key).to[Array].result.map { r =>
        println("All keys in an unboxed Array[Int]")
        println("- " + r)
      },

      upTo(3).result.head.map { r =>
        println("Only get the first result, failing if there is none")
        println("- " + r)
      },

      upTo(3).result.headOption.map { r =>
        println("Get the first result as an Option, or None")
        println("- " + r)
      }

    )).flatMap { _ =>
      // The stream captures a Database plus a DBIO action.
      // The action does not run until you consume the stream.
      val s = db.stream(upTo(3).result)

      IO.println("Stream k/v pairs up to 3 via FS2") >>
        s.evalMap(v => IO.println("- " + v)).compile.drain
    }
  }
}
