package dao
import DB.DB


object UtilisateurDAO {


def findAll(): Seq[models.Utilisateur] = {
    val query = "SELECT * FROM utilisateurs"
    val statement = DB.connection.prepareStatement(query)
    val resultSet = statement.executeQuery()
    var utilisateurs: Seq[models.Utilisateur] = Seq.empty
    while (resultSet.next()) {
      val utilisateur = models.Utilisateur(
        email = resultSet.getString("email"),
        mot_de_passe = resultSet.getString("mot_de_passe"),
        nom = resultSet.getString("nom"),
        prenom = resultSet.getString("prenom"),
        telephone = resultSet.getString("telephone"),
        est_conducteur = resultSet.getBoolean("est_conducteur"),
        ville = resultSet.getString("ville"),
        code_postal = resultSet.getString("code_postal"),
        note_moyenne = resultSet.getDouble("note_moyenne"),
        nombre_evaluations = resultSet.getInt("nombre_evaluations"),
        statut = resultSet.getString("statut")
      )
      utilisateurs :+= utilisateur
    }

    utilisateurs
  }


def findByUsername(username: String): Option[models.Utilisateur] = {
    val query = "SELECT * FROM utilisateurs WHERE email = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, username)
    val resultSet = statement.executeQuery()

    if (resultSet.next()) {
      Some(models.Utilisateur(
        email = resultSet.getString("email"),
        mot_de_passe = resultSet.getString("mot_de_passe"),
        nom = resultSet.getString("nom"),
        prenom = resultSet.getString("prenom"),
        telephone = resultSet.getString("telephone"),
        est_conducteur = resultSet.getBoolean("est_conducteur"),
        ville = resultSet.getString("ville"),
        code_postal = resultSet.getString("code_postal"),
        note_moyenne = resultSet.getDouble("note_moyenne"),
        nombre_evaluations = resultSet.getInt("nombre_evaluations"),
        statut = resultSet.getString("statut")
      ))
    } else {
      None
    }
  }


def register(utilisateur: models.Utilisateur): Boolean = {
    val query = "INSERT INTO utilisateurs (email, mot_de_passe, nom, prenom, telephone, est_conducteur, ville, code_postal, note_moyenne, nombre_evaluations, statut) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, utilisateur.email)
    statement.setString(2, utilisateur.mot_de_passe)
    statement.setString(3, utilisateur.nom)
    statement.setString(4, utilisateur.prenom)
    statement.setString(5, utilisateur.telephone)
    statement.setBoolean(6, utilisateur.est_conducteur)
    statement.setString(7, utilisateur.ville)
    statement.setString(8, utilisateur.code_postal)
    statement.setDouble(9, utilisateur.note_moyenne)
    statement.setInt(10, utilisateur.nombre_evaluations)
    statement.setString(11, utilisateur.statut)

    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }


def login(email: String, mot_de_passe: String): Option[models.Utilisateur] = {
    val query = "SELECT * FROM utilisateurs WHERE email = ? AND mot_de_passe = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, email)
    statement.setString(2, mot_de_passe)
    val resultSet = statement.executeQuery()

    if (resultSet.next()) {
      Some(models.Utilisateur(
        email = resultSet.getString("email"),
        mot_de_passe = resultSet.getString("mot_de_passe"),
        nom = resultSet.getString("nom"),
        prenom = resultSet.getString("prenom"),
        telephone = resultSet.getString("telephone"),
        est_conducteur = resultSet.getBoolean("est_conducteur"),
        ville = resultSet.getString("ville"),
        code_postal = resultSet.getString("code_postal"),
        note_moyenne = resultSet.getDouble("note_moyenne"),
        nombre_evaluations = resultSet.getInt("nombre_evaluations"),
        statut = resultSet.getString("statut")
      ))
    } else {
      None
    }
  }


def update(utilisateur: models.Utilisateur): Boolean = {
    val query = "UPDATE utilisateurs SET mot_de_passe = ?, nom = ?, prenom = ?, telephone = ?, est_conducteur = ?, ville = ?, code_postal = ?, note_moyenne = ?, nombre_evaluations = ?, statut = ? WHERE email = ?"
    val statement = DB.connection.prepareStatement(query)
    statement.setString(1, utilisateur.mot_de_passe)
    statement.setString(2, utilisateur.nom)
    statement.setString(3, utilisateur.prenom)
    statement.setString(4, utilisateur.telephone)
    statement.setBoolean(5, utilisateur.est_conducteur)
    statement.setString(6, utilisateur.ville)
    statement.setString(7, utilisateur.code_postal)
    statement.setDouble(8, utilisateur.note_moyenne)
    statement.setInt(9, utilisateur.nombre_evaluations)
    statement.setString(10, utilisateur.statut)
    statement.setString(11, utilisateur.email)

    try {
      statement.executeUpdate() > 0
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }

}


