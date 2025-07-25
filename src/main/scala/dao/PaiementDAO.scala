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
  def creerPaiement(p: Paiement, reservationId: Int, payeurId: Int, beneficiaireId: Int)(implicit conn: Connection): Boolean = {
    val sql =
      """
        |INSERT INTO paiements (numero_transaction, montant, statut, date_paiement)
        |VALUES (?, ?, ?, ?)
        |""".stripMargin
    try {
      val stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
      stmt.setString(1, p.numeroTransaction)
      stmt.setBigDecimal(2, p.montant.bigDecimal)
      stmt.setString(3, p.statut)
      val ts: Timestamp = p.datePaiement match {
        case Some(date) => Timestamp.valueOf(date.atStartOfDay())
        case None       => null
      }
      stmt.setTimestamp(4, ts)
      val rows = stmt.executeUpdate()
      if (rows > 0) {
        val keys = stmt.getGeneratedKeys
        if (keys.next()) {
          val paiementId = keys.getInt(1)
          // Lier le paiement à la réservation
          val stmtLink = conn.prepareStatement("INSERT INTO reservation_paiement (reservation_id, paiement_id, payeur_id, beneficiaire_id) VALUES (?, ?, ?, ?)")
          stmtLink.setInt(1, reservationId)
          stmtLink.setInt(2, paiementId)
          stmtLink.setInt(3, payeurId)
          stmtLink.setInt(4, beneficiaireId)
          stmtLink.executeUpdate() > 0
        } else false
      } else false
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  // Récupérer les paiements d'un utilisateur (par jointure sur reservation_paiement)
  def getPaiementsPourUtilisateur(utilisateurId: Int)(implicit conn: Connection): List[Paiement] = {
    try {
      val sql = """
        SELECT p.* FROM paiements p
        JOIN reservation_paiement rp ON p.id = rp.paiement_id
        JOIN reservations r ON rp.reservation_id = r.id
        WHERE rp.payeur_id = ? OR rp.beneficiaire_id = ?
        ORDER BY p.date_paiement DESC
      """
      val stmt = conn.prepareStatement(sql)
      stmt.setInt(1, utilisateurId)
      stmt.setInt(2, utilisateurId)
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
