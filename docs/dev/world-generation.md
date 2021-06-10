# World Generation

## Facet Providers

Primary world generator used is the `DynamicWorldGenerator`.
The module uses custom humidity, surface and temperature providers.
Other necessary facet providers are taken straight out from other modules, without modification.

### Surface

The `SurfaceProvider` uses subsampled simplex noise to generate minor variations in terrain, 3 blocks in size, by this line of code

```java
facet.setWorld(position, surfaceNoise.noise(position.x(), position.y()) * 3 + 10);
```

Mountains are formed using the `SimplexHillsAndMountainsProvider` from the **Core** module.

### Biomes

The module uses the default `BiomeProvider`from **Core**, which uses humidity and temperature values to assign biomes.
These parameters are adjusted through the module's custom `HumidityProvider` and `TemperatureProvider`.
For now they maintain constant values throughout, which results in the entire map being a desert.

## Cities

The module has an implementation of **DynamicCities**.
Multiple cities can be spawned and have several NPCs inside them with their own behaviors, which depends on how the player interacts with them.
Players can also trade with NPCs and buy/sell items in markets.
All discovered cities are also shown on the player's minimap.

For more details on NPC behavior: [AI](dev/ai.md)

For more details on trade and markets: [Economic Systems](dev/economic-systems.md)

## Rails

**DynamicCities** exposes certain functions of road placement which allow other modules to specify custom _road rasterizers_.
MetalRenegades connects cities with rails, using the `RailRasterizer`.
The `RoadManager` passes this rasterizer to the `Construction` system in DynamicCities which handles actual block placement. 

The `RailRasterizer` creates rails in a zig-zag fashion and lays down rails on two adjacent sides of the provided road segments, something like in the diagram below, which leads to some inconsistencies at segment borders.

<fig src="/_media/rail-rasterizer.png" alt="Mapping of rail blocks to road segements">Mapping of rail blocks to road segements.</fig>
