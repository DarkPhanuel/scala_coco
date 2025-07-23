package dao

import models.Message
import java.sql.PreparedStatement

object MessageDAO {
  private val connection: java.sql.Connection = DB.DB.connection

  def envoyerUnMessage(message: Message): Int = {
    val sql =
      """INSERT INTO messages (numero_message, contenu, lu, date_lecture, type_msg, statut_msg)
         |VALUES (?, ?, ?, ?, ?, ?)""".stripMargin
    try {
      val stmt: PreparedStatement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
      stmt.setString(1, message.numeroMessage)
      stmt.setString(2, message.contenu)
      stmt.setBoolean(3, message.lu)
      stmt.setTimestamp(4, message.dateLecture.orNull)
      stmt.setString(5, message.typeMessage)
      stmt.setString(6, message.statut)
      stmt.executeUpdate()
      val keys = stmt.getGeneratedKeys
      if (keys.next()) {
        val messageId = keys.getInt(1)
        val requeteLiaison = "INSERT INTO message_relation (message_id, expediteur_id, destinataire_id) VALUES (?, ?, ?)"
        val statementLiaison = connection.prepareStatement(requeteLiaison)
        statementLiaison.setInt(1, messageId)
        statementLiaison.setInt(2, message.expediteur.id)
        statementLiaison.setInt(3, message.destinataire.id)
        statementLiaison.executeUpdate()
        messageId
      } else {
        throw new RuntimeException("Échec de l'insertion du message, aucune clé générée.")
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        -1
    }
  }

  // Récupérer les messages reçus par un utilisateur
  def getMessagesRecus(utilisateurId: Int): List[Message] = {
    val sql =
      """
      SELECT m.*, mr.created_at AS relation_created_at
      FROM messages m
      JOIN message_relation mr ON m.id = mr.message_id
      WHERE mr.expediteur_id = ?
      ORDER BY mr.created_at DESC
    """
    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, utilisateurId)
      val rs = stmt.executeQuery()
      var messages: List[Message] = List()
      while (rs.next()) {
        messages = Message(
          id = rs.getInt("id"),
          numeroMessage = rs.getString("numero_message"),
          typeMessage = rs.getString("type_msg"),
          contenu = rs.getString("contenu"),
          lu = rs.getBoolean("lu"),
          dateLecture = Option(rs.getTimestamp("date_lecture")),
          statut = rs.getString("statut_msg"),
          expediteur = null, 
          destinataire = null, 
          createdAt = rs.getTimestamp("relation_created_at")
        ) :: messages
      }
      messages.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  // Récupérer les messages envoyés par un utilisateur
  def getMessagesEnvoyes(utilisateurId: Int): List[Message] = {
    val sql =
      """
      SELECT m.*, mr.created_at AS relation_created_at
      FROM messages m
      JOIN message_relation mr ON m.id = mr.message_id
      WHERE mr.expediteur_id = ?
      ORDER BY mr.created_at DESC
    """

    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, utilisateurId)
      val rs = stmt.executeQuery()
      var messages: List[Message] = List()
      while (rs.next()) {
        messages = Message(
          id = rs.getInt("id"),
          numeroMessage = rs.getString("numero_message"),
          typeMessage = rs.getString("type_msg"),
          contenu = rs.getString("contenu"),
          lu = rs.getBoolean("lu"),
          dateLecture = Option(rs.getTimestamp("date_lecture")),
          statut = rs.getString("statut_msg"),
          expediteur = null,
          destinataire = null,
          createdAt = rs.getTimestamp("relation_created_at")
        ) :: messages
      }
      messages.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }
}

