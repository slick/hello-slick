import cats.effect.{IO, IOApp}
import slick.cats.Database
import slick.jdbc.{DatabaseConfig, H2Profile}
import slick.jdbc.H2Profile.api._

object CaseClassMapping extends IOApp.Simple {

  // the base query for the Users table
  val users = TableQuery[Users]

  def run: IO[Unit] = Database.resource(DatabaseConfig.forProfileConfig(H2Profile, "h2mem1")).use { db =>
    db.run(DBIO.seq(
      // create the schema
      users.schema.create,

      // insert two User instances
      users += User("John Doe"),
      users += User("Fred Smith"),

      // print the users (select * from USERS)
      users.result.map(println(_))
    ))
  }
}

case class User(name: String, id: Option[Int] = None)

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  // Auto Increment the id primary key column
  //#autoInc
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  //#autoInc
  // The name can't be null
  def name = column[String]("NAME")
  // the * projection (e.g. select * ...) auto-transforms the tupled
  // column values to / from a User
  //#mapTo
  def * = (name, id.?).mapTo[User]
  //#mapTo
}
