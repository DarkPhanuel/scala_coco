package dao

import DB.DB
import java.sql._
import java.time.{LocalDate, LocalDateTime}

object TrajetDAO {
  
  

 def proposerUnTrajet(trajet: models.Trajet, conducteurId: Int, vehiculeId: Int): Int = {
    val sql = """
      INSERT INTO trajets (
        ville_depart, ville_arrivee,
        date_depart, prix_par_place, places_disponibles, places_totales,
         statut
      ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
    """
    val stmt = DB.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
    stmt.setString(1, trajet.ville_depart)
    stmt.setString(2, trajet.ville_arrivee)
    stmt.setDate(3, Date.valueOf(trajet.date_depart))
    stmt.setInt(4, trajet.prix_par_place)
    stmt.setInt(5, trajet.places_disponibles)
    stmt.setInt(6, trajet.places_totales)
    stmt.setString(7, trajet.statut)
    
    stmt.executeUpdate()
    
    val rs = stmt.getGeneratedKeys
    if (rs.next()) {
      val trajetId = rs.getInt(1)
      // Enregistrement de la liaison entre utilisateur et trajet
      val linkSql = """
        INSERT INTO utilisateur_trajet (utilisateur_id, trajet_id, vehicule_id, role)
        VALUES (?, ?, ?, 'conducteur')
      """
      val stmtLink = DB.connection.prepareStatement(linkSql)
      stmtLink.setInt(1, conducteurId)
      stmtLink.setInt(2, trajetId)
      stmtLink.setInt(3, vehiculeId)
      stmtLink.executeUpdate()
      trajetId
    } else 0
  }


