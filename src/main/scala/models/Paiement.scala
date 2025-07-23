package models

import java.sql.Timestamp
import java.time.LocalDate

case class Paiement(
                     id: Int,
                     numeroTransaction: String,
                     montant: BigDecimal,
                      reservation: Reservation = null,
                     statut: String,
                     datePaiement: Option[LocalDate]                 
)
