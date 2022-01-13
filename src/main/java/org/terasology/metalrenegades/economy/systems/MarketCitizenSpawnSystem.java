// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.systems;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.behaviors.components.StrayRestrictionComponent;
import org.terasology.dialogs.action.CloseDialogAction;
import org.terasology.dialogs.components.DialogComponent;
import org.terasology.dialogs.components.DialogPage;
import org.terasology.dialogs.components.DialogResponse;
import org.terasology.dynamicCities.buildings.GenericBuildingComponent;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.economy.actions.ShowMarketScreenAction;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.metalrenegades.minimap.events.AddCharacterToOverlayEvent;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Spawns a market citizen in all markets
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class MarketCitizenSpawnSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    private Logger logger = LoggerFactory.getLogger(MarketCitizenSpawnSystem.class);

    @ReceiveEvent(components = GenericBuildingComponent.class)
    public void onMarketPlaceSpawn(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        GenericBuildingComponent genericBuildingComponent = entityRef.getComponent(GenericBuildingComponent.class);
        if (genericBuildingComponent.name.equals("marketplace")) {
            DynParcel dynParcel = entityRef.getComponent(DynParcelRefComponent.class).dynParcel;

            Optional<Prefab> traderGooeyOptional = Assets.getPrefab("MetalRenegades:marketCitizen");
            if (traderGooeyOptional.isPresent()) {
                BlockArea area = new BlockArea(dynParcel.getShape().getMin(new Vector2i()), dynParcel.getShape().getMax(new Vector2i()));
                Vector3f spawnPosition = new Vector3f(area.minX() + area.getSizeX() / 2, dynParcel.getHeight() + 1,
                        area.minY() + area.getSizeY() / 2);
                EntityRef trader = entityManager.create(traderGooeyOptional.get(), spawnPosition);
                trader.send(new AddCharacterToOverlayEvent());

                SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
                trader.addComponent(settlementRefComponent);
                MarketComponent marketComponent = settlementRefComponent.settlement.getComponent(MarketComponent.class);
                BlockArea dynParcelShape = new BlockArea(dynParcel.getShape().getMin(new Vector2i()), dynParcel.getShape().getMax(new Vector2i()));
                trader.addComponent(new StrayRestrictionComponent(dynParcelShape));

                DialogComponent dialogComponent = new DialogComponent();
                dialogComponent.pages = new ArrayList<>();

                DialogPage mainPage = new DialogPage();
                mainPage.paragraphText = new ArrayList<>();
                mainPage.responses = new ArrayList<>();

                DialogResponse buyResponse = new DialogResponse();
                buyResponse.action = new ArrayList<>();
                buyResponse.action.add(new ShowMarketScreenAction(marketComponent.getMarketId(),
                    TransactionType.BUYING));
                buyResponse.text = "Buy";

                DialogResponse sellResponse = new DialogResponse();
                sellResponse.action = new ArrayList<>();
                sellResponse.action.add(new ShowMarketScreenAction(marketComponent.getMarketId(),
                    TransactionType.SELLING));
                sellResponse.text = "Sell";

                DialogResponse closeResponse = new DialogResponse();
                closeResponse.action = new ArrayList<>();
                closeResponse.action.add(new CloseDialogAction());
                closeResponse.text = "Close";

                mainPage.id = "MainPage";
                mainPage.paragraphText.add("Need anything?");
                mainPage.title = "Welcome to the market";
                mainPage.responses.add(buyResponse);
                mainPage.responses.add(sellResponse);
                mainPage.responses.add(closeResponse);

                dialogComponent.pages.add(mainPage);
                dialogComponent.firstPage = mainPage.id;
                trader.addComponent(dialogComponent);
            }
        }
    }
}
