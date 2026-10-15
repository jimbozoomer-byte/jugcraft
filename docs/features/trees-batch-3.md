# Trees batch 3: yew and the Gloomweald

Status: owner-authorized implementation in progress, 15 October 2026. Public branch `speedygroyper:codex/tree-generation-batch-3-yew`, based on the tested batch-2 checkpoint. Actual tool: OpenAI Codex (GPT-6).

The owner requested the remaining [tree roster](../branches/TREES.md) after batch 2. Its palettes were approved on 6 October; yew uses the approved painted wood color `#654135`. Runtime logs follow the subsequent 11 October vanilla-texture reference policy. Pine's separately approved original textures remain unchanged.

## Intended player outcome

Yew supplies an evergreen, low, dark crown and a full timber set for building, with naturally ripening red arils. Single saplings grow ordinary yew; four grow a gnarled ancient yew. Arils are poisonous food, a source of red dye and an optional cauldron floater. Existing wood processing and replanting apply without a new progression gate.

Gloomweald replaces the overlapping vanilla dark-forest vegetation with one canopy list: about sixteen attempts per chunk, dark oak 45%, yew 20%, dead tree 10%, dead snag 10%, huge red mushroom 8%, huge brown mushroom 7%. Tree attempts still require compatible ground and space. New chunks only.

The yew also introduces optional leaf drawing controls for gaps, blossom edge greens/coverage and frond midribs. Existing texture defaults and seeds stay identical. Later roster trees reuse these controls.

This is Discovery-stage exploration/building content: natural trees and planted saplings produce timber; players, crafting, sawmill and tree farm consume it. No required cross-specialty dependency is added. Failed giant growth retains the four saplings. Evergreen fruiting leaves tick only while unripe or while unsupported and decaying; player-placed leaves do not fruit.

## Evidence

Implementation and tests are pending. Record growth dimensions, seasonal/fruit behavior, wood recipes and the actual natural canopy survey before marking complete. The previous batch's test results do not validate this work. No merge, release or server deployment is included.
