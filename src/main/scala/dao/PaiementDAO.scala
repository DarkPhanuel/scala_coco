package dao

import java.sql.{Connection, Timestamp}
import scala.collection.mutable.ListBuffer
import models.Paiement
import java.time.LocalDate

object PaiementDAO {
  def create(p: Paiement)(implicit conn: Connection): Boolean = {
    val sql =
      """
        |INSERT INTO paiements (numero_transaction, montant, statut, date_paiement)
        |VALUES (?, ?, ?, ?)
        |""".stripMargin
    try {
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, p.numeroTransaction)
      stmt.setBigDecimal(2, p.montant.bigDecimal)
      stmt.setString(3, p.statut)
      val ts: Timestamp = p.datePaiement match {
        case Some(date) => Timestamp.valueOf(date.atStartOfDay())
        case None       => null
      }
      stmt.setTimestamp(4, ts)
      stmt.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  def getAll()(implicit conn: Connection): List[Paiement] = {
    try {
      val stmt = conn.prepareStatement("SELECT * FROM paiements")
      val rs = stmt.executeQuery()
      val result = ListBuffer[Paiement]()
      while (rs.next()) {
        val dateOpt: Option[LocalDate] = Option(rs.getTimestamp("date_paiement")).map(_.toLocalDateTime.toLocalDate)
        result += Paiement(
          id = rs.getInt("id"),
          numeroTransaction = rs.getString("numero_transaction"),
          montant = rs.getBigDecimal("montant"),
          statut = rs.getString("statut"),
          datePaiement = dateOpt
        )
      }
      result.toList
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  def findByNumero(numero: String)(implicit conn: Connection): Option[Paiement] = {
    try {
      val stmt = conn.prepareStatement("SELECT * FROM paiements WHERE numero_transaction = ?")
      stmt.setString(1, numero)
      val rs = stmt.executeQuery()
      if (rs.next()) {
        val dateOpt = Option(rs.getTimestamp("date_paiement")).map(_.toLocalDateTime.toLocalDate)
        Some(Paiement(
          id = rs.getInt("id"),
          numeroTransaction = rs.getString("numero_transaction"),
          montant = rs.getBigDecimal("montant"),
          statut = rs.getString("statut"),
          datePaiement = dateOpt
        ))
      } else None
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  // Créer un paiement
  def creerPaiement(p: Paiement)(implicit conn: Connection): Boolean = create(p)

  // Récupérer les paiements d'un utilisateur
  def getPaiementsPourUtilisateur(utilisateurId: Int)(implicit conn: Connection): List[Paiement] = {
    try {
      val stmt = conn.prepareStatement("SELECT * FROM paiements WHERE reservation_id IN (SELECT id FROM reservations WHERE passager_id = ?)")
      stmt.setInt(1, utilisateurId)
      val rs = stmt.executeQuery()
      val result = ListBuffer[Paiement]()
      while (rs.next()) {
        val dateOpt: Option[LocalDate] = Option(rs.getTimestamp("date_paiement")).map(_.toLocalDateTime.toLocalDate)
        result += Paiement(
          id = rs.getInt("id"),
          numeroTransaction = rs.getString("numero_transaction"),
          montant = rs.getBigDecimal("montant"),
          statut = rs.getString("statut"),
          datePaiement = dateOpt
        )
      }
      result.toList
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }
}
