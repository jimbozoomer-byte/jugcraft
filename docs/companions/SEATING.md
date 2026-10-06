# Companion seating

Small Wooden Stool: `/give @s peepo_companion:wooden_stool`, also in Functional Blocks. Craft two planks over two sticks (`PP / SS`). Players can right-click it to sit; sneak to leave. The oak seat is 10 pixels wide and 7 pixels high, with four wooden legs.

Peepo, pumpkin Peepo and Jughead automatically discover nearby seats when no useful wheel or nighttime bed takes priority. Discovery checks loaded blocks within six blocks horizontally and two vertically, every two seconds. They approach a clear ground position, reserve one seat, and rest with alternating little leg kicks and hands resting forward. Sitting regenerates 8 JE/t plus the active food bonus. Full-energy companions can take an optional break too; normal breaks last at most 30 seconds including approach, then have a 30-second leisure cooldown. Exhausted companions remain resting until recovery permits work again. Food interrupts resting.

Supported now:
- Any current or future block implementing Jugcraft `Seat.Sittable` (rocking chairs, haunted dining chairs, bone thrones, memorial benches, hay bale seats, skull footstools, and the new stool).
- The Operator Chair, through its existing separate seating system.
- The foot-half edges of vanilla beds, companion bed foot edges, and fence posts, provided the approach and headroom are clear.

Shared furniture rejects player seating while reserved by a companion and companions avoid player-occupied seats. Companion sleeping and edge sitting share exclusive bed occupancy. Removing/changing a surface, unloading it, or eating releases the seat. The ground exit persists for safe recovery after a reload; gravity is restored on leaving.

Future furniture should implement `Seat.Sittable` and use `Seat.sit` for player seating: companions then discover it automatically with the correct seat height and shared occupancy. Simple additional surfaces may be opted in using the `peepo_companion:seats` block tag; they use the collision-shape top as the sitting height. External mods with independent passenger entities or custom seating rules need an adapter for occupancy and height; the tag alone cannot coordinate an unknown mod's player seats. Vehicles are not treated as rest furniture.

Built with assemble only; no automated tests or in-game checks were run. Suggested manual checks: stool use by a player and both companions, all existing chair styles, single/stacked bed edges with free headroom, wooden and nether-brick fences, feeding while seated, removal while occupied, and save/reload.
