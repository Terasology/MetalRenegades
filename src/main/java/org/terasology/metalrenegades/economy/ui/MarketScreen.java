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
package org.terasology.metalrenegades.economy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.economy.events.MarketTransactionRequest;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.metalrenegades.economy.systems.MarketManagementSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UIText;

import java.util.ArrayList;
import java.util.List;

/**
 * UI for the marketplace
 */
public class MarketScreen extends CoreScreenLayer {

    @In
    private LocalPlayer player;

    @In
    private MarketManagementSystem marketManagementSystem;

    @In
    private NUIManager nuiManager;

    private UIList<MarketItem> items;

    private UILabel name;
    private UILabel cost;
    private UILabel quantity;

    private UIText description;

    private UIButton confirm;
    private UIButton back;

    private List<MarketItem> list = new ArrayList<>();
    private MarketItem selected = MarketItemBuilder.getEmpty();
    private TransactionType type;

    private Logger logger = LoggerFactory.getLogger(MarketScreen.class);

    @Override
    public void initialise() {

        // Initialise name label
        name = find("itemName", UILabel.class);
        name.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return selected.displayName;
            }
        });


        // Initialise description text
        description = find("description", UIText.class);
        description.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return selected.description;
            }
        });


        // Initialise cost label
        cost = find("cost", UILabel.class);
        cost.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return "Cost: " + selected.cost;
            }
        });


        // Initialise quantity label
        quantity = find("quantity", UILabel.class);
        quantity.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return "In Stock: " + selected.quantity;
            }
        });


        // Initialise items list
        items = find("itemList", UIList.class);
        items.setList(new ArrayList<>());
        items.setItemRenderer(new StringTextRenderer<MarketItem>() {
            @Override
            public String getString(MarketItem value) {
                return value.displayName;
            }
        });
        items.subscribeSelection(((widget, item) -> handleItemSelection(item)));
        items.bindList(new ReadOnlyBinding<List<MarketItem>>() {
            @Override
            public List<MarketItem> get() {
                return list;
            }
        });


        // Initialise confirm button
        confirm = find("confirm", UIButton.class);
        confirm.subscribe((widget -> {
            if (type == TransactionType.BUYING || type == TransactionType.SELLING) {
                MarketTransactionRequest marketTransactionRequest = new MarketTransactionRequest();
                marketTransactionRequest.item = selected;
                marketTransactionRequest.type = type;
                player.getCharacterEntity().send(marketTransactionRequest);
                logger.info("Confirmed transaction of one {}", selected.name);
            } else {
                logger.warn("TransactionType not recognised. No transaction.");
            }
        }));


        // Initialise back button
        back = find("back", UIButton.class);
        back.subscribe(widget -> {
            nuiManager.closeScreen("MetalRenegades:marketScreen");
        });
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public void onClosed() {
        super.onClosed();
        selected = MarketItemBuilder.getEmpty();
        items.setSelection(selected);
    }

    public void setItemList(List<MarketItem> resources) {
        list = resources;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    private void handleItemSelection(MarketItem item) {
        selected = item;
    }
}
