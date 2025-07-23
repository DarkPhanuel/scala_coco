package dao

import DB.DB
import models.Reservation
import java.sql.{PreparedStatement, ResultSet, Timestamp, Date}
import scala.collection.mutable.ListBuffer
import java.math.BigDecimal

object ReservationDAO {

  def findAll(): Seq[Reservation] = {
    val query = "SELECT * FROM reservations"
    val statement = DB.connection.prepareStatement(query)
    val resultSet = statement.executeQuery()
    var reservations: Seq[Reservation] = Seq.empty

    while (resultSet.next()) {
      val reservation = fromResultSet(resultSet)
      reservations :+= reservation
    }

    reservations
  }

  def findById(id: Int): Option[Reservation] = {
    val query = "SELECT * FROM reservations WHERE id = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setInt(1, id)
    val resultSet = statement.executeQuery()

    if (resultSet.next()) Some(fromResultSet(resultSet)) else None
  }

  def findByUtilisateurId(utilisateurId: Int): Seq[Reservation] = {
    val query = """
      |SELECT r.* FROM reservations r
      |JOIN trajet_reservation tr ON r.id = tr.reservation_id
      |WHERE tr.passager_id = ?""".stripMargin
    val statement = DB.connection.prepareStatement(query)
    statement.setInt(1, utilisateurId)
    val resultSet = statement.executeQuery()

    var reservations: Seq[Reservation] = Seq.empty
    while (resultSet.next()) {
      val reservation = fromResultSet(resultSet)
      reservations :+= reservation
    }

    reservations
  }

  def findTrajetsReserves(utilisateurId: Int): Seq[Int] = {
    val query = "SELECT trajet_id FROM trajet_reservation WHERE passager_id = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setInt(1, utilisateurId)
    val resultSet = statement.executeQuery()

    var trajets: Seq[Int] = Seq.empty
    while (resultSet.next()) {
      trajets :+= resultSet.getInt("trajet_id")
    }

    trajets
  }

  def annulerReservation(id: Int, motif: String): Boolean = {
    val query = "UPDATE reservations SET statut = 'annulee', date_annulation = CURRENT_TIMESTAMP, motif_annulation = ? WHERE id = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, motif)
    statement.setInt(2, id)

    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def create(res: Reservation): Boolean = {
    val query = """
      |INSERT INTO reservations (
      |  numero_reservation, nombre_places, prix_total, statut, message_passager,
      |  date_reservation, date_confirmation, date_annulation, motif_annulation)
      |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin

    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, res.numeroReservation)
    statement.setInt(2, res.nombrePlaces)
    statement.setBigDecimal(3, res.prixTotal.bigDecimal)
    statement.setString(4, res.statut)
    statement.setString(5, res.messagePassager.orNull)
    statement.setTimestamp(6, res.dateReservation)
    statement.setTimestamp(7, res.dateConfirmation.orNull)
    statement.setTimestamp(8, res.dateAnnulation.orNull)
    statement.setString(9, res.motifAnnulation.orNull)

    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def update(res: Reservation): Boolean = {
    val query = """
      |UPDATE reservations SET
      |  nombre_places = ?, prix_total = ?, statut = ?, message_passager = ?,
      |  date_reservation = ?, date_confirmation = ?, date_annulation = ?, motif_annulation = ?
      |WHERE id = ?""".stripMargin

    val statement = DB.connection.prepareStatement(query)
    statement.setInt(1, res.nombrePlaces)
    statement.setBigDecimal(2, res.prixTotal.bigDecimal)
    statement.setString(3, res.statut)
    statement.setString(4, res.messagePassager.orNull)
    statement.setTimestamp(5, res.dateReservation)
    statement.setTimestamp(6, res.dateConfirmation.orNull)
    statement.setTimestamp(7, res.dateAnnulation.orNull)
    statement.setString(8, res.motifAnnulation.orNull)
    statement.setInt(9, res.id)

    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def delete(id: Int): Boolean = {
    val query = "DELETE FROM reservations WHERE id = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setInt(1, id)

    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  private def fromResultSet(rs: ResultSet): Reservation = {
    Reservation(
      id = rs.getInt("id"),
      numeroReservation = rs.getString("numero_reservation"),
      nombrePlaces = rs.getInt("nombre_places"),
      prixTotal = rs.getBigDecimal("prix_total"),
      statut = rs.getString("statut"),
      messagePassager = Option(rs.getString("message_passager")),
      dateReservation = rs.getTimestamp("date_reservation"),
      dateConfirmation = Option(rs.getTimestamp("date_confirmation")),
      dateAnnulation = Option(rs.getTimestamp("date_annulation")),
      motifAnnulation = Option(rs.getString("motif_annulation"))
    )
  }
}
