# Powered night vision ownership

Status: implemented on `claude/nov10-a-nightvision`, on top of the PR609 powered-equipment port. Written with Claude Code, 10 November 2026. **Not compiled or run**: this environment cannot reach the Fabric or Mojang build hosts.
Proposal issue: none. Owner report of 7 November 2026, plus a Codex review of the first fix.
Owner: jimbozoomer-byte
Target milestone and tier: the existing electrical tier (exosuit helmets; industrial goggles, nano and quantum helmets).
Primary specialty and supported player role: a repair for miners and explorers; no new content.

## Player experience

In the dark, a worn, charged and enabled powered helmet gives steady night vision that never flashes.

- **Its own vision goes at once** when you take the helmet off, switch it off, it runs flat, you step into light, die or become a spectator.
- **Everyone else's vision is left alone.** This covers potions, food, beacons, the Jimtafacts accessory goggles, the glowing flower crown, the hood of darkness, gem and Neptunium helmets, pocket pets and candles.
  - The helmet never takes that vision away and never pays for it.
  - It gives its own vision again once the other has run out.
- **Two Jugcraft sources worn together don't fight.** For example, the accessory goggles with an exosuit helmet, or the industrial goggles with the rest of an exosuit.

## How it went wrong

1. **The 7 November report.** The Industrial Night Vision Goggles spent charge but gave no vision.
   - On `main`, the exosuit's helmet tick runs for every player. When it was not giving vision itself, it removed any ambient, particle-free night vision of 260 ticks or less.
   - That is exactly the goggles' effect. Both callbacks run at the end of each server tick, so each tick the goggles paid and the exosuit took the vision away.
   - The nano and quantum helmets, the accessory goggles, the flower crown and the hood of darkness were stripped the same way.
2. **The first fix** (branch `claude/nov7-p0-nightvision-goggles`, `6171f9d5bb`). The exosuit remembered the effect it gave, by object identity, and removed only that one.
3. **PR609** replaced both helmet paths with one owner, `PoweredNightVision`. It checks identity, the helmet's look, and a duration no longer than the helmet's own.
4. **The review.** Minecraft merges a new night vision into the player's existing instance in place (`MobEffectInstance.update`), so identity alone cannot prove ownership. PR609's look check covered potions and beacons, but two cases still got through:
   - **An alike effect.** Another source with exactly the helmet's flags, merged in with a duration still within the helmet's 260 or 400 ticks, was taken for the helmet's and removed.
   - **An endless effect** (duration -1) passed the "no longer than" check.

## Ownership rule

How vanilla merges a new night vision into the existing one, as reasoned from `MobEffectInstance.update`. The 26.3 jar was not available here to decompile. The tests below assert each premise they rely on.

| The new effect is | Duration | Ambient | Particles, icon |
| --- | --- | --- | --- |
| longer or endless, same level | becomes the new one's | becomes the new one's | become the new one's |
| shorter, same level | unchanged | cleared if the new one is not ambient | become the new one's |
| a higher level | becomes the new one's; a shorter one keeps the old as a hidden effect that returns in place when it ends | becomes the new one's | become the new one's |

The helmet owns the player's night vision only if **all** of these hold:
- it is the very instance the helmet last added;
- it still has the helmet's look: ambient, no particles, an icon, level I;
- its duration is finite;
- its duration has not risen since the helmet last checked it (every tick).

Otherwise the helmet forgets that instance for good. While any other vision is on, the helmet adds, refreshes and pays for nothing, so it never writes into another source's effect.

| Overlap | What happens |
| --- | --- |
| Potion or food, longer | The instance becomes the potion's. Kept, unpaid; the helmet resumes after it ends. |
| Potion or food, shorter | The helmet's longer duration stays, with the potion's look. Kept, unpaid; the helmet resumes after it ends. |
| Beacon, aura candle, incense (ambient, with particles) | The particle flag differs. Kept, unpaid. |
| Accessory goggles, crown, hood, botanical accessories (ambient, no icon) | The icon flag differs. The accessory keeps the vision; the helmet pays nothing. |
| Gem helmet, Neptunium helmet (not ambient, no icon) | Kept, unpaid. |
| Level II (pocket pets) | Kept, unpaid. |
| Endless | Kept, unpaid. |
| Alike flags, longer | The duration rises. Kept, unpaid. |

