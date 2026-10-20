# Installing and distributing Jugcraft

Jugcraft has two artifacts:

- **Jugcraft JAR:** the original mod. Supporting launchers can install the required dependencies declared on its Modrinth release. Manual JAR installation still requires the dependencies.
- **Jugcraft Complete `.mrpack`:** the original mod plus every selected framework and presentation integration, with exact versions and client/server flags. Import this file in Modrinth App and confirm installation to install the complete set automatically.

Opening a website alone does not install software. Fabric dependency metadata checks requirements but does not download them. Jugcraft never downloads mods at game startup.


### Diorama map included by default

From the October 18 Diorama pack update, every newly generated Complete pack installs
[Diorama 0.1.1 for 26.3](https://modrinth.com/mod/diorama-map/version/kWas7LpC)
from its official Modrinth download, with a pinned size, SHA-1 and SHA-512. It is
required for the pack's client installation and excluded from server installation.
Standalone Jugcraft still loads without it. The existing Fabric API dependency
already satisfies Diorama's requirements; no additional platform upgrade is needed.

Pack controls: **M** opens the map, **N** creates a waypoint, **U** opens the waypoint
list, and **B** remains the Jugcraft encyclopedia. The new-waypoint default differs
from upstream's B to avoid opening two screens with one key. Saved custom bindings
are preserved; if Diorama was installed previously with B, change its New Waypoint
binding to N in Controls (or reset that binding).

Existing exported packs and already installed profiles are not changed retroactively.
Import the new pack or explicitly update an existing profile. See the
[integration and validation record](features/diorama-pack.md).
