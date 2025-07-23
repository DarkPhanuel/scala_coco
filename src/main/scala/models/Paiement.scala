package models

import java.sql.Timestamp

case class Paiement(
                     id: Int = 0,
                     numeroTransaction: String,
                     montant: BigDecimal,
                     statut: String = "en_attente",
                     datePaiement: Option[Timestamp] = None
                   )
