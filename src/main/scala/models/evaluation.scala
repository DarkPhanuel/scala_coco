package models

import java.sql.Timestamp

case class evaluation(
                       id: Int,
                       codeEvaluation: String,
                       note: Int,
                       commentaire: Option[String],
                       typeEvaluation: String,
                       dateEvaluation: Timestamp,
                       //relations
                       trajetId: Int,                  
                       evaluateurId: Int,             
                       evalueId: Int,                 
                       createdAt: Timestamp           
                     )
