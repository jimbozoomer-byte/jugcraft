JUGCRAFT release candidate: matched player pack and server upload bundle built from the exact main commit whose Build and Foundation checks passed. The source SHA, platform pins and SHA-512 checksums are in `release-manifest.json`.

- Players: use the owner's `JUGCRAFT-Installer.zip`. Import once, select that instance folder, and copy its command into that instance's Launch hooks > Pre-launch. Later launches prompt to update that same installation while preserving personal files. Plain `.mrpack` imports still work but need the hook for update checks. No Modrinth listing is required.
- Owner: the server ZIP contains only server-compatible mods and default configuration. Select Minecraft 26.3 / Fabric Loader 0.19.5 / Java 25 in the host panel; follow `START-HERE.txt`. It is an upload bundle, not a standalone server launcher.
- Ongoing updates: after deploying and verifying the matching server, explicitly promote this release's `stable.json` to the GitHub `player-channel` release. A main commit or release publication alone does not update players. See `docs/features/direct-player-updater.md` for installation, promotion and recovery.

This candidate has not been deployed to the live server. Human multiplayer testing, the server address/project configuration and owner promotion remain separate steps. The custom JUGCRAFT title screen and server Play button are deferred as requested.
