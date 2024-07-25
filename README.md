# mobrenamer

## A mod to modify spawned mobs.

---

This mod uses a JSON configuration file, making it easy to customize without needing to modify code.

What's more, instead of using string to configure names.

it allows you to use Text Component to identify all names.

Therefore you can add translations to all your names.

Here's an example of how you can use MobRenamer:
The path of the file should be nameOfDatapack/data/mobrenamer/definitions/anyName.json
```json
{
  "mob_definitions": {
    "minecraft:zombie": {
      "type": "minecraft:zombie",
      "always_named": true,
      "names": [
        {
          "name": {
            "text": "Super Zombie",
            "color": "red",
            "bold": true
          },
          "probability": 0.5,
          "structures": ["minecraft:village_plains", "minecraft:pillager_outpost"],
          "biomes": ["minecraft:plains", "minecraft:desert"],
          "dimensions": ["minecraft:overworld"],
          "attributes": {
            "minecraft:generic.max_health": 30.0,
            "minecraft:generic.attack_damage": 5.0
          },
          "spawnTypes": ["NATURAL", "SPAWNER"],
          "priority": 10
        }
      ]
    },
    "minecraft:skeleton": {
      "type": "minecraft:skeleton",
      "always_named": false,
      "names": [
        {
          "name": {
            "text": "Elite Archer",
            "color": "aqua"
          },
          "probability": 1.0,
          "structures": ["minecraft:stronghold"],
          "biomes": [],
          "dimensions": ["minecraft:overworld", "minecraft:the_nether"],
          "attributes": {
            "minecraft:generic.movement_speed": 0.3,
            "minecraft:generic.attack_speed": 1.5
          },
          "spawnTypes": ["NATURAL", "COMMAND"],
          "priority": 5
        }
      ]
    }
  }
}
```
In this example, zombies in villages or outposts have a 50% chance to be named "Super Zombie" with increased health and damage,

while skeletons in strongholds are always named "Elite Archer" with improved movement and attack speed.

> If you set always_named to true and no names meet their requirements, the first name will be chosen as the default name.