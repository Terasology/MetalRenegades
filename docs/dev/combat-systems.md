# Combat System

Firearms as arrows, player drops with DropItemRequest

## Firearms

The pistol and the gatling gun are implemented as bows and bullets are just reskinned arrows. For example, in the `pistol.prefab`

```json
"Item": {
    "icon": "MetalRenegades:pistol",
    "usage": "IN_DIRECTION",
    "cooldownTime": 400
},
```

The `cooldownTime` parameter is essentially what makes this bow a gun. This below is from the `bullet.prefab` which makes our arrows hurt like bullets

```json
"Hurting" : {
    "amount" : 20
}
```

## Animal Spawn

The `WildAnimalsSpawnSystem` exposes us ways to set custom configuration options (through `AnimalSpawnConfig`) and spawn locations (through `setSpawnCondition`). The `AnimalSpawnSystem` in Metal Renegades handles these.

## Inventory

The player's starting inventory is setup by the `PlayerInventorySystem` through the `CombatStartingInventory` in the **CombatSystem**
