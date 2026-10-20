import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest._
import slick.cats.Database
import slick.jdbc.{DatabaseConfig, H2Profile}
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta._

class TablesSuite extends funsuite.AnyFunSuite {

  val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]

  // Each test gets a fresh in-memory database, which is dropped when the Resource is released
  def withDb[T](f: Database => IO[T]): T =
    Database.resource(DatabaseConfig.forProfileConfig(H2Profile, "h2mem1")).use(f).unsafeRunSync()

  def createSchema(db: Database): IO[Unit] =
    db.run((suppliers.schema ++ coffees.schema).create)

  def insertSupplier(db: Database): IO[Int] =
    db.run(suppliers += (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"))

  test("Creating the Schema works") {
    val tables = withDb(db => createSchema(db) >> db.run(MTable.getTables))

    assert(tables.size == 2)
    assert(tables.count(_.name.name.equalsIgnoreCase("suppliers")) == 1)
    assert(tables.count(_.name.name.equalsIgnoreCase("coffees")) == 1)
  }

  test("Inserting a Supplier works") {
    val insertCount = withDb(db => createSchema(db) >> insertSupplier(db))
    assert(insertCount == 1)
  }

  test("Query Suppliers works") {
    val results = withDb(db => createSchema(db) >> insertSupplier(db) >> db.run(suppliers.result))
    assert(results.size == 1)
    assert(results.head._1 == 101)
  }
}
