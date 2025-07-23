package models

import java.time.{LocalDate, LocalDateTime}

case class Trajet (
                    id: Int,
                    ville_depart:String,
                    ville_arrivee:String,
                    date_depart:LocalDate,
                    prix_par_place: Int,
                    places_disponibles: Int,
                    places_totales : Int,
                    statut : String,
                    conducteur: Option[Utilisateur] = None,
                    vehicule: Option[Vehicule] = None
  )
