# Economy

This modules uses the **Economy** module to implement these functions. For more information, refer to its [wiki](https://github.com/Terasology/Economy/wiki).

## Marketplace

All marketplaces have a _Market Citizen_, marked by the `MarketCitizenComponent`.
These don't spawn anywhere else in the city. Spawning these citizens and setting up their dialogue is handled by the `MarketCitizenSpawnSystem`.
Players have to interact with this market citizen to bring up the marketplace UI.

### Basic Systems

The entire market system majorly depends on two systems: `MarketManagementSystem` and `MarketUISystem`.
 - `MarketManagementSystem`
   - Sets the market up with the **Economy** module
   - Handles the actual transactions with the `buy` and `sell` functions. These also manage the inventory, translate the _resource_ being bought into actual items (while buying) and vice-versa (while selling)
 - `MarketUISystem`
   - Fetches the list of resources the city has from the **Economy** module while buying, or fetches the player's inventory items while selling
   - Handles toggling the actual market UI and passes the resources fetched as `MarketItem`s

### Other Supporting Classes

 - `MarketItem`: A data class containing information about a market resource. It is created using the `MarketItemBuildier` which also contains a map with all this information.
 - `ShowMarketScreenAction`: It is a `PlayerAction` launched when the player selects an option in the market dialogue. It launches a `MarketScreenRequestEvent`
 - `MarketScreenRequestEvent`: It is an event which is consumed by the `MarketUISystem` to bring up the UI. It also containd the transaction type - buy or sell, that the `MarketUISystem` uses to differentiate the two possible UIs

## Trading

Tradable NPCs are marked by the `TraderComponent`.
All trading and UI manipulation happens through the `TradingUISystem`.
It works a lot like our markets. When a trade is proposed, we first evaluate the `isAcceptable` function and then execute the `trade` function, which handles the actual inventory management.

## Resource Management

### Syncing Resources

Currently, the transactions that happen through the marketplace don't use resource manipulation events provided in the **Economy** module properly.
We use the `ResourceStoreEvent` and `ResourceDrawEvent`, but the _quantity_ of the items bought or sold doesn't sync.
This is especially apparent with the resources kept in the chests with the Blacksmith.

### Custom Resource Production

Related to the above problem, the **Economy** module also allows custom resource generation systems, which we cannot leverage at this point.
