# Paint (Java Swing)

Application de dessin type Paint réalisée en Java GUI (Swing).

## Fonctionnalités
- Outils : crayon, ligne, rectangle, ovale (plein ou non), gomme, pot de peinture
- Choix de la couleur (palette rapide + sélecteur) et de l'épaisseur
- Annuler (Ctrl+Z), effacer tout, nouveau, ouvrir une image, enregistrer en PNG

## Lancer
```
java -jar Paint.jar
```

## Compiler
```
javac -d out src/paint/*.java
printf "Main-Class: paint.Main\n" > manifest.txt
jar cfm Paint.jar manifest.txt -C out .
```
