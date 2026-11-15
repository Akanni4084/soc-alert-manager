# SOC Alert Manager

Application Java en console pour gérer des alertes de sécurité fictives :
brute force, malware et phishing.

Ce projet a été réalisé dans le cadre du cours de programmation orientée
objet en M1 à JUNIA ISEN Lille.

Auteur : Abdul Manan Omichessan.

## Objectif

L'application représente le suivi des alertes par une équipe SOC :
évaluation du risque, attribution à un analyste, investigation et clôture.

Les alertes proviennent de fichiers de démonstration. L'application
n'effectue pas de détection réelle sur un réseau ou un ordinateur.

## Fonctionnalités

- Chargement initial des alertes et des analystes depuis des fichiers CSV.
- Calcul du risque selon le type d'alerte.
- Classement en quatre priorités : LOW, MEDIUM, HIGH et CRITICAL.
- Consultation d'une alerte par son identifiant.
- Attribution avec contrôle de la capacité de l'analyste.
- Démarrage d'une investigation.
- Résolution ou classement comme faux positif avec conclusion obligatoire.
- Historique daté des opérations réussies.
- Filtrage par statut ou priorité.
- Tableau de bord des alertes et de la charge des analystes.
- Sauvegarde à la fermeture normale et restauration au lancement suivant.

## Prérequis

- JDK 21.
- IntelliJ IDEA ou Maven.

## Lancer dans IntelliJ IDEA

1. Ouvrir le dossier contenant le fichier `pom.xml`.
2. Sélectionner un JDK 21 pour le projet.
3. Charger le projet Maven et attendre la résolution des dépendances.
4. Exécuter la méthode `main` de `com.abdulmanan.Main`.
5. Saisir les choix dans la console et valider avec Entrée.

Le répertoire de travail doit être la racine du projet.

## Compiler et lancer avec Maven

Depuis la racine du projet :

```bash
mvn clean verify
java -cp target/classes com.abdulmanan.Main
```

La commande `verify` compile le projet, exécute les tests et produit
le fichier JAR dans `target`.

L'application ne nécessite pas de dépendance externe à l'exécution.

## Utilisation

Exemple de traitement :

1. Choisir `1` pour afficher les alertes.
2. Choisir `4`, puis saisir `ALT-001` et `ANA-001`.
3. Choisir `5`, puis saisir `ALT-001` pour démarrer l'analyse.
4. Choisir `6`, saisir `ALT-001`, puis renseigner une conclusion.
5. Choisir `8`, puis `ALT-001` pour consulter l'historique.
6. Choisir `11` pour consulter le tableau de bord.
7. Choisir `0` pour quitter et sauvegarder.

Une alerte déjà clôturée ne peut plus être traitée.

## Règles métier

Le cycle de traitement est :

- NEW vers ASSIGNED : attribution à un analyste.
- ASSIGNED vers IN_PROGRESS : démarrage de l'analyse.
- IN_PROGRESS vers RESOLVED ou FALSE_POSITIVE : clôture avec conclusion.

Une alerte ne peut être attribuée qu'une seule fois.
Les alertes ASSIGNED et IN_PROGRESS comptent dans la charge de l'analyste.
Une clôture libère une place.

Les opérations refusées ne modifient pas le statut ni l'historique.

### Priorités

| Score | Priorité |
|---|---|
| 0 à 29 | LOW |
| 30 à 59 | MEDIUM |
| 60 à 79 | HIGH |
| 80 à 100 | CRITICAL |

### Calcul des scores

Ces règles sont des choix pédagogiques propres au projet.

**Brute force :**

- 1 à 9 échecs : 20 points.
- 10 à 49 échecs : 40 points.
- À partir de 50 échecs : 60 points.
- Machine sensible : +20 points.
- Connexion réussie après les échecs : +20 points.

**Malware :**

- Détection : 40 points.
- Machine sensible : +20 points.
- Malware exécuté : +40 points.

**Phishing :**

- Message suspect : 20 points.
- Machine sensible : +20 points.
- Lien ouvert : +20 points.
- Identifiants saisis : +40 points.

Dans le scénario de phishing par lien utilisé ici, la saisie
d'identifiants nécessite l'ouverture préalable du lien.

## Organisation et classes principales

| Package ou classe | Responsabilité |
|---|---|
| `model` | Objets métier et règles propres aux alertes |
| `Alert` | Données communes, transitions et historique |
| `BruteForceAlert` | Informations et score de brute force |
| `MalwareAlert` | Informations et score de malware |
| `PhishingAlert` | Informations et score de phishing |
| `Analyst` | Identité et capacité maximale d'un analyste |
| `AlertHistoryEntry` | Une action datée |
| `AlertService` | Recherche, attribution, capacité et filtres |
| `repository` | Chargement CSV et persistance |
| `ApplicationState` | Données regroupées pour la sauvegarde |
| `ConsoleMenu` | Saisie et affichage dans la console |
| `Main` | Initialisation et assemblage de l'application |
| `exception` | Exceptions métier personnalisées |

## Programmation orientée objet

### Encapsulation

Les attributs sont privés. Les transitions passent par des méthodes
explicites comme `assignTo`, `startInvestigation` et `resolve`.
Il n'existe pas de modification libre du statut.

L'historique est accessible sous forme d'une copie non modifiable.

### Héritage et classe abstraite

`BruteForceAlert`, `MalwareAlert` et `PhishingAlert` héritent de la classe
abstraite `Alert`.

Cette relation correspond au domaine : chacun de ces objets est une alerte.
La classe parent regroupe les données et comportements communs.

### Polymorphisme

