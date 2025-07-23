-- TABLES PRINCIPALES

-- Table des utilisateurs
CREATE TABLE utilisateurs (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    telephone VARCHAR(20),
    est_conducteur BOOLEAN DEFAULT FALSE,
    ville VARCHAR(100),
    code_postal VARCHAR(10),
    note_moyenne DECIMAL(3,2) DEFAULT 0.00,
    nombre_evaluations INTEGER DEFAULT 0,
    statut VARCHAR(20) CHECK (statut IN ('actif', 'suspendu', 'supprime')) DEFAULT 'actif'
);

-- Création d'un type énuméré pour le statut des véhicules
CREATE TYPE statut_vehicule AS ENUM ('actif', 'inactif');

-- Table des véhicules
CREATE TABLE vehicules (
    id SERIAL PRIMARY KEY,
    immatriculation VARCHAR(20) UNIQUE NOT NULL,
    marque VARCHAR(50) NOT NULL,
    modele VARCHAR(50) NOT NULL,
    nombre_places INTEGER NOT NULL DEFAULT 4,
    annee INTEGER,
    statut statut_vehicule DEFAULT 'actif'
);

-- Création des types énumérés nécessaires
CREATE TYPE statut_trajet AS ENUM ('prevu', 'en_cours', 'termine', 'annule');
CREATE TYPE statut_reservation AS ENUM ('en_attente', 'confirmee', 'annulee', 'terminee');
CREATE TYPE statut_paiement AS ENUM ('en_attente', 'complete', 'echue', 'rembourse');
CREATE TYPE type_evaluation AS ENUM ('conducteur', 'passager');
CREATE TYPE type_message AS ENUM ('general', 'trajet', 'reservation', 'paiement');
CREATE TYPE statut_message AS ENUM ('envoye', 'lu', 'archive', 'supprime');

-- Table des trajets
CREATE TABLE trajets (
    id SERIAL PRIMARY KEY,
    ville_depart VARCHAR(100) NOT NULL,
    ville_arrivee VARCHAR(100) NOT NULL,
    date_depart DATE NOT NULL,
    heure_depart TIME NOT NULL,
    prix_par_place DECIMAL(8,2) NOT NULL,
    places_totales INTEGER NOT NULL,
    code_trajet VARCHAR(50) UNIQUE,
    statut VARCHAR(100) NOT NULL DEFAULT 'prevu'
);

-- Table des réservations
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    numero_reservation VARCHAR(50) UNIQUE NOT NULL,
    nombre_places INTEGER DEFAULT 1,
    prix_total DECIMAL(8,2) NOT NULL,
    statut VARCHAR(50) DEFAULT 'en_attente',
    message_passager TEXT,
    date_reservation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_confirmation TIMESTAMP,
    date_annulation TIMESTAMP,
    motif_annulation TEXT
);

-- Table des paiements
CREATE TABLE paiements (
    id SERIAL PRIMARY KEY,
    numero_transaction VARCHAR(50) UNIQUE NOT NULL,
    montant DECIMAL(8,2) NOT NULL,
    statut VARCHAR(50) DEFAULT 'en_attente',
    date_paiement TIMESTAMP
);

-- Table des évaluations
CREATE TABLE evaluations (
    id SERIAL PRIMARY KEY,
    note INTEGER NOT NULL CHECK (note >= 1 AND note <= 5),
    commentaire TEXT,
    type_eval VARCHAR(50) NOT NULL
);

-- Table des messages
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    numero_message VARCHAR(50) UNIQUE NOT NULL,
    contenu TEXT NOT NULL,
    lu BOOLEAN DEFAULT FALSE,
    date_lecture TIMESTAMP,
    type_msg VARCHAR(50) UNIQUE NOT NULL DEFAULT 'general',
    statut_msg VARCHAR(50) UNIQUE NOT NULL DEFAULT 'envoye'
);

-- TABLES DE LIAISON (Relations)

-- Types pour les relations
CREATE TYPE type_relation_vehicule AS ENUM ('proprietaire', 'utilisateur_autorise');

-- Liaison utilisateur-véhicule (propriété)
CREATE TABLE utilisateur_vehicule (
    id SERIAL PRIMARY KEY,
    utilisateur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    vehicule_id INTEGER NOT NULL REFERENCES vehicules(id) ON DELETE CASCADE,
    type_relation type_relation_vehicule DEFAULT 'proprietaire',
    date_debut DATE DEFAULT CURRENT_DATE,
    date_fin DATE,
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (utilisateur_id, vehicule_id)
);

