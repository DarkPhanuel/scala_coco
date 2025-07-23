package dao

import DB.DB
import java.sql._
import java.time.LocalDate

object TrajetDAO {
  def proposerUnTrajet(trajet: models.Trajet): Int = {
    val sql = """
      INSERT INTO trajets (
        ville_depart, ville_arrivee, date_depart, heure_depart, prix_par_place, places_totales, statut
      ) VALUES (?, ?, ?, ?, ?, ?, ?)"""
    try {
      val stmt = DB.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      stmt.setString(1, trajet.ville_depart)
      stmt.setString(2, trajet.ville_arrivee)
      stmt.setDate(3, java.sql.Date.valueOf(trajet.date_depart))
      stmt.setTime(4, java.sql.Time.valueOf(trajet.heure_depart))
      stmt.setInt(5, trajet.prix_par_place)
      stmt.setInt(6, trajet.places_totales)
      stmt.setString(7, trajet.statut)
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
  
  
  def getSharedTrajets(): List[models.Trajet] = {
    val sql = "SELECT * FROM trajets WHERE statut = 'partage'"
    try {
      val stmt = DB.connection.prepareStatement(sql)
      val rs = stmt.executeQuery()
      var trajets: List[models.Trajet] = List()
      while (rs.next()) {
        trajets ::= models.Trajet(
          id = rs.getInt("id"),
          ville_depart = rs.getString("ville_depart"),
          ville_arrivee = rs.getString("ville_arrivee"),
          date_depart = rs.getDate("date_depart").toLocalDate,
          heure_depart = rs.getTime("heure_depart").toLocalTime,
          prix_par_place = rs.getInt("prix_par_place"),
          places_totales = rs.getInt("places_totales"),
          statut = rs.getString("statut"),
          conducteur = null, // à compléter si besoin
          vehicule = null // à compléter si besoin
        )
      }
      trajets.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }


  // Récupérer les trajets d'un utilisateur (conducteur ou passager) avec conducteur renseigné
  def getTrajetsPourUtilisateur(utilisateurId: Int, aVenir: Boolean): List[models.Trajet] = {
    val now = java.sql.Date.valueOf(java.time.LocalDate.now())
    val sql =
      if (aVenir)
        """
        SELECT t.*, u.id as conducteur_id, u.nom as conducteur_nom, u.email as conducteur_email
        FROM trajets t
        JOIN utilisateur_trajet ut ON t.id = ut.trajet_id
        JOIN utilisateur_trajet utc ON t.id = utc.trajet_id AND utc.role = 'conducteur'
        JOIN utilisateurs u ON utc.utilisateur_id = u.id
        WHERE ut.utilisateur_id = ? AND t.date_depart >= ?
        ORDER BY t.date_depart ASC
        """
      else
        """
        SELECT t.*, u.id as conducteur_id, u.nom as conducteur_nom, u.email as conducteur_email
        FROM trajets t
        JOIN utilisateur_trajet ut ON t.id = ut.trajet_id
        JOIN utilisateur_trajet utc ON t.id = utc.trajet_id AND utc.role = 'conducteur'
        JOIN utilisateurs u ON utc.utilisateur_id = u.id
        WHERE ut.utilisateur_id = ? AND t.date_depart < ?
        ORDER BY t.date_depart DESC
        """
    try {
      val stmt = DB.connection.prepareStatement(sql)
      stmt.setInt(1, utilisateurId)
      stmt.setDate(2, now)
      val rs = stmt.executeQuery()
      var trajets: List[models.Trajet] = List()
      while (rs.next()) {
        trajets ::= models.Trajet(
          id = rs.getInt("id"),
          ville_depart = rs.getString("ville_depart"),
          ville_arrivee = rs.getString("ville_arrivee"),
          date_depart = rs.getDate("date_depart").toLocalDate,
          heure_depart = rs.getTime("heure_depart").toLocalTime,
          prix_par_place = rs.getInt("prix_par_place"),
          places_totales = rs.getInt("places_totales"),
          statut = rs.getString("statut"),
          conducteur = models.Utilisateur(
            id = rs.getInt("conducteur_id"),
            email = rs.getString("conducteur_email"),
            mot_de_passe = "",
            nom = rs.getString("conducteur_nom"),
            prenom = "",
            telephone = "",
            est_conducteur = true,
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
          ),
          vehicule = null,
          passagers = List.empty
        )
      }
      trajets.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  // Supprimer un trajet par son id
  def supprimerTrajet(trajetId: Int): Boolean = {
    val sql = "DELETE FROM trajets WHERE id = ?"
    try {
      val stmt = DB.connection.prepareStatement(sql)
      stmt.setInt(1, trajetId)
      stmt.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  // Rechercher des trajets par ville et date avec conducteur renseigné
  def rechercherTrajets(villeDepart: String, villeArrivee: String, date: java.time.LocalDate): List[models.Trajet] = {
    val sql = """
      SELECT t.*, u.id as conducteur_id, u.nom as conducteur_nom, u.email as conducteur_email
      FROM trajets t
      JOIN utilisateur_trajet ut ON t.id = ut.trajet_id AND ut.role = 'conducteur'
      JOIN utilisateurs u ON ut.utilisateur_id = u.id
      WHERE t.ville_depart = ? AND t.ville_arrivee = ? AND t.date_depart = ?
    """
    try {
      val stmt = DB.connection.prepareStatement(sql)
      stmt.setString(1, villeDepart)
      stmt.setString(2, villeArrivee)
      stmt.setDate(3, java.sql.Date.valueOf(date))
      val rs = stmt.executeQuery()
      var trajets: List[models.Trajet] = List()
      while (rs.next()) {
        trajets ::= models.Trajet(
          id = rs.getInt("id"),
          ville_depart = rs.getString("ville_depart"),
          ville_arrivee = rs.getString("ville_arrivee"),
          date_depart = rs.getDate("date_depart").toLocalDate,
          heure_depart = rs.getTime("heure_depart").toLocalTime,
          prix_par_place = rs.getInt("prix_par_place"),
          places_totales = rs.getInt("places_totales"),
          statut = rs.getString("statut"),
          conducteur = models.Utilisateur(
            id = rs.getInt("conducteur_id"),
            email = rs.getString("conducteur_email"),
            mot_de_passe = "",
            nom = rs.getString("conducteur_nom"),
            prenom = "",
            telephone = "",
            est_conducteur = true,
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
          ),
          vehicule = null,
          passagers = List.empty
        )
      }
      trajets.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  // Lister tous les trajets avec conducteur renseigné
  def getAllTrajets(): List[models.Trajet] = {
    val sql = """
      SELECT t.*, u.id as conducteur_id, u.nom as conducteur_nom, u.email as conducteur_email
      FROM trajets t
      JOIN utilisateur_trajet ut ON t.id = ut.trajet_id AND ut.role = 'conducteur'
      JOIN utilisateurs u ON ut.utilisateur_id = u.id
      ORDER BY t.date_depart DESC, t.heure_depart DESC
    """
    try {
      val stmt = DB.connection.prepareStatement(sql)
      val rs = stmt.executeQuery()
      var trajets: List[models.Trajet] = List()
      while (rs.next()) {
        trajets ::= models.Trajet(
          id = rs.getInt("id"),
          ville_depart = rs.getString("ville_depart"),
          ville_arrivee = rs.getString("ville_arrivee"),
          date_depart = rs.getDate("date_depart").toLocalDate,
          heure_depart = rs.getTime("heure_depart").toLocalTime,
          prix_par_place = rs.getInt("prix_par_place"),
          places_totales = rs.getInt("places_totales"),
          statut = rs.getString("statut"),
          conducteur = models.Utilisateur(
            id = rs.getInt("conducteur_id"),
            email = rs.getString("conducteur_email"),
            mot_de_passe = "",
            nom = rs.getString("conducteur_nom"),
            prenom = "",
            telephone = "",
            est_conducteur = true,
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
          ),
          vehicule = null,
          passagers = List.empty
        )
      }
      trajets.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }
} 
