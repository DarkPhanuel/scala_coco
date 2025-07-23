package models

import java.sql.Timestamp
case class messagerie(
                    id: Int,
                    numeroMessage: String,
                    //sujet: Option[String],
                    contenu: String,
                    lu: Boolean = false,                //false par défaut
                    dateLecture: Option[Timestamp],
                    typeMessage: String = "general",
                    statut: String = "envoye",
                    //relations
                    expediteurId: Int,
                    destinataireId: Int,
                    trajetId: Option[Int],
                    reservationId: Option[Int],
                    messageParentId: Option[Int],
                    createdAt: Timestamp)


