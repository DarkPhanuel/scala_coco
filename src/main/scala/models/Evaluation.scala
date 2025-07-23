package models

import java.sql.Timestamp

case class Evaluation(
                       id: Int,
                       note: Int,
                       commentaire: Option[String],
                       typeEvaluation: String,
                       dateEvaluation: Timestamp,
                       //relations
                       trajet: Trajet,                  
                       evaluateur: Utilisateur,
                       evalue: Utilisateur,
                     )
