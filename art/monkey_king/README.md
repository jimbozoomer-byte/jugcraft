# Monkey King

Sun Wukong as a Jugcraft boss model, built by `tools/monkey_king.py` (run it with `--bbmodel` to regenerate; `--preview out.png` renders views and a frame sheet). Model and clips only; no entity, loot or recipes yet (see the [TODO](../../docs/TODO.md#monkey-king-make-him-a-boss)).

- `monkey_king.bbmodel`: the king, 397 boxes, bones `body > kilt, head, tail, left_arm, right_arm`, `left_leg`, `right_leg`. Pivots at the hip (0, 42, 0), neck (0, 84, 0), shoulders (±19, 79, 0), hips (±7.2, 42, 0) and tail root (0, 45, -8); ground at y = 0, about 150 pixels to the feather tips (three times the Monkey Monk). Clips: `idle` 4.0 s, `walk` 1.6 s (loops), `jump` 2.5 s, `roar` 2.5 s, `attack_smash` 2.2 s, `attack_sweep` 2.2 s, `attack_thrust` 1.5 s.
- `ruyi_staff_large.bbmodel`: his staff, 149 pixels long with the grip at the origin, to be parented to the right hand (hand pivot 48 pixels below the right shoulder) with the arm's rest rotation cancelled by (24, 0, 16).
- Textures `mkk_*` (22, 16×16): drawn by the script in the project's flat clean manner (fur, cream face, scale mail, gold, lacquer red, cape with cloud squares, tiger stripes, feathers, jade, flame).

Previews: `docs/images/monkey_king_preview.png` (four views) and `docs/images/monkey_king_frames.png` (four frames of each clip).
