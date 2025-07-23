package menu

import dao.{EvaluationDao, TrajetDAO, UtilisateurDAO}

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
    print("Marque : ")
    val marque = StdIn.readLine()
    print("Modèle : ")
    val modele = StdIn.readLine()
    print("Plaque d'immatriculation : ")
    val plaque = StdIn.readLine()
    print("Nombre de places : ")
    val places = StdIn.readLine()

    println(s"Véhicule $marque $modele ($plaque) affecté avec succès !")
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
    val date = StdIn.readLine()
    print("Prix par place : ")
    val prix = StdIn.readLine()
    print("Places Total : ")
    val placesTotal = StdIn.readLine()


    utilisateurConnecte match {
      case Some(u) => {

        val trajet: models.Trajet = models.Trajet(
          id = 0, // ID sera généré par la base de données
          ville_depart = depart,
          ville_arrivee = arrivee,
          prix_par_place = prix.toInt,
          places_totales = placesTotal.toInt,
          statut = "Proposé",
          vehicule = models.Vehicule(
            id = 0, // ID sera généré par la base de données
            immatriculation = "ABC123", // À remplacer par une logique d'affectation de véhicule
            marque = "Marque",
            modele = "Modèle",
            nombrePlaces = placesTotal.toInt,
            annee = 2025, // À remplacer par l'année actuelle ou une logique d'affectation
            statut = "Disponible"
          ),
          conducteur = u // Utilisateur connecté est le conducteur
        )

        TrajetDAO.proposerUnTrajet(
          trajet = trajet
        )
        println("Connexion réussie !")
      }
      case None => println("Mot de passe ou email incorrect. Veuillez réessayer.")
    }



  }

  def supprimerTrajet(): Unit = {
    println("\n--- SUPPRIMER UN TRAJET ---")
    println("Vos trajets proposés :")
    println("1. Paris → Lyon (15/08/2025)")
    println("2. Marseille → Nice (20/08/2025)")
    print("Numéro du trajet à supprimer : ")
    val numero = StdIn.readLine()

    println(s"Trajet $numero supprimé avec succès !")
  }

  def afficherTrajetsAVenir(): Unit = {
    println("\n--- MES TRAJETS À VENIR ---")
    println("1. Paris → Lyon - 15/08/2025 à 14:00 (2 places libres)")
    println("2. Marseille → Nice - 20/08/2025 à 09:30 (1 place libre)")
    println("3. Bordeaux → Toulouse - 25/08/2025 à 16:00 (3 places libres)")
    attendre()
  }

  def afficherTrajetsPasses(): Unit = {
    println("\n--- MES TRAJETS PASSÉS ---")
    println("1. Lyon → Paris - 10/07/2025 à 18:00 (Complet)")
    println("2. Nice → Cannes - 05/07/2025 à 10:00 (2 passagers)")
    attendre()
  }

  // ===== FONCTIONS DE RÉSERVATION =====

  def rechercherTrajet(): Unit = {
    println("\n--- RECHERCHER UN TRAJET ---")
    print("Ville de départ : ")
    val depart = StdIn.readLine()
    print("Ville d'arrivée : ")
    val arrivee = StdIn.readLine()
    print("Date (JJ/MM/AAAA) : ")
    val date = StdIn.readLine()

    println(s"\nTrajets trouvés pour $depart → $arrivee le $date :")
    println("1. Départ 08:00 - Conducteur: Marie - 15€ (2 places libres)")
    println("2. Départ 14:30 - Conducteur: Pierre - 12€ (1 place libre)")
    println("3. Départ 19:00 - Conducteur: Sophie - 18€ (3 places libres)")

    print("\nRéserver le trajet numéro (0 pour annuler) : ")
    val choix = StdIn.readLine()

    if (choix != "0") {
      print("Combien de places souhaitez-vous réserver ? ")
      val places = StdIn.readLine()
      println(s"Réservation de $places place(s) confirmée pour le trajet $choix !")
    }
  }

  def afficherMesReservations(): Unit = {
    println("\n--- MES RÉSERVATIONS ---")
    println("1. Paris → Lyon - 22/08/2025 à 10:00 - Conducteur: Jean (Confirmée)")
    println("2. Nice → Monaco - 28/08/2025 à 15:30 - Conducteur: Anna (En attente)")
    attendre()
  }

  def annulerReservation(): Unit = {
    println("\n--- ANNULER UNE RÉSERVATION ---")
    println("Vos réservations :")
    println("1. Paris → Lyon - 22/08/2025 à 10:00")
    println("2. Nice → Monaco - 28/08/2025 à 15:30")
    print("Numéro de la réservation à annuler : ")
    val numero = StdIn.readLine()

    println(s"Réservation $numero annulée avec succès !")
  }

  // ===== FONCTIONS DE PAIEMENT =====

  def simulerPaiement(): Unit = {
    println("\n--- SIMULER UN PAIEMENT ---")
    print("Montant (€) : ")
    val montant = StdIn.readLine()
    print("Destinataire : ")
    val destinataire = StdIn.readLine()
    print("Motif : ")
    val motif = StdIn.readLine()

    println("Simulation du paiement...")
    Thread.sleep(2000)
    println(s"Paiement de $montant€ vers $destinataire effectué avec succès !")
  }

  def historiquePaiements(): Unit = {
    println("\n--- HISTORIQUE DES PAIEMENTS ---")
    println("12/07/2025 - 15€ → Marie (Trajet Paris-Lyon)")
    println("08/07/2025 - 12€ ← Pierre (Trajet Nice-Cannes)")
    println("03/07/2025 - 20€ → Sophie (Trajet Marseille-Toulouse)")
    attendre()
  }

  // ===== FONCTIONS DE NOTATION =====

  def noterUtilisateur(): Unit = {
    println("\n--- NOTER UN UTILISATEUR ---")
    print("Nom de l'utilisateur à noter : ")
    val utilisateur = StdIn.readLine()
    print("Note (1-5 étoiles) : ")
    val note = StdIn.readLine()
    print("Commentaire (optionnel) : ")
    val commentaire = StdIn.readLine()

    println(s"Note de $note/5 attribuée à $utilisateur !")
  }

  def voirMesNotes(): Unit = {
    println("\n--- MES NOTES ---")
    println("Note moyenne en tant que conducteur: 4.2/5 ⭐⭐⭐⭐")
    println("Note moyenne en tant que passager: 4.7/5 ⭐⭐⭐⭐⭐")
    println("\nDerniers commentaires :")
    println("- Marie: 'Très ponctuel et conduite sécurisée' (5/5)")
    println("- Pierre: 'Bon voyage, conversation agréable' (4/5)")
    attendre()
  }

  // ===== FONCTIONS DE MESSAGERIE =====

  def envoyerMessage(): Unit = {
    println("\n--- ENVOYER UN MESSAGE ---")
    print("Destinataire : ")
    val destinataire = StdIn.readLine()
    print("Sujet : ")
    val sujet = StdIn.readLine()
    print("Message : ")
    val message = StdIn.readLine()

    println(s"Message envoyé à $destinataire avec succès !")
  }

  def voirMessages(): Unit = {
    println("\n--- MES MESSAGES ---")
    println("MESSAGES REÇUS :")
    println("1. De Marie - 'Question sur le trajet Paris-Lyon' (Non lu)")
    println("2. De Pierre - 'Merci pour le voyage' (Lu)")
    println()
    println("MESSAGES ENVOYÉS :")
    println("1. À Sophie - 'Confirmation du rendez-vous'")
    println("2. À Jean - 'Demande d'information sur le trajet'")

    print("\nLire le message numéro (0 pour retour) : ")
    val numero = StdIn.readLine()

    if (numero != "0") {
      println(s"\n--- MESSAGE $numero ---")
      println("De: Marie")
      println("Sujet: Question sur le trajet Paris-Lyon")
      println("Date: 23/07/2025 14:30")
      println("Message: Bonjour, pouvez-vous me confirmer le point de rendez-vous exact ?")
      attendre()
    }
  }

  // ===== FONCTION UTILITAIRE =====

  def attendre(): Unit = {
    print("\nAppuyez sur Entrée pour continuer...")
    StdIn.readLine()
  }
}
