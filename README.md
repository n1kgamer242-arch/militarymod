# Military Mod (Forge 1.20.1 - 47.4.0)

## Build
1. Scarica l'MDK ufficiale Forge 1.20.1-47.4.0 (files.minecraftforge.net) per avere gradlew/wrapper.
2. Copia dentro la cartella `src/` di questo progetto (e sovrascrivi build.gradle se vuoi usare il mio).
3. `./gradlew build`  ->  jar in `build/libs/`.  Test: `./gradlew runClient`.
Java 17 richiesto.

## Gameplay
- Fucile d'assalto + Munizioni (si craftano), Radio di comando.
- Reclutare: tasto destro su un soldato alleato "libero" con 3 smeraldi in mano.
- Tasto destro (mano vuota) su un tuo soldato: alterna SEGUI / GUARDIA.
- Radio: click su un blocco = i soldati vanno li' e fanno guardia; Shift+click = ti seguono.
- Ondata nemica ogni 5 giorni al tramonto (WaveManager.DAYS_BETWEEN_WAVES).
- I nemici attaccano giocatore, villager, golem e i tuoi soldati.

## Strutture (data/militarymod/structures/*.nbt)
Generate con tools/gen.py (python3 tools/gen.py, riscrive tutto). Puoi anche aprirle in gioco
con i Structure Block e modificarle a mano.
