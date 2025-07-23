import java.sql.{Connection, DriverManager, ResultSet, Statement}
object DB {
  
  var connection: Connection = null
  val url = "jdbc:postgresql://ep-blue-dust-a2s88sx9-pooler.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
  val username = "neondb_owner"
  val password = "npg_ybcu3mp7KvCR"
  try {
    Class.forName("org.postgresql.Driver")
    connection = DriverManager.getConnection(url, username, password)
  } catch {
    case e: Exception => e.printStackTrace()

  }
}

