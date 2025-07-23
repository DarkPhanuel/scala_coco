package models

import java.sql.Timestamp

case class Paiement(
                     id: Int = 0,                         // Identifiant unique (auto-incrémenté en base)
                     numeroTransaction: String,           // Numéro unique de la transaction
                     montant: BigDecimal,                 // Montant du paiement
                     statut: String = "en_attente",      // Statut : en_attente, complete, echue, rembourse
                     datePaiement: Option[Timestamp] = None  // Date et heure du paiement, optionnelle
                   )