-- Liaison utilisateur-trajet (conducteur)
CREATE TABLE utilisateur_trajet (
    id SERIAL PRIMARY KEY,
    utilisateur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    trajet_id INTEGER NOT NULL REFERENCES trajets(id) ON DELETE CASCADE,
    vehicule_id INTEGER NOT NULL REFERENCES vehicules(id) ON DELETE CASCADE,
    role VARCHAR(20) DEFAULT 'conducteur' CHECK (role IN ('conducteur')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (utilisateur_id, trajet_id)
);

-- Liaison trajet-réservation-utilisateur (passager)
CREATE TABLE trajet_reservation (
    id SERIAL PRIMARY KEY,
    trajet_id INTEGER NOT NULL REFERENCES trajets(id) ON DELETE CASCADE,
    reservation_id INTEGER NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    passager_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    conducteur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (trajet_id, reservation_id, passager_id)
);

-- Liaison réservation-paiement
CREATE TABLE reservation_paiement (
    id SERIAL PRIMARY KEY,
    reservation_id INTEGER NOT NULL REFERENCES reservations(id) ON DELETE CASCADE,
    paiement_id INTEGER NOT NULL REFERENCES paiements(id) ON DELETE CASCADE,
    payeur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    beneficiaire_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (reservation_id, paiement_id)
);

-- Liaison évaluation-utilisateurs-trajet
CREATE TABLE evaluation_relation (
    id SERIAL PRIMARY KEY,
    evaluation_id INTEGER NOT NULL REFERENCES evaluations(id) ON DELETE CASCADE,
    trajet_id INTEGER NOT NULL REFERENCES trajets(id) ON DELETE CASCADE,
    evaluateur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    evalue_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (evaluation_id, trajet_id, evaluateur_id, evalue_id)
);

-- Liaison messages entre utilisateurs
CREATE TABLE message_relation (
    id SERIAL PRIMARY KEY,
    message_id INTEGER NOT NULL REFERENCES messages(id) ON DELETE CASCADE,
    expediteur_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    destinataire_id INTEGER NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    trajet_id INTEGER REFERENCES trajets(id) ON DELETE SET NULL,
    reservation_id INTEGER REFERENCES reservations(id) ON DELETE SET NULL,
    message_parent_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (message_id, expediteur_id, destinataire_id)
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

-- FONCTIONS ET TRIGGERS

-- Fonction pour générer un code de trajet
CREATE OR REPLACE FUNCTION generate_trajet_code()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.code_trajet IS NULL OR NEW.code_trajet = '' THEN
        NEW.code_trajet := 'TRJ_' || TO_CHAR(NEW.date_depart, 'YYYYMMDD') || '_' || 
                          TO_CHAR(NEW.heure_depart, 'HH24MI') || '_' || 
                          FLOOR(EXTRACT(EPOCH FROM NOW()));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger pour le code trajet
CREATE TRIGGER trg_generate_trajet_code
BEFORE INSERT ON trajets
FOR EACH ROW
EXECUTE FUNCTION generate_trajet_code();

-- Fonction pour générer un numéro de réservation
CREATE OR REPLACE FUNCTION generate_reservation_numero()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_reservation IS NULL OR NEW.numero_reservation = '' THEN
        NEW.numero_reservation := 'RES_' || TO_CHAR(NOW(), 'YYYYMMDD') || '_' || 
                                FLOOR(EXTRACT(EPOCH FROM NOW()));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger pour le numéro de réservation
CREATE TRIGGER trg_generate_reservation_numero
BEFORE INSERT ON reservations
FOR EACH ROW
EXECUTE FUNCTION generate_reservation_numero();

-- Fonction pour générer un numéro de transaction
CREATE OR REPLACE FUNCTION generate_transaction_numero()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.numero_transaction IS NULL OR NEW.numero_transaction = '' THEN
        NEW.numero_transaction := 'PAY_' || TO_CHAR(NOW(), 'YYYYMMDD') || '_' || 
                                FLOOR(EXTRACT(EPOCH FROM NOW()));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger pour le numéro de transaction
CREATE TRIGGER trg_generate_transaction_numero
BEFORE INSERT ON paiements
FOR EACH ROW
EXECUTE FUNCTION generate_transaction_numero();
