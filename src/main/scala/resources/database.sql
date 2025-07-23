

-- TABLES PRINCIPALES

-- Table des utilisateurs
CREATE TABLE utilisateurs (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              email VARCHAR(255) UNIQUE NOT NULL,
                              mot_de_passe VARCHAR(255) NOT NULL,
                              nom VARCHAR(100) NOT NULL,
                              prenom VARCHAR(100) NOT NULL,
                              telephone VARCHAR(20),
                              est_conducteur BOOLEAN DEFAULT FALSE,
                              ville VARCHAR(100),
                              code_postal VARCHAR(10),
                              note_moyenne DECIMAL(3,2) DEFAULT 0.00,
                              nombre_evaluations INT DEFAULT 0,
                              statut ENUM('actif', 'suspendu', 'supprime') DEFAULT 'actif',
                              );

-- Table des véhicules
CREATE TABLE vehicules (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           immatriculation VARCHAR(20) UNIQUE NOT NULL,
                           marque VARCHAR(50) NOT NULL,
                           modele VARCHAR(50) NOT NULL,
                           nombre_places INT NOT NULL DEFAULT 4,
                           annee INT,
                           statut ENUM('actif', 'inactif') DEFAULT 'actif',
                          );

-- Table des trajets
CREATE TABLE trajets (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         code_trajet VARCHAR(50) UNIQUE NOT NULL,
                         ville_depart VARCHAR(100) NOT NULL,
                         adresse_depart TEXT,
                         ville_arrivee VARCHAR(100) NOT NULL,
                         adresse_arrivee TEXT,
                         date_depart DATE NOT NULL,
                         heure_depart TIME NOT NULL,
                         prix_par_place DECIMAL(8,2) NOT NULL,
                         places_disponibles INT NOT NULL,
                         places_totales INT NOT NULL,
                         distance_km INT,
                         duree_estimee TIME,
                         description TEXT,
                         statut ENUM('prevu', 'en_cours', 'termine', 'annule') DEFAULT 'prevu',
);

-- Table des réservations
CREATE TABLE reservations (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              numero_reservation VARCHAR(50) UNIQUE NOT NULL,
                              nombre_places INT DEFAULT 1,
                              prix_total DECIMAL(8,2) NOT NULL,
                              statut ENUM('en_attente', 'confirmee', 'annulee', 'terminee') DEFAULT 'en_attente',
                              message_passager TEXT,
                              date_reservation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              date_confirmation TIMESTAMP,
                              date_annulation TIMESTAMP,
                              motif_annulation TEXT,
);

-- Table des paiements
CREATE TABLE paiements (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           numero_transaction VARCHAR(50) UNIQUE NOT NULL,
                           montant DECIMAL(8,2) NOT NULL,
                           statut ENUM('en_attente', 'complete', 'echue', 'rembourse') DEFAULT 'en_attente',
                           date_paiement TIMESTAMP
                       );

-- Table des évaluations
CREATE TABLE evaluations (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             code_evaluation VARCHAR(50) UNIQUE NOT NULL,
                             note INT NOT NULL CHECK (note >= 1 AND note <= 5),
                             commentaire TEXT,
                             type_evaluation ENUM('conducteur', 'passager') NOT NULL,
                             date_evaluation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
);

-- Table des messages
CREATE TABLE messages (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          numero_message VARCHAR(50) UNIQUE NOT NULL,
                          contenu TEXT NOT NULL,
                          lu BOOLEAN DEFAULT FALSE,
                          date_lecture TIMESTAMP,
                          type_message ENUM('general', 'trajet', 'reservation', 'paiement') DEFAULT 'general',
                          statut ENUM('envoye', 'lu', 'archive', 'supprime') DEFAULT 'envoye',
);

-- TABLES DE LIAISON (Relations)


