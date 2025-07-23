package dao

import DB.DB
import java.sql._
import java.time.LocalDate

object TrajetDAO {
  def proposerUnTrajet(trajet: models.Trajet): Int = {
    val sql = """
      INSERT INTO trajets (
        ville_depart, ville_arrivee, prix_par_place, places_totales, statut
      ) VALUES (?, ?, ?, ?, ?)"""
    try {
      val stmt = DB.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      stmt.setString(1, trajet.ville_depart)
      stmt.setString(2, trajet.ville_arrivee)
      stmt.setInt(3, trajet.prix_par_place)
      stmt.setInt(4, trajet.places_totales)
      stmt.setString(5, trajet.statut)
      stmt.executeUpdate()
      val rs = stmt.getGeneratedKeys
      if (rs.next()) {
        val trajetId = rs.getInt(1)
        val linkSql = "INSERT INTO utilisateur_trajet (utilisateur_id, trajet_id, vehicule_id, role) VALUES (?, ?, ?, 'conducteur')"
        val stmtLink = DB.connection.prepareStatement(linkSql)
        stmtLink.setInt(1, trajet.conducteur.id)
        stmtLink.setInt(2, trajetId)
        stmtLink.setInt(3, trajet.vehicule.id)
        stmtLink.executeUpdate()
        trajetId
      } else 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        0
    }
  }
} 
