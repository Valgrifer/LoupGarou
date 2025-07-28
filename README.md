


## Table des matières

- [À propos](#à-propos)
- [Installation](#installation)
  - [Dépendances requises](#dépendances-requises)
  - [Installation classique](#installation-classique)
  - [Installation avec docker (Alternative)](#installation-avec-docker-alternative)
- [Commandes](#commandes)
- [Todo](#todo)
- [Crédits](#crédits-du-projet-dorigine)
- [Aide](#aide)
  - [Questions fréquentes](#questions-fréquentes)
- [Indications pour les développeurs](#indications-pour-les-développeurs)
  - [Ajouter des rôles](#ajouter-des-rôles)
 	 - [Quelques classes utiles](#quelques-classes-utiles)
  - [Publier un rôle](#publier-un-rôle)
- [License](#license)

## À propos

Le mode Loup-Garou est un mode inspiré du jeu de société [Les Loups-Garous de Thiercelieux](https://fr.wikipedia.org/wiki/Les_Loups-garous_de_Thiercelieux) reprenant son fonctionnement ainsi que sa manière d'être joué, à la seule différence qu'aucun maître du jeu n'est requis, le déroulement de chaque partie étant entièrement automatisé :

- Déroulement de la partie automatisé
- Rôles du jeu de base, et nouveaux rôles
- Utilisable sur n'importe quelle map


## Installation

### Dépendances requises
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

### Installation classique
**Minecraft 1.16.5 est requis.**  
Pour installer le plug-in, merci de suivre les étapes suivantes:
  - Téléchargez Spigot 1.16.5 et lancez une fois le serveur
  - Dans le dossier `plugins`, insérez [ProtocolLib (4.6.0 minimum)](https://www.spigotmc.org/resources/protocollib.1997/) et [LoupGarou.jar](https://github.com/Valgrifer/LoupGarou/releases)
  - Redémarrez votre serveur puis donnez vous les permissions administrateur (/op <votre_pseudo> dans la console)
  - Allez sur la map et ajoutez les points de spawn sur chaque dalle `/lg addSpawn`
  - Connectez-vous au serveur et choisissez les rôles à utiliser avec `/lg`
	  - ⚠️ Il faut qu'il y ait autant de places dans les rôles que de joueur pour lancer une partie
  - Vous pouvez démarrer la partie avec `/lg start` 

Lien des releases : [Cliquez ici](https://github.com/Valgrifer/LoupGarou/releases)


### Installation avec docker (Alternative)

Vous devez avoir installé `docker` et `docker-compose` sur votre machine

#### Installation du serveur
```sh
docker-compose up -d --build
```

#### Démarage du serveur

Vous devez exécuter la commande suivante à chaque redémarage de votre machine avant de pouvoir continuer

```sh
docker-compose up -d
```

Ainsi, vous pouvez lancer le serveur en utilisant la commande suivante :

```sh
docker-compose exec loup-garou java -jar spigot.jar
```

Les fichiers relatifs à minecraft se situeront dans le dossier `minecraft_data` 
> **Tip :** Il faut accepter les conditions d'utilisations de Mojang en modifiant le fichier `minecraft_data/eula.txt`

## Commandes

`/lg roles` : Retourne la liste des rôles dans la partie  
`/lg roles set <ID> <MONTANT>` : Définit le nombre de joueurs pour un certain rôle  
`/lg addSpawn` : Ajoute un point de spawn (emplacement de joueur)  
`/lg start` : Lance la partie  
`/lg end` : Arrête une partie  
`/lg reloadConfig` : Recharge la configuration  
`/lg joinAll` : À utiliser après avoir changé les rôles  

## Todo

- [x] Add Identifier for LGChat add a Event / Ajouter un authentifier pour LGChat et lui ajouter un event
- [x] Change to a Better system Item Card / Changement pour un meilleure system item de carte
- [x] Add a resource pack generator / Ajouter un géneréteur de Resource Pack
- [x] Add RoleActionEvent with Identifier / Ajouter un event RoleAction avec un authentifier
- [ ] Add A Option System / Ajouter un system d'option
- [ ] Change SpawnPoint System / Changer le system de SpawnPoint
- [ ] Add A Map selection System / Ajouter un system de selection de map
- [ ] Add A Composition System / Ajouter un system de compo de role
- [ ] Add Language selection System / Ajouter un system de Langue
- [ ] Add a wiki / Ajouter un Wiki


## Crédits du projet d'origine

- Chef de Projet : [Shytoos](https://twitter.com/Shytoos)
- Développement : [Leomelki](https://twitter.com/leomelki)
- Mapping : [Cosii](https://www.youtube.com/channel/UCwyOcA41QSk590fl9L0ys8A)
- 
## Crédits du projet maintenant

- Développement : [Valgrifer](https://github.com/Valgrifer)

## Aide

En cas de soucis, envoyé vos problèmes/erreurs sur Github, j'essayerais de les resoudres.

### Si toujours opérationnelle

Par soucis de temps, nous ne pouvons pas faire de support au cas par cas, mais vous pouvez rejoindre notre serveur [Discord](https://discord.gg/Squeezie) pour trouver de l'aide auprès de la communauté.

### Questions fréquentes

- Que faire en cas de problème d'affichage (votes bloqués aux dessus des têtes, etc...) ?  

Cela arrive après avoir `reload` au cours d'une partie, tous les joueurs qui ont ce problème doivent se déconnecter et se reconnecter.

- J'ai mal placés mes spawns ou je veux utiliser une nouvelle map, comment faire ?  

Il suffit d'ouvrir le fichier `plugins\LoupGarou\config.yml` et de supprimer les points de spawn.

- Puis-je mettre plusieurs fois le même rôle dans une seule partie ?

Cela est possible pour les rôles `Loup-Garou`, `Villageois` et `Chasseur`.
D'autres rôles peuvent aussi marcher mais n'ont pas été testés avec plusieurs joueurs ayant ce rôle dans une seule partie. C'est à vos risques et périls.


## Indications pour les développeurs

Ce plugin LoupGarou ayant été modifié de nombreuses fois, parfois dans des timings tendus, le code n'est pas très propre. Aussi, il n'est pas documenté.  

Vous devez utiliser `Lombok` et `Maven` pour modifier ce projet. 
Vous devez aussi installer la repository `Spigot` avec [BuildTools](https://www.spigotmc.org/wiki/buildtools/).

**Cependant, si l'envie vous prend de modifier ou d'utiliser le code ici présent en partie, ou dans sa totalité, merci de créditer [Leomelki](https://twitter.com/leomelki) et [Shytoos](https://twitter.com/shytoos_).
Une utilisation commerciale est cependant interdite. Merci de vous référer aux informations de [license](#license)**

**Il n'est plus nécessaire de modifier le code pour ajouter des rôles, il suffit de créer un second plugin comme addon à celui la!**

### Ajouter des rôles

Ce plugin de Loup-Garou est organisé autour d'un système d'événements, disponibles dans le package `fr.leomelki.loupgarou.events`.  
N'ayant pas le temps de les documenter, vous devriez comprendre vous-même quand ils sont appelés.

Pour vous aider à créer des rôles, copiez des rôles ayant déjà été créés pour ainsi les modifier.

⚠️ Ce projet a été créé de façon à ce que les rôles soient (presque) totalement indépendants du reste du code (LGGame, LGPlayer...).  
Merci de garder cela en tête lors du développement de nouveaux rôles : utilisez un maximum les évènements et, s'il en manque, créez-les !

#### Quelques classes utiles
`LGGame` : Contient le coeur du jeu, à modifier le minimum possible !  
`LGSpot` : Outils des emplacements des joueurs relative aux d'autres.  
`LGPlayer` : Classe utilisée pour intéragir avec les joueurs et stocker leurs données, à modifier le minimum possible !  
`LGVote` : Système gérant les votes.  
`RoleSort`: Classement de l'apparition des rôles durant la nuit.   
`ResourcePack`: Outils relatif au ResourcePack custom. 

### Publier un rôle

Si vous arrivez à créer un rôle, je vous invite à faire une demande de publication dans cette repo afin de les faire partager à l'ensemble de la communauté !

# License
[![License Logo (CC BY-NC)](https://licensebuttons.net/l/by-nc/3.0/88x31.png)](https://creativecommons.org/licenses/by-nc/4.0/legalcode.fr)

Creative Commons BY-NC (https://creativecommons.org/licenses/by-nc/4.0/)

Informations légales : https://creativecommons.org/licenses/by-nc/4.0/legalcode
