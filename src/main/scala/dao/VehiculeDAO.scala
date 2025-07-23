package dao

import DB.DB

object VehiculeDAO {
  def addVehicule(vehicule: models.Vehicule): Boolean = {
    val query = "INSERT INTO vehicules (immatriculation, marque, modele, nombre_places, annee, statut) VALUES (?, ?, ?, ?, ?, ?)"
    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, vehicule.immatriculation)
    statement.setString(2, vehicule.marque)
    statement.setString(3, vehicule.modele)
    statement.setInt(4, vehicule.nombrePlaces)
    statement.setInt(5, vehicule.annee)
    statement.setString(6, vehicule.statut)
    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
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
}
