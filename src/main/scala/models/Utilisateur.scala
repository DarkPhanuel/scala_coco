package models

import java.time.{LocalDate, LocalTime}

case class Utilisateur(
  id: Int=0,                    
email: String,
mot_de_passe: String,
nom: String,
prenom: String,
telephone: String,
est_conducteur: Boolean,
ville: String,
code_postal: String,
note_moyenne: Double,
nombre_evaluations: Int,
statut: String,
                      
// Relations :
vehicules: Seq[Vehicule] = Seq.empty,
messagesEnvoyes: Seq[Message] = Seq.empty,
messagesRecus: Seq[Message] = Seq.empty,
trajetsConducteur: Seq[Trajet] = Seq.empty,
reservationsPassager: Seq[Reservation] = Seq.empty,
evaluationsDonnees: Seq[Evaluation] = Seq.empty,
evaluationsRecues: Seq[Evaluation] = Seq.empty

)


