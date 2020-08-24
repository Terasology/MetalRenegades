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

import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.economy.events.TradeRequest;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.itemRendering.StringTextRenderer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;

import java.util.ArrayList;
import java.util.List;

/**
 * UI for trading with citizens
 */
public class TradingScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    @In
    private TradingUISystem tradingUISystem;

    @In
    private NUIManager nuiManager;

    @In
    private MarketItemRegistry marketItemRegistry;

    /**
     * UI Elements
     */
    private UIList<MarketItem> pList;
    private UIList<MarketItem> cList;
    private UIButton confirm;
    private UIButton cancel;
    private UILabel result;
    private UILabel pCost;
    private UILabel cCost;

    /**
     * Information for UILists
     */
    private List<MarketItem> pItems = new ArrayList<>();
    private List<MarketItem> cItems = new ArrayList<>();

    /**
     * Selected items
     */
    private MarketItem pSelected;
    private MarketItem cSelected;

    /**
     * Trade result message
     */
    private String message;

    @Override
    public void initialise() {
        pSelected = marketItemRegistry.getEmpty();
        cSelected = marketItemRegistry.getEmpty();

        // Initialize player inventory list
        pList = find("playerList", UIList.class);
        pList.setList(new ArrayList<>());
        pList.setItemRenderer(new StringTextRenderer<MarketItem>() {
            @Override
            public String getString(MarketItem value) {
                return value.displayName;
            }
        });
        pList.subscribeSelection(((widget, item) -> pSelected = item));
        pList.bindList(new ReadOnlyBinding<List<MarketItem>>() {
            @Override
            public List<MarketItem> get() {
                return pItems;
            }
        });

        // Initialize citizen inventory list
        cList = find("citizenList", UIList.class);
        cList.setList(new ArrayList<>());
        cList.setItemRenderer(new StringTextRenderer<MarketItem>() {
            @Override
            public String getString(MarketItem value) {
                return value.displayName;
            }
        });
        cList.subscribeSelection(((widget, item) -> cSelected = item));
        cList.bindList(new ReadOnlyBinding<List<MarketItem>>() {
            @Override
            public List<MarketItem> get() {
                return cItems;
            }
        });

        // Initialize result message label
        result = find("message", UILabel.class);
        result.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return message;
            }
        });

        // Initialize confirm trade button
        confirm = find("tradeButton", UIButton.class);
        confirm.subscribe(widget -> {
            localPlayer.getCharacterEntity().send(new TradeRequest(tradingUISystem.getTargetCitizen(), pList.getSelection(), cList.getSelection()));
        });

        // Initialize close dialogue button
        cancel = find("cancelButton", UIButton.class);
        cancel.subscribe(widget -> {
            nuiManager.closeScreen("MetalRenegades:tradingScreen");
        });

        // Initialize player item cost label
        pCost = find("playerCost", UILabel.class);
        pCost.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                int cost = (pSelected != null) ? (pSelected.cost) : 0;
                return "Cost: " + cost;
            }
        });

        // Initialize citizen item cost label
        cCost = find("citizenCost", UILabel.class);
        cCost.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                int cost = (cSelected != null) ? (cSelected.cost) : 0;
                return "Cost: " + cost;
            }
        });
    }

    /**
     * Set the player's inventory items
     * @param list: Content for the player's UIList
     */
    public void setPlayerItems(List<MarketItem> list) {
        pItems = list;
    }

    /**
     * Set the citizen's inventory items
     * @param list: Content for the citizen's UIList
     */
    public void setCitizenItems(List<MarketItem> list) {
        this.cItems = list;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void onClosed() {
        super.onClosed();
        message = "";
        pList.setSelection(null);
        cList.setSelection(null);
    }
}
