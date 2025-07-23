package models

import java.time.{LocalDate, LocalTime}

case class Trajet(
  id: Int,
  ville_depart: String,
  ville_arrivee: String,
  date_depart: LocalDate = LocalDate.now(),
  heure_depart: LocalTime,
  prix_par_place: Int,
  places_totales: Int,
  statut: String = "prevu",
  conducteur: Utilisateur,
  vehicule: Vehicule,
  passagers: List[Utilisateur] = List.empty
)