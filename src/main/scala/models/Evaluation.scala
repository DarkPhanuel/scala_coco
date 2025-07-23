package models

import java.sql.Timestamp

case class Evaluation(
                       id: Int,
                       note: Int,
                       commentaire: Option[String],
                       typeEvaluation: String, //relations
                       trajet: Trajet = null,                  
                       evaluateur: Utilisateur = null,
                       evalue: Utilisateur = null,
                     )
