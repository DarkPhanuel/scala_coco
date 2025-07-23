package dao

import DB.DB
import java.sql.{PreparedStatement, ResultSet, Timestamp}
import models.Evaluation

object EvaluationDao {
  def noterUnUtilisateur(evaluation: Evaluation): Int = {
    val sql =
      """
      INSERT INTO evaluations (
        note, commentaire, type_evaluation
      )
      VALUES (?, ?, ?)
      """
    val sqlRelation =
      """
      INSERT INTO evaluation_relation (
        evaluation_id, trajet_id, evaluateur_id, evalue_id, created_at
      )
      VALUES (?, ?, ?, ?, ?)
      """
    try {
      val stmt: PreparedStatement = DB.connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
      stmt.setInt(1, evaluation.note)
      stmt.setString(2, evaluation.commentaire.orNull)
      stmt.setString(3, evaluation.typeEvaluation)
      stmt.executeUpdate()
      val ids = stmt.getGeneratedKeys
      if (ids.next()) {
        val evaluationId = ids.getInt(1)
        val stmtRelation: PreparedStatement = DB.connection.prepareStatement(sqlRelation)
        stmtRelation.setInt(1, evaluationId)
        stmtRelation.setInt(2, evaluation.trajet.id)
        stmtRelation.setInt(3, evaluation.evaluateur.id)
        stmtRelation.setInt(4, evaluation.evalue.id)
        stmtRelation.setTimestamp(5, new Timestamp(System.currentTimeMillis()))
        stmtRelation.executeUpdate()
        evaluationId
      } else {
        throw new RuntimeException("Échec de l'insertion de l'évaluation, aucune clé générée.")
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        -1
    }
  }

  // afficher la moyenne des notes reçues par un utilisateur
  def moyenneNotes(userId: Int): Option[Double] = {
    val sql =
      """
      SELECT AVG(e.note) AS moyenne
      FROM evaluations e
      JOIN evaluation_relation er ON e.id = er.evaluation_id
      WHERE er.evalue_id = ?
      """
    try {
      val stmt = DB.connection.prepareStatement(sql)
      stmt.setInt(1, userId)
      val rs: ResultSet = stmt.executeQuery()
      if (rs.next()) {
        val moyenne = rs.getDouble("moyenne")
        if (rs.wasNull()) None else Some(moyenne)
      } else {
        None
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        None
    }
  }

  // obtenir toutes les évaluations reçues par un utilisateur
  def getEvaluationsRecues(userId: Int): List[Evaluation] = {
    val sql =
      """
      SELECT e.*, er.trajet_id, er.evaluateur_id, er.evalue_id, er.created_at
      FROM evaluations e
      JOIN evaluation_relation er ON e.id = er.evaluation_id
      WHERE er.evalue_id = ?
      ORDER BY e.id DESC
      """
    try {
      val stmt = DB.connection.prepareStatement(sql)
      stmt.setInt(1, userId)
      val rs: ResultSet = stmt.executeQuery()
      var evaluations: List[Evaluation] = List()
      while (rs.next()) {
        val evaluation = Evaluation(
          id = rs.getInt("id"),
          note = rs.getInt("note"),
          commentaire = Option(rs.getString("commentaire")),
          typeEvaluation = rs.getString("type_evaluation"),
          trajet = null, // à compléter selon le modèle
          evaluateur = null, // à compléter selon le modèle
          evalue = null // à compléter selon le modèle
        )
        evaluations = evaluation :: evaluations
      }
      evaluations.reverse
    } catch {
      case e: Exception =>
        e.printStackTrace()
        List()
    }
  }

  // Vérifier si un utilisateur a déjà évalué un autre pour un trajet donné
  def dejaEvalue(evaluateurId: Int, evalueId: Int, trajetId: Int): Boolean = {
    val sql =
      """
      SELECT COUNT(*) as count
      FROM evaluation_relation
      WHERE evaluateur_id = ? AND evalue_id = ? AND trajet_id = ?
      """
    try {
      val stmt = DB.connection.prepareStatement(sql)
      stmt.setInt(1, evaluateurId)
      stmt.setInt(2, evalueId)
      stmt.setInt(3, trajetId)
      val rs: ResultSet = stmt.executeQuery()
      if (rs.next()) {
        rs.getInt("count") > 0
      } else {
        false
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
    }
  }
}