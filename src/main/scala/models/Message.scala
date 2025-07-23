package models

import java.sql.Timestamp
case class Message(
                    id: Int,
                    numeroMessage: String,
                    //sujet: Option[String],
                    contenu: String,
                    lu: Boolean = false,                //false par défaut
                    dateLecture: Option[Timestamp],
                    typeMessage: String = "general",
                    statut: String = "envoye",
                    expediteur: Utilisateur,
                    destinataire: Utilisateur,
                    trajetId: Trajet,
                    reservationId: Option[Int],
                    messageParentId: Option[Int],
                    createdAt: Timestamp)




