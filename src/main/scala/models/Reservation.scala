package models

import java.sql.Timestamp

case class Reservation(
                      
                        id : Int ,
                        numero_reservation :  String,
                        nombre_places :  Int,
                        prix_total :  Int, 
                        statut : String,
                        message_passager :  String,
                        date_reservation :  Timestamp ,
                        date_confirmation :  Timestamp,
                        date_annulation :  Timestamp,
                        motif_annulation :  String,
                      )
