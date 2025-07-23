package models

import java.sql.Timestamp
import java.math.BigDecimal

case class Reservation(
                        id: Int,
                        numeroReservation: String,
                        nombrePlaces: Int,
                        prixTotal: BigDecimal,
                        statut: String,
                        messagePassager: Option[String],
                        dateReservation: Timestamp,
                        dateConfirmation: Option[Timestamp],
                        dateAnnulation: Option[Timestamp],
                        motifAnnulation: Option[String],
                        passager: Utilisateur = null,
                      )