## Energy

- **Exosuit helmet:** 2 JE a tick, only for ticks it supplies its own vision. When it cannot pay, it removes its own vision; recharged, it gives it again.
- **Industrial helmets:** 200 JE per 160 ticks of supplied vision, unchanged ([night-vision metering](night-vision-metering.md)). The helmet stops once its paid time is used and it cannot pay again; recharged, it resumes.
- **No payment while other vision is on.**

## Connections

- Existing input producer: the JE producers and charging stations.
- Existing output consumer: mining and exploration in the dark.
- No new resource, recipe, unlock or conversion.

## Multiplayer and persistence

- **Server only, per player.** Nothing is saved; no migration.
- **Ownership lasts one session.** After a relog, the helmet no longer recognises an effect left from before. That effect runs out within 20 seconds, unpaid, and then the helmet gives its own.

## Dependencies and assets

None.

## Verification

The server game tests are in `PoweredNightVisionGameTests`. Each seals its player in a dark stone room. None of them have been run: this environment cannot build the mod.

| Test | What it checks |
| --- | --- |
| `helmetsGiveAndTakeAwayOnlyTheirOwnVision` | Goggles: on, off, on again without paying twice, toggled off. Exosuit helmet: takes over, runs flat, is recharged, has its setting turned off. An alike effect it did not give is neither paid for nor removed. Spectator mode removes the helmet's vision. |
| `aLongerPotionIsKeptAndTheHelmetResumesAfterIt` | A drunk potion before the goggles, and drunk over the goggles' vision (merged in place). Not paid for, kept when the goggles come off; the goggles' own vision comes back after it. |
| `aShorterPotionMergedIntoHelmetVisionRunsOutBeforeTheHelmetResumes` | A shorter potion merged into the goggles' vision is never removed or paid for. It runs to its end over real ticks, and the goggles resume on time already paid. |
| `beaconVisionIsNeverTheHelmets` | A beacon's ambient vision with particles: before the helmet, a shorter pulse merged in, a longer pulse merged in. |
| `alikeOrEndlessVisionMergedIntoTheHelmetsIsNotTakenAway` | The two cases the review found. PR609's previous check would have removed both. |
| `gogglesBesideTheExosuitGiveSteadyVisionAtTheirOwnPrice` | The 7 November report, beside a worn exosuit. Real end-of-tick callbacks only: steady vision above the flashing window for 340 ticks, exactly 200 JE per 160 ticks, and the vision goes when the goggles come off. |
| `theJimtafactsGogglesAndAnExosuitHelmetDoNotFight` | The accessory and an exosuit helmet worn together. The vision stays steady and the helmet stops paying. With the accessory removed, its vision runs its whole time before the helmet takes over and pays again. |
| `flatGogglesStopTheirOwnVisionAndRechargedGogglesResume` | One payment gives 160 ticks. Then the flat goggles remove their own vision, and recharged goggles resume. |

The PR609 tests are unchanged. By reading them against this rule, they should still pass: `PoweredArmorGameTests`, `PoweredEquipmentGameTests`, the night-vision cases in `JimtaIndustrialGearGameTests`, `NightVisionUsageProbe` and `PoweredArmorClientGameTests`.

## World and event applicability

Applies wherever a player wears a powered helmet; no world, event or season rules apply.

## Rollout and open questions

- **Flashing at the end of another source's vision.** When another source's vision runs out while a helmet is worn, it flashes in its last 10 seconds before the helmet takes over. Topping it up would mean writing into another source's effect, and the helmet could then take part of it away later.
- **A shorter potion merged into the helmet's vision** keeps the helmet's longer leftover (at most 20 seconds) after the potion's own time, unpaid.
- **Not detectable:**
  - an alike effect (exactly the helmet's flags) shorter than the helmet's own, which merges invisibly and goes with it; no vanilla or Jugcraft source gives that look;
  - an alike effect that raises the duration by no more than the one tick since the last check.
- **Reverting:** this restores PR609's previous rule. No save data is involved.
