# Supernatural: The Hunt (unofficial fan mod)

A Fabric mod for **Minecraft Java 1.21.1** inspired by the TV series *Supernatural*.
Hunt ghosts, wendigos, shapeshifters, vampires and demons with the tools the Winchesters use.
The **Season 1 story mode** has 23 chapters, one for each episode, with the two-part finale split into two chapters.

> Fan project. Not affiliated with or endorsed by the show's creators, The CW or Warner Bros.
> The story text is original writing that retells the episodes; it doesn't quote the show.

---

## 1. Build the mod jar

You need **Java 21** (for example Temurin 21 from adoptium.net).

```bash
cd supernatural-the-hunt
./gradlew build          # Windows: gradlew.bat build
```

The jar shows up in `build/libs/supernatural-the-hunt-0.1.0.jar`. The first build downloads Minecraft and the Fabric tools, so it takes a few minutes.

**No Java set up? Let GitHub build it:** push this folder to a new GitHub repository. The included workflow (`.github/workflows/build.yml`) builds the jar automatically; download it from the run's **Artifacts** on the repository's **Actions** tab.

If the build complains about a version, open `gradle.properties` and update the numbers from https://fabricmc.net/develop (choose 1.21.1).

## 2. Install

1. Install **Fabric Loader** for 1.21.1 (https://fabricmc.net/use/installer/).
2. Put the mod jar **and** **Fabric API** for 1.21.1 (https://modrinth.com/mod/fabric-api) in your `.minecraft/mods` folder.
3. Start Minecraft with the Fabric profile.

## 3. Play story mode

- When you first join a world you get a **Hunter's Journal**. Right-click it, or type `/hunt`.
- Click **▶ Start this hunt** (or `/hunt start`). On your first hunt Dean hands you a starter kit.
- The story plays out in chat. Then the monsters arrive, and graves or cursed objects appear nearby.
- Finish the objective to get the outro and a reward. Each reward prepares you for the next episode.
- Start hunts **outdoors in the Overworld**. Night is scarier.

| Command | What it does |
|---|---|
| `/hunt` | Open the journal |
| `/hunt start` | Start the current chapter |
| `/hunt hint` | Direction and distance to your objective; makes targets glow |
| `/hunt restart` | Respawn the current hunt if something got lost |
| `/hunt abandon` | Give up the current hunt |
| `/hunt chapters` | Season 1 progress |
| `/hunt lore` | Monster weaknesses (or sneak + right-click the journal) |
| `/hunt goto <n>` | Jump to chapter *n* (needs cheats/op) |

### Season 1 chapters
1 Pilot · 2 Wendigo · 3 Dead in the Water · 4 Phantom Traveler · 5 Bloody Mary · 6 Skin · 7 Hook Man · 8 Bugs ·
9 Home · 10 Asylum · 11 Scarecrow · 12 Faith · 13 Route 666 · 14 Nightmare · 15 The Benders · 16 Shadow ·
17 Hell House · 18 Something Wicked · 19 Provenance · 20 Dead Man's Blood · 21 Salvation ·
22 Devil's Trap (Part 1) · 23 Devil's Trap (Part 2, boss)

Objective types: **salt & burn** a grave · **break** a cursed object · **kill** targets · **exorcise** demons · **survive** waves · **boss**.

## 4. Monsters and how to beat them

| Monster | Weakness |
|---|---|
| Vengeful Spirit | Immune to weapons. Iron and salt disperse it; it can't cross salt lines. Story spirits die only when you **salt and burn** their remains or break their cursed object. |
| Wendigo | Barely hurt unless it's **on fire** (Flare Gun, flint and steel, fire charge). |
| Shapeshifter | **Silver Knife** does triple damage; everything else barely scratches it. |
| Demon | Weapons can't kill the host. **Devil's Trap + Rite of Exorcism**, or **Holy Water**. Salt lines stop it. |
| Vampire | Heals constantly. **Dead Man's Blood** stops the healing; only a **Machete or axe** finishes it. |
| Shtriga | Only **iron** hurts it: iron weapons and salt rounds. |
| Daeva | Invisible and tough in the dark, weak in the **light**. |
| Scarecrow (pagan god) | Can't be hurt while its sacred tree stands. Freezes in daylight. |
| Yellow-Eyed Demon (boss) | Throws you around. Won't drop below half health unless you shoot it with **the Colt**. |

Outside story mode, the monsters (except the boss) also spawn at night: wendigos in taigas, scarecrows in plains, shtrigas in forests, and the rest anywhere in the Overworld. Spawn eggs are in the creative tab **Supernatural: The Hunt**.

## 5. Gear and recipes

| Item | Recipe |
|---|---|
| Rock Salt ×4 | sugar + bone meal (shapeless). Place it to make a salt line; it's also shotgun ammo. |
| Rock Salt Shotgun | `III` / `GTP`: iron ×3, gunpowder, tripwire hook, planks |
| Flare Gun | `IO` / `G_`: iron, orange dye, gunpowder |
| EMF Meter | redstone torch over compass over iron ingot (vertical). Hold it to sense spirits and graves. |
| Hunter's Journal | book + leather + ink sac |
| Holy Water ×3 | 3 glass bottles + water bucket + rock salt + gold nugget |
| Rite of Exorcism | paper + ink sac + rock salt + gold nugget |
| Devil's Trap ×4 | 2 red dye + rock salt |
| Silver Knife | iron ingot over stick |
| Machete | iron, iron, stick (diagonal) |
| Dead Man's Blood | glass bottle + rotten flesh + red dye |
| Colt Round ×2 | diamond + gold ingot + gunpowder |
| The Colt | story reward only (chapter 20) |

## 6. Customizing

- **Story text:** `src/main/java/com/fanmods/snhunt/story/Season1.java`. Each chapter is one readable block, so you can add Season 2 the same way.
- **Textures:** all art is generated by `tools/make_textures.py` (`python3 tools/make_textures.py`). You can also swap any PNG in `src/main/resources/assets/snhunt/textures/` for your own.
- **Models, recipes, loot:** `tools/make_json.py`.
- **Monster stats and rules:** `src/main/java/com/fanmods/snhunt/entity/`.

The Winchesters appear in chat, not as in-game characters, and the monsters use original generic designs.
