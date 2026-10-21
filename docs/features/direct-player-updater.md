# Direct player distribution and persistent updates

Owner requested 21 October 2026. Implemented using OpenAI Codex (GPT-6).

## Player flow

Windows players download `JUGCRAFT-Installer.zip` from Discord/GitHub and run
`JUGCRAFT.exe`. Initial installation opens one verified `.mrpack` import in the
official Modrinth App. Players select the resulting instance folder (or adopt
an already matching installation) and copy one command into that instance's
**Launch hooks > Pre-launch** setting. This manual setup is required once;
the installer does not edit Modrinth's private database or account information.

After setup, the usual Modrinth Play button runs the updater before Minecraft.
The updater checks the owner's promoted channel, asks before changing versions,
and applies changes in that same folder. Cancellation or an update/check failure
returns a nonzero exit code and prevents launch. An unchanged release returns
success without a prompt. Modrinth's official implementation waits for the hook
and checks its exit code: [run.rs](https://github.com/modrinth/code/blob/main/packages/app-lib/src/api/instance/run.rs).

## Preserved state and limits

Only reviewed `mods/*.jar` paths in the managed inventory may be replaced or
removed. Unrelated mods, options, keybinds, configs, saves, screenshots, resource
packs, shader selections and server lists are not touched. Initial import supplies
defaults; a future required config migration must be explicitly implemented.
Modified managed files cause a conflict rather than silent overwriting. Adoption
requires an exact known inventory; an arbitrary old pack is not guessed compatible.
Existing bound profiles may be reselected after a move.

The updater uses HTTPS and SHA-512 checks, limits downloads and inventory size,
rejects unsafe paths/reparse points and unapproved sources, stages all payloads
before replacing files, checks for running Java processes using this profile,
and holds a per-instance exclusive lock. An on-disk journal and verified backups
recover an interrupted replacement before the next check. It never downloads or
executes scripts specified by a release. The GitHub repository/release account is
the update trust root; hashes protect integrity, not a compromised publisher.

This first executable supports Windows 10/11 and .NET Framework 4.x. It stays at
Minecraft 26.3 / Fabric 0.19.5 / Java 25. Platform changes and updater executable
upgrades require reviewed manual setup. It is unsigned. No game startup downloader,
security-policy changes, account login handling or automatic server deployment.
Disabling the hook bypasses checks. Offline/check errors stop launch rather than
claiming the installed version matches the live server.

## Owner publishing

`scripts/package_release.py` now also creates `player-update.json`,
`jugcraft-client.jar` and a candidate `stable.json`. These are fixed to the exact
pack version and source commit. Do not replace already published payload files.

1. Build/test the release, publish its immutable payloads, stage/deploy the matching
   server version and verify startup. Preserve a matching world backup.
2. Use `scripts/promote_player_update.py` with that release tag and the live server
   version. It checks the public manifest hash and release asset sizes, and changes only the
   `stable.json` asset on the `player-channel` GitHub release.
3. The next pre-launch check sees that promotion. A main commit, PR merge or
   unrelated GitHub release never promotes an update by itself.

Build the Windows updater with `distribution/updater/build.ps1`. Share its EXE
and `distribution/updater/READ-ME.txt` as `JUGCRAFT-Installer.zip`. The application
keeps a stable copy in `%LOCALAPPDATA%/JUGCRAFT`, so deleting the download does not
break the hook. No Modrinth listing or moderation is involved. Normal pinned
library downloads still use their authors' official CDN locations.

## Validation

The offline Windows harness exercises failed hashes, rollback after partial
replacement, process-death recovery, unchanged-release no-op, missing-file repair,
settings/config/save preservation, unknown-mod preservation, managed-file
conflicts, path/URL rejection and platform/schema limits. Packaging tests cover
fixed release identities and excluded personal files. Results and remaining
end-to-end checks are recorded with the delivered installer.

Local validation on 21 October: the Windows harness passed 34 assertions. The
full Python suite passed 179 tests; the subsequently added disabled-listing
packaging and promotion-validation cases passed in their focused suites. The
updater compiled with warnings treated as errors. Its setup window was rendered
and inspected. Pack pins and repository checks passed. No gameplay code changed,
and Minecraft was not launched or controlled. Native launcher-hook setup and a
real player acceptance/cancellation flow remain human end-to-end checks; offline
fixtures and official launcher source are not claimed as a played game test.
