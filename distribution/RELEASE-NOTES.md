JUGCRAFT release candidate: matched player pack and server upload bundle built from the exact main commit whose Build and Foundation checks passed. The source SHA, platform pins and SHA-512 checksums are in `release-manifest.json`.

- Players: use `JUGCRAFT-Join.zip` or https://modrinth.com/server/jugcraft-server, click Play and accept the linked installation. Keep it linked for pre-launch update prompts. The Server Project must be approved before public access is available. The versioned installer/player ZIP are manual-import fallbacks and do not subscribe to updates.
- Owner: the server ZIP contains only server-compatible mods and default configuration. Select Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25 in the host panel; follow `START-HERE.txt`. It is an upload bundle, not a standalone server launcher.
- Ongoing updates: use a Modrinth Server Project linked to this exact pack version. A standalone downloaded pack does not subscribe itself to updates. See the repository's `docs/SERVER_RELEASES.md` for staging, promotion and rollback.

This candidate has not been deployed to the live server. Human multiplayer testing, the server address/project configuration and owner promotion remain separate steps. The custom JUGCRAFT title screen and server Play button are deferred as requested.
