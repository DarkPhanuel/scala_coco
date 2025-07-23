package models

case class Vehicule(
                     id: Int,
                     immatriculation: String,
                     marque: String,
                     modele: String,
                     nombrePlaces: Int = 4,
                     annee: Option[Int],
                     statut: String = "actif"
                   )

