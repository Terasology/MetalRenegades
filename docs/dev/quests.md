# Quests

This module uses the **Tasks** module to implement quests.
For more information, refer to the [Tasks](https://github.com/Terasology/Tasks) or [TutorialQuests](https://github.com/Terasology/TutorialQuests) modules.

## Quest Items

The **Tasks** module works in this way.
There is a _Quest Entity_ which gives you a _Quest Item_.
You need to _use_ the item to activate the quest.
Information about the tasks in the quest is actually in the quest item, which in our case is the `card.prefab`.
The sample fetch quest provided has a `CollectBlocksTask` and a `GoToBeaconTask`, both of which are provided in the **Tasks** module and are used without modification.

```json
"Quest" : {
    "shortName" : "FetchQuest",
    "description" : "Bring me some meat!",
    "tasks" : [
        {
            "id" : "collectMeat",
            "class" : "CollectBlocksTask",
            "data" : {
                "itemId" : "WildAnimals:Meat",
                "targetAmount" : 2
            }
        },
        {
            "id" : "returnHome",
            "class" : "GoToBeaconTask",
            "dependsOn" : "collectMeat",
            "data" : {
                "targetBeaconId" : "homeBeacon"
            }
        }
    ]
}
```

## FetchQuestSystem

The `FetchQuestSystem` handles spawning and deletion of Quest Entities, inventory modification and other work when tasks or quests are completed/started.
 - `onChurchSpawn` adds a quest entity to all churches
 - `onQuestActivated` keeps track of the items required (for inventory management) and which quest entity gave the currently active quest
 - `onReturnTaskInitiated` changes the quest entity into a _beacon_ which the player must return to in order to complete the quest. Beacons are provided in the **Tasks** module and are used without modification
 - `onQuestComplete` removes the collected items from the player's inventory and adds the reward money to the player's wallet through the `UpdateWalletEvent` from the **Economy** module
