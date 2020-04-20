/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.metalrenegades.economy.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.economy.actions.ShowMarketScreenAction;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.namegenerator.creature.CreatureAssetTheme;
import org.terasology.namegenerator.creature.CreatureNameProvider;
import org.terasology.registry.In;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

/**
 * Spawns a market citizen in all markets
 */
@RegisterSystem
public class MarketCitizenSpawnSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    private Logger logger = LoggerFactory.getLogger(MarketCitizenSpawnSystem.class);

    Random random = new Random();

    @ReceiveEvent(components = GenericBuildingComponent.class)
    public void onMarketPlaceSpawn(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        GenericBuildingComponent genericBuildingComponent = entityRef.getComponent(GenericBuildingComponent.class);
        if (genericBuildingComponent.name.equals("marketplace")) {
            DynParcel dynParcel = entityRef.getComponent(DynParcelRefComponent.class).dynParcel;

            Optional<Prefab> traderGooeyOptional = Assets.getPrefab("MetalRenegades:marketCitizen");
            if (traderGooeyOptional.isPresent()) {
                Rect2i rect2i = dynParcel.shape;
                Vector3f spawnPosition = new Vector3f(rect2i.minX() + rect2i.sizeX() / 2, dynParcel.getHeight() + 1, rect2i.minY() + rect2i.sizeY() / 2);
                EntityRef trader = entityManager.create(traderGooeyOptional.get(), spawnPosition);

                SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
                trader.addComponent(settlementRefComponent);
                MarketComponent marketComponent = settlementRefComponent.settlement.getComponent(MarketComponent.class);
                if(entityRef.hasComponent(DisplayNameComponent.class)) {
                    DisplayNameComponent displayNameComponent = entityRef.getComponent(DisplayNameComponent.class);
                    CreatureNameProvider creatureNameProvider = new CreatureNameProvider(random.nextLong(), CreatureAssetTheme.OLD_WEST);
                    displayNameComponent.name = "shopkeeper " + creatureNameProvider.generateName();
                    entityRef.saveComponent(displayNameComponent);
                }


                DialogComponent dialogComponent = new DialogComponent();
                dialogComponent.pages = new ArrayList<>();

                DialogPage mainPage = new DialogPage();
                mainPage.paragraphText = new ArrayList<>();
                mainPage.responses = new ArrayList<>();

                DialogResponse buyResponse = new DialogResponse();
                buyResponse.action = new ArrayList<>();
                buyResponse.action.add(new ShowMarketScreenAction(marketComponent.market.getId(), TransactionType.BUYING));
                buyResponse.text = "Buy";

                DialogResponse sellResponse = new DialogResponse();
                sellResponse.action = new ArrayList<>();
                sellResponse.action.add(new ShowMarketScreenAction(marketComponent.market.getId(), TransactionType.SELLING));
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
