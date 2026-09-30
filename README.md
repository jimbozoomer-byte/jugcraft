# Jugcraft

A community-built Minecraft Java 26.3 + Fabric experience about factories, magical workshops, cozy homes, and dangerous frontiers.

**Status: contribution foundation.** Feature proposals and documentation contributions are welcome. There is no playable mod, server, or Minecraft build yet. The first implementation milestone establishes the pinned platform and working mod before feature code is accepted.

## The vision

Build factories and magical workshops, specialize in agriculture or a school of magic, and trade with other players. Settle in cozy biomes, brave dangerous caves and dungeons, raise friendly companions, find rare equipment, launch rockets to moons and planets, and open portals to magical realms. Halloween and Christmas-esque seasonal content should grow from these permanent systems. Branches stand on their own while supporting each other at meaningful milestones.

Jugcraft's original gameplay ships as one mod, organized into internal feature packages. Third-party mods, if approved, remain separately installed dependencies in an accompanying pack. We do not copy their code or bundle their JARs into Jugcraft.

## Start here

- [Propose a feature](../../issues/new?template=feature.yml): no coding experience needed.
- [Propose an existing mod integration](../../issues/new?template=integration.yml).
- [Contribute code, art, or documentation](CONTRIBUTING.md).
- [Read the connected gameplay design](docs/DESIGN.md).
- [Explore specialties, magic schools, creatures and seasonal briefs](docs/CONTENT_BRANCHES.md).
- [See architecture and integration rules](docs/ARCHITECTURE.md).
- [Check the roadmap](docs/ROADMAP.md) and [platform decision](docs/PLATFORM.md).
- [Maintainer setup and review guide](docs/MAINTAINERS.md).

## How additions become part of Jugcraft

Proposal → maintainer approval → fork and feature branch → commits → pull request → checks and review → multiplayer playtest → merge → numbered release.

One feature per PR. Include the proposal, integration contract, and actual test evidence. An approved idea is permission to develop it, not a promise to merge it. Maintainers decide fit, quality, and release timing.

## AI-assisted contributions

Use **Claude Opus 5.5** for AI-assisted feature implementation and review your output yourself. Follow [CLAUDE.md](CLAUDE.md). Declare model use honestly; documentation, ideas, and hand-made art do not require an AI subscription. Git history cannot prove model provenance. This initial repository foundation was prepared with OpenAI Codex, not Opus 5.5.

## Local repository checks

Install Python 3.11 or newer, then run:

```sh
python scripts/check_repository.py
```

This checks the contribution foundation only. It does **not** compile Minecraft code or certify multiplayer compatibility. The platform bootstrap must introduce a pinned Gradle wrapper, mod build, and required game tests before implementation PRs can merge.

## Rights and conduct

Be constructive and credit contributors. Follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) and [SECURITY.md](SECURITY.md). See [LICENSE_POLICY.md](LICENSE_POLICY.md): maintainers must choose and publish the project license before accepting implementation or asset contributions.
