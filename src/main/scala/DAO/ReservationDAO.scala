package dao
import java.sql.{Connection, ResultSet, Statement}
import scala.collection.mutable.ListBuffer
import scala.math.BigDecimal.javaBigDecimal2bigDecimal
import models.Reservation

object ReservationDAO {
  private val connection: Connection = DB.DB.connection;

  def create(res: Reservation): Int = {
    val sql =
      """INSERT INTO reservations (numero_reservation, nombre_places, prix_total, statut, message_passager,
        | date_reservation, date_confirmation, date_annulation, motif_annulation)
        | VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
    try {
      val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      stmt.setString(1, res.numeroReservation)
      stmt.setInt(2, res.nombrePlaces)
      stmt.setBigDecimal(3, res.prixTotal.bigDecimal)
      stmt.setString(4, res.statut)
      stmt.setString(5, res.messagePassager.orNull)
      stmt.setTimestamp(6, res.dateReservation)
      stmt.setTimestamp(7, res.dateConfirmation.orNull)
      stmt.setTimestamp(8, res.dateAnnulation.orNull)
      stmt.setString(9, res.motifAnnulation.orNull)
      stmt.executeUpdate()
      val keys = stmt.getGeneratedKeys
      if (keys.next()) keys.getInt(1) else -1
    } catch {
      case e: Exception =>
        e.printStackTrace()
        -1
    }
  }

  def getById(id: Int): Option[Reservation] = {
    val sql = "SELECT * FROM reservations WHERE id = ?"
    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, id)
      val rs = stmt.executeQuery()
      if (rs.next()) Some(fromResultSet(rs)) else None
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  def getAll(): List[Reservation] = {
    val sql = "SELECT * FROM reservations"
    try {
      val stmt = connection.createStatement()
      val rs = stmt.executeQuery(sql)
      val reservations = ListBuffer[Reservation]()
      while (rs.next()) {
        reservations += fromResultSet(rs)
      }
      reservations.toList
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  def updateStatut(id: Int, statut: String): Boolean = {
    val sql = "UPDATE reservations SET statut = ? WHERE id = ?"
    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setString(1, statut)
      stmt.setInt(2, id)
      stmt.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def delete(id: Int): Boolean = {
    val sql = "DELETE FROM reservations WHERE id = ?"
    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, id)
      stmt.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  // Récupérer les réservations d'un utilisateur (par jointure)
  def getReservationsPourUtilisateur(utilisateurId: Int): List[Reservation] = {
    val sql = """
      SELECT r.* FROM reservations r
      JOIN trajet_reservation tr ON r.id = tr.reservation_id
      WHERE tr.passager_id = ?
      ORDER BY r.date_reservation DESC
    """
    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, utilisateurId)
      val rs = stmt.executeQuery()
      val reservations = ListBuffer[Reservation]()
      while (rs.next()) {
        reservations += fromResultSetWithPassager(rs)
      }
      reservations.toList
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  // Annuler une réservation (update statut)
  def annulerReservation(reservationId: Int): Boolean = {
    val sql = "UPDATE reservations SET statut = 'annulee' WHERE id = ?"
    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, reservationId)
      stmt.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  // Créer une réservation et la lier à un passager via trajet_reservation
  def creerReservation(res: Reservation, trajetId: Int, passagerId: Int, conducteurId: Int): Int = {
    val sql =
      """INSERT INTO reservations (numero_reservation, nombre_places, prix_total, statut, message_passager, date_reservation)
         |VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
    try {
      val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      stmt.setString(1, res.numeroReservation)
      stmt.setInt(2, res.nombrePlaces)
      stmt.setBigDecimal(3, res.prixTotal.bigDecimal)
      stmt.setString(4, res.statut)
      stmt.setString(5, res.messagePassager.orNull)
      stmt.setTimestamp(6, res.dateReservation)
      stmt.executeUpdate()
      val keys = stmt.getGeneratedKeys
      if (keys.next()) {
        val reservationId = keys.getInt(1)
        // Lier la réservation au passager et au trajet
        val stmtLink = connection.prepareStatement("INSERT INTO trajet_reservation (trajet_id, reservation_id, passager_id, conducteur_id) VALUES (?, ?, ?, ?)")
        stmtLink.setInt(1, trajetId)
        stmtLink.setInt(2, reservationId)
        stmtLink.setInt(3, passagerId)
        stmtLink.setInt(4, conducteurId)
        stmtLink.executeUpdate()
        reservationId
      } else -1
    } catch {
      case e: Exception =>
        e.printStackTrace()
        -1
    }
  }

  private def fromResultSet(rs: ResultSet): Reservation = {
    Reservation(
      rs.getInt("id"),
      rs.getString("numero_reservation"),
      rs.getInt("nombre_places"),
      rs.getBigDecimal("prix_total"),
      rs.getString("statut"),
      Option(rs.getString("message_passager")),
      rs.getTimestamp("date_reservation"),
      Option(rs.getTimestamp("date_confirmation")),
      Option(rs.getTimestamp("date_annulation")),
      Option(rs.getString("motif_annulation"))
    )
  }

  // Récupère aussi le passager (Utilisateur minimal)
  private def fromResultSetWithPassager(rs: ResultSet): Reservation = {
    val passager = models.Utilisateur(
      id = rs.getInt("id"),
      email = "",
      mot_de_passe = "",
      nom = "",
      prenom = "",
      telephone = "",
      est_conducteur = false,
      ville = "",
      code_postal = "",
      note_moyenne = 0.0,
      nombre_evaluations = 0,
      statut = "",
      vehicules = Seq(),
      messagesEnvoyes = Seq(),
      messagesRecus = Seq(),
      trajetsConducteur = Seq(),
      reservationsPassager = Seq(),
      evaluationsDonnees = Seq(),
      evaluationsRecues = Seq()
    )
    Reservation(
      rs.getInt("id"),
      rs.getString("numero_reservation"),
      rs.getInt("nombre_places"),
      rs.getBigDecimal("prix_total"),
      rs.getString("statut"),
      Option(rs.getString("message_passager")),
      rs.getTimestamp("date_reservation"),
      Option(rs.getTimestamp("date_confirmation")),
      Option(rs.getTimestamp("date_annulation")),
      Option(rs.getString("motif_annulation")),
      passager = passager
    )
  }
}

