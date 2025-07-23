package dao

import java.sql.{PreparedStatement, ResultSet, Timestamp}
import models.Evaluation
import DB.DB.connection

object Evaluation_dao {

  // ajouter une évaluation (note d'un utilisateur à un autre pour un trajet)
  def noter(evaluation: Evaluation): Boolean = {
    val sql =
      """
      INSERT INTO evaluations (
        code_evaluation, note, commentaire, type_evaluation, date_evaluation
      )
      VALUES (?, ?, ?, ?, ?) RETURNING id
      """

    val sqlRelation =
      """
      INSERT INTO evaluation_relation (
        evaluation_id, trajet_id, evaluateur_id, evalue_id, created_at
      )
      VALUES (?, ?, ?, ?, ?)
      """

    try {
      // insérer l'évaluation principale avec RETURNING
      val stmt: PreparedStatement = connection.prepareStatement(sql)

      stmt.setString(1, evaluation.codeEvaluation)
      stmt.setInt(2, evaluation.note)
      stmt.setString(3, evaluation.commentaire.orNull)
      stmt.setString(4, evaluation.typeEvaluation)
      stmt.setTimestamp(5, evaluation.dateEvaluation)

      val rs = stmt.executeQuery() // executeQuery pour RETURNING

      if (rs.next()) {
        val evaluationId = rs.getInt("id")

        // insérer la relation
        val stmtRelation: PreparedStatement = connection.prepareStatement(sqlRelation)
        stmtRelation.setInt(1, evaluationId)
        stmtRelation.setInt(2, evaluation.trajetId)
        stmtRelation.setInt(3, evaluation.evaluateurId)
        stmtRelation.setInt(4, evaluation.evalueId)
        stmtRelation.setTimestamp(5, evaluation.createdAt)

        stmtRelation.executeUpdate() > 0
      } else {
        false
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        false
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
      val stmt = connection.prepareStatement(sql)
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
      ORDER BY e.date_evaluation DESC
      """

    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, userId)

      val rs: ResultSet = stmt.executeQuery()
      var evaluations: List[Evaluation] = List()

      while (rs.next()) {
        val evaluation = Evaluation(
          id = rs.getInt("id"),
          codeEvaluation = rs.getString("code_evaluation"),
          note = rs.getInt("note"),
          commentaire = Option(rs.getString("commentaire")),
          typeEvaluation = rs.getString("type_evaluation"),
          dateEvaluation = rs.getTimestamp("date_evaluation"),
          trajetId = rs.getInt("trajet_id"),
          evaluateurId = rs.getInt("evaluateur_id"),
          evalueId = rs.getInt("evalue_id"),
          createdAt = rs.getTimestamp("created_at")
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

  // 🔹 Obtenir toutes les évaluations faites par un utilisateur
  def getEvaluationsFaites(userId: Int): List[Evaluation] = {
    val sql =
      """
      SELECT e.*, er.trajet_id, er.evaluateur_id, er.evalue_id, er.created_at
      FROM evaluations e
      JOIN evaluation_relation er ON e.id = er.evaluation_id
      WHERE er.evaluateur_id = ?
      ORDER BY e.date_evaluation DESC
      """

    try {
      val stmt = connection.prepareStatement(sql)
      stmt.setInt(1, userId)

      val rs: ResultSet = stmt.executeQuery()
      var evaluations: List[Evaluation] = List()

      while (rs.next()) {
        val evaluation = Evaluation(
          id = rs.getInt("id"),
          codeEvaluation = rs.getString("code_evaluation"),
          note = rs.getInt("note"),
          commentaire = Option(rs.getString("commentaire")),
          typeEvaluation = rs.getString("type_evaluation"),
          dateEvaluation = rs.getTimestamp("date_evaluation"),
          trajetId = rs.getInt("trajet_id"),
          evaluateurId = rs.getInt("evaluateur_id"),
          evalueId = rs.getInt("evalue_id"),
          createdAt = rs.getTimestamp("created_at")
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

  // 🔹 Vérifier si un utilisateur a déjà évalué un autre pour un trajet donné
  def dejaEvalue(evaluateurId: Int, evalueId: Int, trajetId: Int): Boolean = {
    val sql =
      """
      SELECT COUNT(*) as count
      FROM evaluation_relation
      WHERE evaluateur_id = ? AND evalue_id = ? AND trajet_id = ?
      """

    try {
      val stmt = connection.prepareStatement(sql)
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