package dao

import DB.DB

object VehiculeDAO {
  def addVehicule(vehicule: models.Vehicule): Option[Int] = {
    val query = "INSERT INTO vehicules (immatriculation, marque, modele, nombre_places, annee, statut) VALUES (?, ?, ?, ?, ?, ?::statut_vehicule)"
    val statement = DB.connection.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)
    statement.setString(1, vehicule.immatriculation)
    statement.setString(2, vehicule.marque)
    statement.setString(3, vehicule.modele)
    statement.setInt(4, vehicule.nombrePlaces)
    statement.setInt(5, vehicule.annee)
    statement.setString(6, vehicule.statut)
    try {
      val rows = statement.executeUpdate()
      if (rows > 0) {
        val rs = statement.getGeneratedKeys
        if (rs.next()) Some(rs.getInt(1)) else None
      } else None
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  def getVehicules: Seq[models.Vehicule] = {
    val query = "SELECT * FROM vehicules"
    val statement = DB.connection.prepareStatement(query)
    val resultSet = statement.executeQuery()
    var vehicules: Seq[models.Vehicule] = Seq()
    while (resultSet.next()) {
      val vehicule = models.Vehicule(
        id = resultSet.getInt("id"),
        immatriculation = resultSet.getString("immatriculation"),
        marque = resultSet.getString("marque"),
        modele = resultSet.getString("modele"),
        nombrePlaces = resultSet.getInt("nombre_places"),
        annee = resultSet.getInt("annee"),
        statut = resultSet.getString("statut")
      )
      vehicules :+= vehicule
    }
    vehicules
  }

  // Affecter un véhicule à un utilisateur
  def affecterVehicule(utilisateurId: Int, vehicule: models.Vehicule): Boolean = {
    val query = "INSERT INTO utilisateur_vehicule (utilisateur_id, vehicule_id) VALUES (?, ?)"
    try {
      val statement = DB.connection.prepareStatement(query)
      statement.setInt(1, utilisateurId)
      statement.setInt(2, vehicule.id)
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

  // Récupérer les véhicules d'un utilisateur
  def getVehiculesPourUtilisateur(utilisateurId: Int): Seq[models.Vehicule] = {
    val query = "SELECT v.* FROM vehicules v JOIN utilisateur_vehicule uv ON v.id = uv.vehicule_id WHERE uv.utilisateur_id = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setInt(1, utilisateurId)
    val resultSet = statement.executeQuery()
    var vehicules: Seq[models.Vehicule] = Seq()
    while (resultSet.next()) {
      val vehicule = models.Vehicule(
        id = resultSet.getInt("id"),
        immatriculation = resultSet.getString("immatriculation"),
        marque = resultSet.getString("marque"),
        modele = resultSet.getString("modele"),
        nombrePlaces = resultSet.getInt("nombre_places"),
        annee = resultSet.getInt("annee"),
        statut = resultSet.getString("statut")
      )
      vehicules :+= vehicule
    }
    vehicules
  }
}
