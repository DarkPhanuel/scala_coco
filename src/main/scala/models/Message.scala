package models

import java.sql.Timestamp
case class Message(
                    id: Int,
                    numeroMessage: String,
                    typeMessage: String,
                    contenu: String,
                    lu: Boolean = false,                //false par défaut
                    dateLecture: Option[Timestamp],
                    statut: String = "envoye",
                    expediteur: Utilisateur,
                    destinataire: Utilisateur,
                    createdAt: Timestamp)




