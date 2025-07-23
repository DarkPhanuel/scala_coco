package models

import java.sql.Timestamp

case class Paiement(
                     id: Int,
                     numeroTransaction: String,
                     montant: BigDecimal,
                     statut: String,
                     datePaiement: LocalDate
                   )
