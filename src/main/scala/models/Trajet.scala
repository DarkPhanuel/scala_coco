package models

import java.time.{LocalDate, LocalDateTime}

case class Trajet (
                    id: Int,
                    code_trajet:String,
                    ville_depart:String,
                    adresse_depart:String,
                    ville_arrivee:String,
                    adresse_arrivee: String,
                    date_depart:LocalDate,
                    heure_depart: LocalDateTime,
                    prix_par_place: Int,
                    places_disponibles: Int,
                    places_totales : Int,
                    distance_km: Int,
                    duree_estimee: LocalDateTime,
                    description: String,
                    statut : String
  )