-- Liaison utilisateur-véhicule (propriété)
CREATE TABLE utilisateur_vehicule (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      utilisateur_id INT NOT NULL,
                                      vehicule_id INT NOT NULL,
                                      type_relation ENUM('proprietaire', 'utilisateur_autorise') DEFAULT 'proprietaire',
                                      date_debut DATE DEFAULT (CURRENT_DATE),
                                      date_fin DATE,
                                      actif BOOLEAN DEFAULT TRUE,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Liaison utilisateur-trajet (conducteur)
CREATE TABLE utilisateur_trajet (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    utilisateur_id INT NOT NULL,
                                    trajet_id INT NOT NULL,
                                    vehicule_id INT NOT NULL,
                                    role ENUM('conducteur') DEFAULT 'conducteur',
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Liaison trajet-réservation-utilisateur (passager)
CREATE TABLE trajet_reservation (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    trajet_id INT NOT NULL,
                                    reservation_id INT NOT NULL,
                                    passager_id INT NOT NULL,
                                    conducteur_id INT NOT NULL,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Liaison réservation-paiement
CREATE TABLE reservation_paiement (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      reservation_id INT NOT NULL,
                                      paiement_id INT NOT NULL,
                                      payeur_id INT NOT NULL,
                                      beneficiaire_id INT NOT NULL,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Liaison évaluation-utilisateurs-trajet
CREATE TABLE evaluation_relation (
                                     id INT PRIMARY KEY AUTO_INCREMENT,
                                     evaluation_id INT NOT NULL,
                                     trajet_id INT NOT NULL,
                                     evaluateur_id INT NOT NULL,
                                     evalue_id INT NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Liaison messages entre utilisateurs
CREATE TABLE message_relation (
                                  id INT PRIMARY KEY AUTO_INCREMENT,
                                  message_id INT NOT NULL,
                                  expediteur_id INT NOT NULL,
                                  destinataire_id INT NOT NULL,
                                  trajet_id INT,
                                  reservation_id INT,
                                  message_parent_id INT,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Liaison utilisateurs favoris
CREATE TABLE utilisateur_favori (
                                    id INT PRIMARY KEY AUTO_INCREMENT,
                                    utilisateur_id INT NOT NULL,
                                    favori_id INT NOT NULL,
                                    type_favori ENUM('conducteur', 'passager', 'general') DEFAULT 'general',
                                    note_personnelle TEXT,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- INDEX POUR PERFORMANCES

-- Index sur les tables principales
CREATE INDEX idx_utilisateurs_email ON utilisateurs(email);
CREATE INDEX idx_utilisateurs_statut ON utilisateurs(statut);
CREATE INDEX idx_vehicules_immatriculation ON vehicules(immatriculation);
CREATE INDEX idx_trajets_code ON trajets(code_trajet);
CREATE INDEX idx_trajets_depart_date ON trajets(ville_depart, date_depart);
CREATE INDEX idx_trajets_arrivee_date ON trajets(ville_arrivee, date_depart);
CREATE INDEX idx_trajets_statut ON trajets(statut);
CREATE INDEX idx_reservations_numero ON reservations(numero_reservation);
CREATE INDEX idx_reservations_statut ON reservations(statut);
CREATE INDEX idx_paiements_numero ON paiements(numero_transaction);
CREATE INDEX idx_paiements_statut ON paiements(statut);
CREATE INDEX idx_messages_numero ON messages(numero_message);
CREATE INDEX idx_messages_lu ON messages(lu);

-- Index sur les tables de liaison
CREATE INDEX idx_utilisateur_vehicule_user ON utilisateur_vehicule(utilisateur_id);
CREATE INDEX idx_utilisateur_vehicule_vehicule ON utilisateur_vehicule(vehicule_id);
CREATE INDEX idx_utilisateur_trajet_user ON utilisateur_trajet(utilisateur_id);
CREATE INDEX idx_utilisateur_trajet_trajet ON utilisateur_trajet(trajet_id);
CREATE INDEX idx_trajet_reservation_trajet ON trajet_reservation(trajet_id);
CREATE INDEX idx_trajet_reservation_passager ON trajet_reservation(passager_id);
CREATE INDEX idx_reservation_paiement_reservation ON reservation_paiement(reservation_id);
CREATE INDEX idx_evaluation_relation_evaluateur ON evaluation_relation(evaluateur_id);
CREATE INDEX idx_evaluation_relation_evalue ON evaluation_relation(evalue_id);
CREATE INDEX idx_message_relation_expediteur ON message_relation(expediteur_id);
CREATE INDEX idx_message_relation_destinataire ON message_relation(destinataire_id);

-- Index composites pour requêtes fréquentes
CREATE INDEX idx_trajet_reservation_composite ON trajet_reservation(trajet_id, passager_id);
CREATE INDEX idx_utilisateur_vehicule_composite ON utilisateur_vehicule(utilisateur_id, vehicule_id);
CREATE INDEX idx_message_relation_conversation ON message_relation(expediteur_id, destinataire_id);
CREATE INDEX idx_evaluation_relation_composite ON evaluation_relation(trajet_id, evaluateur_id, evalue_id);

-- TRIGGERS OPTIONNELS (si souhaités)

DELIMITER //

-- Trigger pour générer automatiquement le code trajet
CREATE TRIGGER generate_trajet_code
    BEFORE INSERT ON trajets
    FOR EACH ROW
BEGIN
    IF NEW.code_trajet IS NULL OR NEW.code_trajet = '' THEN
        SET NEW.code_trajet = CONCAT(
            'TRJ_',
            DATE_FORMAT(NEW.date_depart, '%Y%m%d'),
            '_',
            TIME_FORMAT(NEW.heure_depart, '%H%i'),
            '_',
            UNIX_TIMESTAMP()
        );
END IF;
END//

-- Trigger pour générer automatiquement le numéro de réservation
CREATE TRIGGER generate_reservation_numero
    BEFORE INSERT ON reservations
    FOR EACH ROW
BEGIN
    IF NEW.numero_reservation IS NULL OR NEW.numero_reservation = '' THEN
        SET NEW.numero_reservation = CONCAT(
            'RES_',
            DATE_FORMAT(NOW(), '%Y%m%d'),
            '_',
            UNIX_TIMESTAMP()
        );
END IF;
END//

-- Trigger pour générer automatiquement le numéro de transaction
CREATE TRIGGER generate_transaction_numero
    BEFORE INSERT ON paiements
    FOR EACH ROW
BEGIN
    IF NEW.numero_transaction IS NULL OR NEW.numero_transaction = '' THEN
        SET NEW.numero_transaction = CONCAT(
            'PAY_',
            DATE_FORMAT(NOW(), '%Y%m%d'),
            '_',
            UNIX_TIMESTAMP()
        );
END IF;
END//

DELIMITER ;
