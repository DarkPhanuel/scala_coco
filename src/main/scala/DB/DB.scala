package DB

import java.sql.{Connection, DriverManager}

object DB {
  var connection: Connection = null
  private val url = "jdbc:postgresql://ep-proud-sky-a2no67ob-pooler.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require"
  private val username = "neondb_owner"
  private val password = "npg_9m2twcHLeuOA"
  try {
    Class.forName("org.postgresql.Driver")
    connection = DriverManager.getConnection(url, username, password)
  } catch {
    case e: Exception => e.printStackTrace()

  }
}

