package models

import java.sql.Timestamp
import java.time.LocalDate

case class Paiement(
                     id: Int,
                     numeroTransaction: String,
                     montant: BigDecimal,
                     statut: String,
                     datePaiement: LocalDate
                   )