Les différentes alertes sont manipulées à travers le type `Alert`.

L'appel à `calculateRiskScore()` utilise le calcul du type réel de l'objet.
`getPriority()` transforme ensuite ce score en priorité.

### Interfaces

`AlertLoader`, `AnalystLoader` et `StateRepository` définissent les contrats
de chargement et de persistance.

`Main` utilise ces contrats et sélectionne les implémentations au démarrage.

## Gestion des erreurs

`InvalidAlertOperationException` est l'exception personnalisée utilisée
pour les transitions interdites et le dépassement de capacité.

Les autres exceptions utilisées comprennent :

- `IllegalArgumentException` pour les valeurs invalides ou les doublons.
- `NoSuchElementException` pour les identifiants introuvables.
- `IOException` pour les erreurs de lecture et d'écriture.

Le menu intercepte les erreurs d'utilisation pour permettre de poursuivre
la session. Les erreurs de chargement empêchent le démarrage avec des
données incomplètes.

## Chargement des données

Au premier lancement, l'application lit :

- `src/main/resources/alerts.csv`
- `src/main/resources/analysts.csv`

Les fichiers utilisent UTF-8 et le point-virgule comme séparateur.
Leur première ligne contient les noms des colonnes.

Le lecteur valide l'en-tête, le nombre de colonnes, les types des valeurs
et les identifiants en double. Une ligne invalide interrompt le chargement
avec son numéro.

Ce format simple ne prend pas en charge les champs contenant des
points-virgules ou des retours à la ligne.

### Colonnes des alertes

`type;id;description;affectedHost;detectedAt;criticalHost;detail;flag1;flag2`

| Type | detail | flag1 | flag2 |
|---|---|---|---|
| BRUTE_FORCE | Nombre d'échecs | Connexion réussie | Non utilisé, false |
| MALWARE | Nom du malware | Malware exécuté | Non utilisé, false |
| PHISHING | Expéditeur | Lien ouvert | Identifiants saisis |

Les booléens doivent être écrits `true` ou `false`.
Les dates suivent le format `2026-11-15T08:30:00`.

### Colonnes des analystes

`id;name;maxActiveAlerts`

La capacité maximale doit être strictement positive.

## Sauvegarde et restauration

À la sortie normale du menu, les objets sont enregistrés dans :

`data/soc-state.bin`

Au lancement suivant, cette sauvegarde est prioritaire sur les CSV.
Elle conserve les attributions, les conclusions et les dates de l'historique.

L'écriture passe par un fichier temporaire avant de remplacer la sauvegarde.

Pour repartir des CSV, arrêter l'application, puis déplacer ou supprimer
`data/soc-state.bin`. Cela réinitialise le travail enregistré.

La sauvegarde est locale et n'est pas versionnée dans Git.
Elle doit provenir de cette application : la désérialisation Java
ne doit pas être utilisée sur un fichier provenant d'une source inconnue.

Une modification de la structure des classes peut rendre une ancienne
sauvegarde incompatible. Le programme ne prévoit pas de migration.

Utiliser le choix `0` pour sauvegarder : une interruption forcée avec
le bouton Stop ne garantit pas l'enregistrement.

## Principes SOLID

- **Responsabilité unique :** les modèles, le service, le menu et la lecture
  des fichiers ont des responsabilités distinctes.
- **Ouvert/fermé :** un nouveau type d'alerte peut fournir son calcul sans
  modifier le calcul des types existants. Son chargement CSV nécessiterait
  néanmoins une extension du lecteur.
- **Substitution de Liskov :** les types d'alertes respectent le contrat
  commun de `Alert`, notamment un score compris entre 0 et 100.
- **Ségrégation des interfaces :** les contrats de chargement des alertes,
  des analystes et de sauvegarde sont séparés.
- **Inversion des dépendances :** les variables de chargement et de
  persistance utilisent des interfaces ; les implémentations concrètes
  sont choisies dans le point d'entrée.

Le contrôle de capacité est centralisé dans `AlertService`.
Les attributions effectuées par l'application passent par ce service.

## Choix techniques

1. **Java 21 et Maven :** une configuration de compilation commune et
   une gestion des dépendances de test.
2. **Console :** une interaction simple pour se concentrer sur la
   conception objet et les règles métier.
3. **Enums :** des statuts et priorités limités à des valeurs connues.
4. **LinkedHashMap :** une recherche par identifiant avec conservation
   de l'ordre d'ajout.
5. **CSV et sauvegarde binaire :** des données initiales lisibles,
   puis une conservation complète des objets entre les sessions.

## Tests

Exécuter tous les tests :

```bash
mvn test
```

Dans IntelliJ, faire un clic droit sur `src/test/java`, puis lancer
tous les tests.

La suite comporte 65 exécutions :

| Classe | Exécutions |
|---|---:|
| PriorityTest | 10 |
| AlertTest | 10 |
| AlertServiceTest | 11 |
| BinaryStateRepositoryTest | 5 |
| RiskScoreTest | 23 |
| CsvLoaderTest | 6 |
| Total | 65 |

Les tests couvrent les cas normaux, les seuils de score, les opérations
interdites, les capacités, les filtres, le chargement CSV et la persistance.

Les tests de sauvegarde utilisent des dossiers temporaires.
Les tests CSV valides vérifient les fichiers de démonstration fournis.

## Limites

- Application pédagogique utilisant des alertes fictives.
- Aucun branchement à un SIEM ou à un outil de détection.
- Sauvegarde lors de la fermeture normale.
- Pas de gestion des accès ni d'utilisation simultanée par plusieurs personnes.