 /* // Insère un trajet en base et le lie au conducteur et au véhicule
  def insert(trajet: models.Trajet, conducteurId: Int, vehiculeId: Int): Int = {
    val sql = """
      INSERT INTO trajets (
        code_trajet, ville_depart, adresse_depart, ville_arrivee, adresse_arrivee,
        date_depart, heure_depart, prix_par_place, places_disponibles, places_totales,
        distance_km, duree_estimee, description, statut
      ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
    """
    val stmt = DB.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
    stmt.setString(1, trajet.code_trajet)
    stmt.setString(2, trajet.ville_depart)
    stmt.setString(3, trajet.adresse_depart)
    stmt.setString(4, trajet.ville_arrivee)
    stmt.setString(5, trajet.adresse_arrivee)
    stmt.setDate(6, Date.valueOf(trajet.date_depart))
    stmt.setTimestamp(7, Timestamp.valueOf(trajet.heure_depart))
    stmt.setInt(8, trajet.prix_par_place)
    stmt.setInt(9, trajet.places_disponibles)
    stmt.setInt(10, trajet.places_totales)
    stmt.setInt(11, trajet.distance_km)
    stmt.setTimestamp(12, Timestamp.valueOf(trajet.duree_estimee))
    stmt.setString(13, trajet.description)
    stmt.setString(14, trajet.statut)
    stmt.executeUpdate()
    val rs = stmt.getGeneratedKeys
    if (rs.next()) {
      val trajetId = rs.getInt(1)
      // Enregistrement de la liaison entre utilisateur, trajet et véhicule
      val linkSql = """
        INSERT INTO utilisateur_trajet (utilisateur_id, trajet_id, vehicule_id, role)
        VALUES (?, ?, ?, 'conducteur')
      """
      val stmtLink = DB.connection.prepareStatement(linkSql)
      stmtLink.setInt(1, conducteurId)
      stmtLink.setInt(2, trajetId)
      stmtLink.setInt(3, vehiculeId)
      stmtLink.executeUpdate()
      trajetId
    } else 0
  }

  // Récupère tous les trajets
  def findAll(): List[models.Trajet] = {
    val sql = "SELECT * FROM trajets"
    val stmt = DB.connection.prepareStatement(sql)
    val rs = stmt.executeQuery()
    val trajets = scala.collection.mutable.ListBuffer[models.Trajet]()
    while (rs.next()) trajets += mapRowToTrajet(rs)
    trajets.toList
  }

  // Recherche des trajets par ville de départ, d'arrivée et date
  def searchByVilleAndDate(villeDepart: String, villeArrivee: String, date: LocalDate): List[models.Trajet] = {
    val sql = """
      SELECT * FROM trajets
      WHERE ville_depart = ? AND ville_arrivee = ? AND date_depart = ?
    """
    val stmt = DB.connection.prepareStatement(sql)
    stmt.setString(1, villeDepart)
    stmt.setString(2, villeArrivee)
    stmt.setDate(3, Date.valueOf(date))
    val rs = stmt.executeQuery()
    val trajets = scala.collection.mutable.ListBuffer[models.Trajet]()
    while (rs.next()) trajets += mapRowToTrajet(rs)
    trajets.toList
  }

  // Recherche un trajet par son identifiant
  def findById(id: Int): Option[models.Trajet] = {
    val sql = "SELECT * FROM trajets WHERE id = ?"
    val stmt = DB.connection.prepareStatement(sql)
    stmt.setInt(1, id)
    val rs = stmt.executeQuery()
    if (rs.next()) Some(mapRowToTrajet(rs)) else None
  }

  // Supprime un trajet uniquement s’il n’a aucune réservation associée
  def deleteIfNoReservation(trajetId: Int): Boolean = {
    val sqlCheck = "SELECT COUNT(*) FROM trajet_reservation WHERE trajet_id = ?"
    val stmtCheck = DB.connection.prepareStatement(sqlCheck)
    stmtCheck.setInt(1, trajetId)
    val rs = stmtCheck.executeQuery()
    rs.next()
    val count = rs.getInt(1)
    if (count == 0) {
      val sqlDelete = "DELETE FROM trajets WHERE id = ?"
      val stmtDelete = DB.connection.prepareStatement(sqlDelete)
      stmtDelete.setInt(1, trajetId)
      stmtDelete.executeUpdate() > 0
    } else false
  }

  // Récupère les trajets à venir d’un utilisateur (conducteur)
  def trajetsAVenir(utilisateurId: Int): List[models.Trajet] = {
    val sql = """
      SELECT t.* FROM trajets t
      JOIN utilisateur_trajet ut ON ut.trajet_id = t.id
      WHERE ut.utilisateur_id = ? AND t.date_depart >= CURRENT_DATE
      ORDER BY t.date_depart ASC
    """
    val stmt = DB.connection.prepareStatement(sql)
    stmt.setInt(1, utilisateurId)
    val rs = stmt.executeQuery()
    val trajets = scala.collection.mutable.ListBuffer[models.Trajet]()
    while (rs.next()) trajets += mapRowToTrajet(rs)
    trajets.toList
  }

  // Récupère les trajets passés d’un utilisateur
  def trajetsPasses(utilisateurId: Int): List[models.Trajet] = {
    val sql = """
      SELECT t.* FROM trajets t
      JOIN utilisateur_trajet ut ON ut.trajet_id = t.id
      WHERE ut.utilisateur_id = ? AND t.date_depart < CURRENT_DATE
      ORDER BY t.date_depart DESC
    """
    val stmt = DB.connection.prepareStatement(sql)
    stmt.setInt(1, utilisateurId)
    val rs = stmt.executeQuery()
    val trajets = scala.collection.mutable.ListBuffer[models.Trajet]()
    while (rs.next()) trajets += mapRowToTrajet(rs)
    trajets.toList
  }

  // Récupère le conducteur associé à un trajet
  def conducteur(trajetId: Int): Option[models.Utilisateur] = {
    val sql = """
      SELECT u.* FROM utilisateurs u
      JOIN utilisateur_trajet ut ON u.id = ut.utilisateur_id
      WHERE ut.trajet_id = ? AND ut.role = 'conducteur'
    """
    val stmt = DB.connection.prepareStatement(sql)
    stmt.setInt(1, trajetId)
    val rs = stmt.executeQuery()
    if (rs.next()) Some(UtilisateurDAO.mapRowToUtilisateur(rs)) else None
  }

  // Fonction utilitaire pour convertir un ResultSet en objet Trajet complet
  private def mapRowToTrajet(rs: ResultSet): models.Trajet = {
    val id = rs.getInt("id")
    val conducteur = conducteur(id)
    val vehicule = VehiculeDAO.findByTrajetId(id)
    models.Trajet(
      id = id,
      code_trajet = rs.getString("code_trajet"),
      ville_depart = rs.getString("ville_depart"),
      adresse_depart = rs.getString("adresse_depart"),
      ville_arrivee = rs.getString("ville_arrivee"),
      adresse_arrivee = rs.getString("adresse_arrivee"),
      date_depart = rs.getDate("date_depart").toLocalDate,
      heure_depart = rs.getTimestamp("heure_depart").toLocalDateTime,
      prix_par_place = rs.getInt("prix_par_place"),
      places_disponibles = rs.getInt("places_disponibles"),
      places_totales = rs.getInt("places_totales"),
      distance_km = rs.getInt("distance_km"),
      duree_estimee = rs.getTimestamp("duree_estimee").toLocalDateTime,
      description = rs.getString("description"),
      statut = rs.getString("statut"),
      conducteur = conducteur,
      vehicule = vehicule
    )
  }*/
} 
