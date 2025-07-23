package menu

import dao.{EvaluationDao, TrajetDAO, UtilisateurDAO, ReservationDAO}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object MenuPrincipal  {


  // Variables pour simuler l'état de l'application
  private var utilisateurConnecte: Option[models.Utilisateur] = None
  private var running = true

  // Démarrage de l'application
  println("=== BIENVENUE DANS L'APPLICATION DE COVOITURAGE ===")

  while (running) {
    if (utilisateurConnecte.isEmpty) {
      afficherMenuConnexion()
    } else {
      afficherMenuPrincipal()
    }
  }

  def afficherMenuConnexion(): Unit = {
    println("\n" + "="*50)
    println("MENU DE CONNEXION")
    println("="*50)
    println("1. S'inscrire")
    println("2. Se connecter")
    println("3. Quitter l'application")
    println("="*50)
    print("Votre choix : ")

    val choix = StdIn.readLine()
    choix match {
      case "1" => inscription()
      case "2" => connexion()
      case "3" =>
        println("Au revoir !")
        running = false
      case _ => println("Choix invalide. Veuillez réessayer.")
    }
  }

  def afficherMenuPrincipal(): Unit = {
    println("\n" + "="*60)
    println(s"MENU PRINCIPAL - Connecté en tant que: ${utilisateurConnecte.get}")
    println("="*60)
    println("GESTION DU COMPTE")
    println("1. Modifier mon compte")
    println("2. Supprimer mon compte")
    println("3. Affecter un véhicule")
    println()
    println("GESTION DES TRAJETS")
    println("4. Proposer un trajet")
    println("5. Supprimer un trajet")
    println("6. Afficher mes trajets à venir")
    println("7. Afficher mes trajets passés")
    println()
    println("RÉSERVATIONS")
    println("8. Rechercher un trajet")
    println("9. Mes réservations")
    println("10. Annuler une réservation")
    println()
    println("PAIEMENTS")
    println("11. Simuler un paiement")
    println("12. Historique des paiements")
    println()
    println("NOTATIONS")
    println("13. Noter un utilisateur")
    println("14. Voir mes notes")
    println()
    println("MESSAGERIE")
    println("15. Envoyer un message")
    println("16. Voir mes messages")
    println()
    println("17. Se déconnecter")
    println("18. Quitter l'application")
    println("="*60)
    print("Votre choix : ")

    val choix = StdIn.readLine()
    traiterChoixPrincipal(choix)
  }

  def traiterChoixPrincipal(choix: String): Unit = {
    choix match {
      // Gestion du compte
      case "1" => modifierCompte()
      case "2" => supprimerCompte()
      case "3" => affecterVehicule()

      // Gestion des trajets
      case "4" => proposerTrajet()
      case "5" => supprimerTrajet()
      case "6" => afficherTrajetsAVenir()
      case "7" => afficherTrajetsPasses()

      // Réservations
      case "8" => rechercherTrajet()
      case "9" => afficherMesReservations()
      case "10" => annulerReservation()

      // Paiements
      case "11" => simulerPaiement()
      case "12" => historiquePaiements()

      // Notations
      case "13" => noterUtilisateur()
      case "14" => voirMesNotes()

      // Messagerie
      case "15" => envoyerMessage()
      case "16" => voirMessages()

      // Déconnexion/Sortie
      case "17" =>
        utilisateurConnecte = None
        println("Déconnexion réussie.")
      case "18" =>
        println("Au revoir !")
        running = false
      case _ => println("Choix invalide. Veuillez réessayer.")
    }
  }

  // ===== FONCTIONS DE GESTION DU COMPTE =====


  def inscription(): Unit = {
    println("\n--- INSCRIPTION ---")
    print("Email : ")
    val email = StdIn.readLine()
    print("Nom: ")
    val nom = StdIn.readLine()
    print("Prenom: ")
    val prenom = StdIn.readLine()
    print("Mot de passe : ")
    val motDePasse = StdIn.readLine()
    print("Téléphone : ")
    val telephone = StdIn.readLine()
    print("Ville : ")
    val ville = StdIn.readLine()
    print("Code postal : ")
    val codePostal = StdIn.readLine()
    print("Êtes-vous conducteur ? (oui/non) : ")
    val estConducteur = StdIn.readLine().toLowerCase match {
      case "oui" => true
      case "non" => false
      case _ =>
        println("Réponse invalide, par défaut vous n'êtes pas conducteur.")
        false
    }
    val userId:Int = UtilisateurDAO.register(
      models.Utilisateur(
        email = email,
        mot_de_passe = motDePasse,
        nom = nom,
        prenom = prenom,
        telephone = telephone,
        est_conducteur = estConducteur,
        ville = ville,
        code_postal = codePostal,
        note_moyenne = 0.0,
        nombre_evaluations = 0,
        statut = "Actif"
      )
    )
    utilisateurConnecte =  UtilisateurDAO.getUtilisateurById(userId);
  }

  def connexion(): Unit = {
    println("\n--- CONNEXION ---")
    print("Email : ")
    val email = StdIn.readLine()
    print("Mot de passe : ")
    val motDePasse = StdIn.readLine()

    val utilisateur = UtilisateurDAO.login(email, motDePasse)

    utilisateur match {
      case Some(u) => {
        utilisateurConnecte = utilisateur
        println("Connexion réussie !")
      }
      case None => println("Mot de passe ou email incorrect. Veuillez réessayer.")
    }
  }

  def modifierCompte(): Unit = {
    println("\n--- MODIFICATION DU COMPTE ---")
    print("Nouveau nom d'utilisateur (Entrée pour garder l'actuel) : ")
    val nouveauNom = StdIn.readLine()
    print("Nouvel email (Entrée pour garder l'actuel) : ")
    val nouvelEmail = StdIn.readLine()

    if (nouveauNom.nonEmpty) {
      utilisateurConnecte = utilisateurConnecte.map(u => u.copy(nom = nouveauNom))
      utilisateurConnecte = utilisateurConnecte.map(u => u.copy(email = nouvelEmail))
    }

    utilisateurConnecte match {
      case Some(u) => UtilisateurDAO.update(u)
      case None    => println("Aucun utilisateur connecté, update ignoré.")
    }
    println("Compte modifié avec succès !")
  }

  def supprimerCompte(): Unit = {
    println("\n--- SUPPRESSION DU COMPTE ---")
    print("Êtes-vous sûr de vouloir supprimer votre compte ? (oui/non) : ")
    val confirmation = StdIn.readLine().toLowerCase

    if (confirmation == "oui") {

      utilisateurConnecte match {
        case Some(u) =>       UtilisateurDAO.supprimerUtilisateur(u.id);

        case None => println("Aucun utilisateur connecté, update ignoré.")
      }
      utilisateurConnecte = None
    } else {
      println("Suppression annulée.")
    }
  }

  def affecterVehicule(): Unit = {
    println("\n--- AFFECTATION D'UN VÉHICULE ---")
    utilisateurConnecte match {
      case Some(u) =>
        print("Marque : ")
        val marque = StdIn.readLine()
        print("Modèle : ")
        val modele = StdIn.readLine()
        print("Plaque d'immatriculation : ")
        val plaque = StdIn.readLine()
        print("Nombre de places : ")
        val places = Try(StdIn.readLine().toInt).getOrElse(4)
        print("Année : ")
        val annee = Try(StdIn.readLine().toInt).getOrElse(2023)
        val vehicule = models.Vehicule(0, plaque, marque, modele, places, annee, "actif")
        dao.VehiculeDAO.addVehicule(vehicule) match {
          case Some(idVehicule) =>
            val vAjoute = vehicule.copy(id = idVehicule)
            if (dao.VehiculeDAO.affecterVehicule(u.id, vAjoute))
              println(s"Véhicule ${marque} ${modele} ($plaque) affecté avec succès !")
            else
              println("Erreur lors de l'affectation du véhicule.")
          case None =>
            println("Erreur lors de l'ajout du véhicule.")
        }
      case None => println("Aucun utilisateur connecté.")
    }
  }

  // ===== FONCTIONS DE GESTION DES TRAJETS =====

  def proposerTrajet(): Unit = {
    val formatter = DateTimeFormatter.ofPattern("d/M/yyyy")

    println("\n--- PROPOSER UN TRAJET ---")
    print("Ville de départ : ")
    val depart = StdIn.readLine()
    print("Ville d'arrivée : ")
    val arrivee = StdIn.readLine()
    print("Date (JJ/MM/AAAA) : ")
    val dateStr = StdIn.readLine()
    print("Heure de départ (HH:mm) : ")
    val heureDepartStr = StdIn.readLine()
    val heureDepart = Try(java.time.LocalTime.parse(heureDepartStr)).getOrElse({ println("Heure invalide, 00:00 utilisée."); java.time.LocalTime.MIDNIGHT })
    print("Prix par place : ")
    val prixStr = StdIn.readLine()
    val prix = Try(prixStr.toInt).getOrElse({ println("Prix invalide, valeur par défaut 1 utilisée."); 1 })
    print("Places Total : ")
    val placesTotalStr = StdIn.readLine()
    val placesTotal = Try(placesTotalStr.toInt).getOrElse({ println("Nombre de places invalide, valeur par défaut 1 utilisée."); 1 })
    val date = Try(LocalDate.parse(dateStr, formatter)).getOrElse(LocalDate.now())

    utilisateurConnecte match {
      case Some(u) =>
        // Récupérer les véhicules de l'utilisateur
        val vehicules = dao.VehiculeDAO.getVehiculesPourUtilisateur(u.id)
        val vehicule =
          if (vehicules.isEmpty) {
            println("Vous n'avez pas de véhicule. Veuillez en ajouter un.")
            print("Marque : ")
            val marque = StdIn.readLine()
            print("Modèle : ")
            val modele = StdIn.readLine()
            print("Plaque d'immatriculation : ")
            val plaque = StdIn.readLine()
            print("Nombre de places : ")
            val placesVehicule = Try(StdIn.readLine().toInt).getOrElse(4)
            print("Année : ")
            val annee = Try(StdIn.readLine().toInt).getOrElse(java.time.LocalDate.now().getYear)
            val v = models.Vehicule(0, plaque, marque, modele, placesVehicule, annee, "actif")
            dao.VehiculeDAO.addVehicule(v) match {
              case Some(idVehicule) =>
                val vAjoute = v.copy(id = idVehicule)
                dao.VehiculeDAO.affecterVehicule(u.id, vAjoute)
                vAjoute
              case None =>
                println("Erreur lors de l'ajout du véhicule. Opération annulée."); return
            }
          } else {
            println("Sélectionnez un véhicule pour ce trajet :")
            vehicules.zipWithIndex.foreach { case (v, i) =>
              println(s"${i + 1}. ${v.marque} ${v.modele} (${v.immatriculation})")
            }
            print("Numéro du véhicule : ")
            val num = Try(StdIn.readLine().toInt).getOrElse(1)
            vehicules.lift(num - 1).getOrElse(vehicules.head)
          }
        val trajet: models.Trajet = models.Trajet(
          id = 0,
          ville_depart = depart,
          ville_arrivee = arrivee,
          date_depart = date,
          heure_depart = heureDepart,
          prix_par_place = prix,
          places_totales = placesTotal,
          statut = "Proposé",
          vehicule = vehicule,
          conducteur = u
        )
        TrajetDAO.proposerUnTrajet(trajet = trajet)
        println("Trajet proposé avec succès !")
      case None => println("Mot de passe ou email incorrect. Veuillez réessayer.")
    }
  }

  def afficherTrajetsAVenir(): Unit = {
    println("\n--- MES TRAJETS À VENIR ---")
    utilisateurConnecte match {
      case Some(u) =>
        val trajets = dao.TrajetDAO.getTrajetsPourUtilisateur(u.id, aVenir = true)
        if (trajets.isEmpty) println("Aucun trajet à venir.")
        else trajets.zipWithIndex.foreach { case (t, i) =>
          println(s"${i + 1}. ${t.ville_depart} → ${t.ville_arrivee} - ${t.date_depart} (${t.places_totales} places)")
        }
      case None => println("Aucun utilisateur connecté.")
    }
    attendre()
  }

  def afficherTrajetsPasses(): Unit = {
    println("\n--- MES TRAJETS PASSÉS ---")
    utilisateurConnecte match {
      case Some(u) =>
        val trajets = dao.TrajetDAO.getTrajetsPourUtilisateur(u.id, aVenir = false)
        if (trajets.isEmpty) println("Aucun trajet passé.")
        else trajets.zipWithIndex.foreach { case (t, i) =>
          println(s"${i + 1}. ${t.ville_depart} → ${t.ville_arrivee} - ${t.date_depart} (${t.places_totales} places)")
        }
      case None => println("Aucun utilisateur connecté.")
    }
    attendre()
  }

  def supprimerTrajet(): Unit = {
    println("\n--- SUPPRIMER UN TRAJET ---")
    utilisateurConnecte match {
      case Some(u) =>
        val trajets = dao.TrajetDAO.getTrajetsPourUtilisateur(u.id, aVenir = true)
        if (trajets.isEmpty) {
          println("Aucun trajet à supprimer.")
          return
        }
        trajets.zipWithIndex.foreach { case (t, i) =>
          println(s"${i + 1}. ${t.ville_depart} → ${t.ville_arrivee} - ${t.date_depart}")
        }
        print("Numéro du trajet à supprimer : ")
        val numero = StdIn.readLine()
        Try(numero.toInt).toOption.filter(n => n > 0 && n <= trajets.length) match {
          case Some(idx) =>
            val trajetId = trajets(idx - 1).id
            if (dao.TrajetDAO.supprimerTrajet(trajetId))
              println("Trajet supprimé avec succès !")
            else
              println("Erreur lors de la suppression du trajet.")
          case None => println("Numéro invalide.")
        }
      case None => println("Aucun utilisateur connecté.")
    }
  }

  // ===== FONCTIONS DE RÉSERVATION =====

  def afficherMesReservations(): Unit = {
    println("\n--- MES RÉSERVATIONS ---")
    utilisateurConnecte match {
      case Some(u) =>
        val reservations = ReservationDAO.getReservationsPourUtilisateur(u.id)
        if (reservations.isEmpty) println("Aucune réservation trouvée.")
        else reservations.zipWithIndex.foreach { case (r, i) =>
          println(s"${i + 1}. ${r.numeroReservation} - ${r.statut} - ${r.dateReservation}")
        }
      case None => println("Aucun utilisateur connecté.")
    }
    attendre()
  }

  def annulerReservation(): Unit = {
    println("\n--- ANNULER UNE RÉSERVATION ---")
    utilisateurConnecte match {
      case Some(u) =>
        val reservations = ReservationDAO.getReservationsPourUtilisateur(u.id)
        if (reservations.isEmpty) {
          println("Aucune réservation à annuler.")
          return
        }
        reservations.zipWithIndex.foreach { case (r, i) =>
          println(s"${i + 1}. ${r.numeroReservation} - ${r.statut}")
        }
        print("Numéro de la réservation à annuler : ")
        val numero = StdIn.readLine()
        Try(numero.toInt).toOption.filter(n => n > 0 && n <= reservations.length) match {
          case Some(idx) =>
            val reservationId = reservations(idx - 1).id
            if (ReservationDAO.annulerReservation(reservationId))
              println("Réservation annulée avec succès !")
            else
              println("Erreur lors de l'annulation de la réservation.")
          case None => println("Numéro invalide.")
        }
      case None => println("Aucun utilisateur connecté.")
    }
  }

  def rechercherTrajet(): Unit = {
    println("\n--- RECHERCHER UN TRAJET ---")
    print("Ville de départ : ")
    val depart = StdIn.readLine()
    print("Ville d'arrivée : ")
    val arrivee = StdIn.readLine()
    print("Date (JJ/MM/AAAA) : ")
    val dateStr = StdIn.readLine()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy")
    val date = Try(java.time.LocalDate.parse(dateStr, formatter)).getOrElse(java.time.LocalDate.now())
    val trajets = dao.TrajetDAO.rechercherTrajets(depart, arrivee, date)
    if (trajets.isEmpty) {
      println(s"Aucun trajet trouvé pour $depart → $arrivee le $dateStr.")
      return
    }
    trajets.zipWithIndex.foreach { case (t, i) =>
      println(s"${i + 1}. Départ: ${t.ville_depart} → ${t.ville_arrivee} - ${t.date_depart} - ${t.prix_par_place}€ (${t.places_totales} places)")
    }
    print("\nRéserver le trajet numéro (0 pour annuler) : ")
    val choix = StdIn.readLine()
    if (choix != "0") {
      Try(choix.toInt).toOption.filter(n => n > 0 && n <= trajets.length) match {
        case Some(idx) =>
          val trajet = trajets(idx - 1)
          print("Combien de places souhaitez-vous réserver ? ")
          val places = Try(StdIn.readLine().toInt).getOrElse(1)
          utilisateurConnecte match {
            case Some(u) =>
              val reservation = models.Reservation(
                id = 0,
                numeroReservation = java.util.UUID.randomUUID().toString,
                nombrePlaces = places,
                prixTotal = java.math.BigDecimal.valueOf(trajet.prix_par_place * places),
                statut = "En attente",
                messagePassager = None,
                dateReservation = new java.sql.Timestamp(System.currentTimeMillis()),
                dateConfirmation = None,
                dateAnnulation = None,
                motifAnnulation = None,
                passager = u
              )
              val resId = ReservationDAO.creerReservation(reservation)
              if (resId > 0) println("Réservation confirmée !") else println("Erreur lors de la réservation.")
            case None => println("Aucun utilisateur connecté.")
          }
        case None => println("Numéro invalide.")
      }
    }
  }

  // ===== FONCTIONS DE PAIEMENT =====

  def simulerPaiement(): Unit = {
    println("\n--- SIMULER UN PAIEMENT ---")
    print("Montant (€) : ")
    val montant = BigDecimal(Try(StdIn.readLine().toDouble).getOrElse(0.0))
    print("Destinataire (email) : ")
    val destinataireEmail = StdIn.readLine()
    print("Motif : ")
    val motif = StdIn.readLine()
    utilisateurConnecte match {
      case Some(u) =>
        val destinataireOpt = dao.UtilisateurDAO.findByUsername(destinataireEmail)
        destinataireOpt match {
          case Some(dest) =>
            val paiement = models.Paiement(
              id = 0,
              numeroTransaction = java.util.UUID.randomUUID().toString,
              montant = montant,
              reservation = null, // à compléter si besoin
              statut = "Effectué",
              datePaiement = Some(java.time.LocalDate.now())
            )
            import DB.DB.connection
            if (dao.PaiementDAO.creerPaiement(paiement)(connection))
              println(s"Paiement de $montant€ vers ${dest.nom} effectué avec succès !")
            else
              println("Erreur lors de l'enregistrement du paiement.")
          case None => println("Destinataire introuvable.")
        }
      case None => println("Aucun utilisateur connecté.")
    }
  }

  def historiquePaiements(): Unit = {
    println("\n--- HISTORIQUE DES PAIEMENTS ---")
    utilisateurConnecte match {
      case Some(u) =>
        import DB.DB.connection
        val paiements = dao.PaiementDAO.getPaiementsPourUtilisateur(u.id)(connection)
        if (paiements.isEmpty) println("Aucun paiement trouvé.")
        else paiements.foreach { p =>
          println(s"${p.datePaiement.getOrElse("")} - ${p.montant}€ - ${p.statut} (Transaction: ${p.numeroTransaction})")
        }
      case None => println("Aucun utilisateur connecté.")
    }
    attendre()
  }

  // ===== FONCTIONS DE NOTATION =====

  def noterUtilisateur(): Unit = {
    println("\n--- NOTER UN UTILISATEUR ---")
    print("Email de l'utilisateur à noter : ")
    val email = StdIn.readLine()
    val utilisateurOpt = dao.UtilisateurDAO.findByUsername(email)
    utilisateurOpt match {
      case Some(evalue) =>
        print("Note (1-5 étoiles) : ")
        val note = Try(StdIn.readLine().toInt).getOrElse(5)
        print("Commentaire (optionnel) : ")
        val commentaire = StdIn.readLine()
        utilisateurConnecte match {
          case Some(evaluateur) =>
            val evaluation = models.Evaluation(
              id = 0,
              note = note,
              commentaire = if (commentaire.nonEmpty) Some(commentaire) else None,
              typeEvaluation = "utilisateur",
              trajet = null,
              evaluateur = evaluateur,
              evalue = evalue
            )
            val res = dao.EvaluationDao.noterUnUtilisateur(evaluation)
            if (res > 0) println("Note enregistrée !") else println("Erreur lors de l'enregistrement de la note.")
          case None => println("Aucun utilisateur connecté.")
        }
      case None => println("Utilisateur à noter introuvable.")
    }
  }

  def voirMesNotes(): Unit = {
    println("\n--- MES NOTES ---")
    utilisateurConnecte match {
      case Some(u) =>
        val moyenne = dao.EvaluationDao.moyenneNotes(u.id).getOrElse(0.0)
        println(f"Note moyenne reçue : $moyenne%.2f/5")
        val commentaires = dao.EvaluationDao.getCommentairesRecus(u.id)
        if (commentaires.isEmpty) println("Aucun commentaire.")
        else {
          println("\nDerniers commentaires :")
          commentaires.foreach(c => println(s"- $c"))
        }
      case None => println("Aucun utilisateur connecté.")
    }
    attendre()
  }

  // ===== FONCTIONS DE MESSAGERIE =====

  def envoyerMessage(): Unit = {
    println("\n--- ENVOYER UN MESSAGE ---")
    print("Destinataire (email) : ")
    val destinataireEmail = StdIn.readLine()
    val destinataireOpt = dao.UtilisateurDAO.findByUsername(destinataireEmail)
    destinataireOpt match {
      case Some(destinataire) =>
        print("Sujet : ")
        val sujet = StdIn.readLine()
        print("Message : ")
        val contenu = StdIn.readLine()
        utilisateurConnecte match {
          case Some(expediteur) =>
            val message = models.Message(
              id = 0,
              numeroMessage = java.util.UUID.randomUUID().toString,
              contenu = contenu,
              lu = false,
              dateLecture = None,
              typeMessage = sujet,
              statut = "envoye",
              expediteur = expediteur,
              destinataire = destinataire,
              trajetId = null,
              reservationId = None,
              messageParentId = None,
              createdAt = new java.sql.Timestamp(System.currentTimeMillis())
            )
            val res = dao.MessageDAO.envoyerUnMessage(message)
            if (res > 0) println("Message envoyé avec succès !") else println("Erreur lors de l'envoi du message.")
          case None => println("Aucun utilisateur connecté.")
        }
      case None => println("Destinataire introuvable.")
    }
  }

  def voirMessages(): Unit = {
    println("\n--- MES MESSAGES ---")
    utilisateurConnecte match {
      case Some(u) =>
        val recus = dao.MessageDAO.getMessagesRecus(u.id)
        val envoyes = dao.MessageDAO.getMessagesEnvoyes(u.id)
        println("MESSAGES REÇUS :")
        if (recus.isEmpty) println("Aucun message reçu.")
        else recus.zipWithIndex.foreach { case (m, i) =>
          println(s"${i + 1}. De ${Option(m.expediteur).map(_.nom).getOrElse("")} - '${m.typeMessage}' (${if (m.lu) "Lu" else "Non lu"})")
        }
        println()
        println("MESSAGES ENVOYÉS :")
        if (envoyes.isEmpty) println("Aucun message envoyé.")
        else envoyes.zipWithIndex.foreach { case (m, i) =>
          println(s"${i + 1}. À ${Option(m.destinataire).map(_.nom).getOrElse("")} - '${m.typeMessage}'")
        }
        print("\nLire le message numéro (0 pour retour) : ")
        val numero = StdIn.readLine()
        if (numero != "0") {
          Try(numero.toInt).toOption.filter(n => n > 0 && n <= recus.length) match {
            case Some(idx) =>
              val m = recus(idx - 1)
              println(s"\n--- MESSAGE ${idx} ---")
              println(s"De: ${Option(m.expediteur).map(_.nom).getOrElse("")}")
              println(s"Sujet: ${m.typeMessage}")
              println(s"Date: ${m.createdAt}")
              println(s"Message: ${m.contenu}")
              attendre()
            case None => println("Numéro invalide.")
          }
        }
      case None => println("Aucun utilisateur connecté.")
    }
  }

  // ===== FONCTION UTILITAIRE =====

  def attendre(): Unit = {
    print("\nAppuyez sur Entrée pour continuer...")
    StdIn.readLine()
  }
}
