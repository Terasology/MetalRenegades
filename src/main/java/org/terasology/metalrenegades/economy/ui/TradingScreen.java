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

import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.ArrayList;
import java.util.List;

/**
 * UI for trading with citizens
 */
public class TradingScreen extends CoreScreenLayer {

    @In
    private TradingUISystem tradingUISystem;

    @In
    private NUIManager nuiManager;

    private UIList<MarketItem> player;
    private UIList<MarketItem> citizen;
    private UIButton confirm;
    private UIButton cancel;
    private UILabel result;
    private UILabel pCost;
    private UILabel cCost;

    private List<MarketItem> playerItems = new ArrayList<>();
    private List<MarketItem> citizenItems = new ArrayList<>();

    private MarketItem pSelected = MarketItemBuilder.getEmpty();
    private MarketItem cSelected = MarketItemBuilder.getEmpty();

    private String message;

    @Override
    public void initialise() {
        List<MarketItem> items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            items.add(MarketItemBuilder.getDefault());
        }

        playerItems = items;
        citizenItems = items;

        player = find("playerList", UIList.class);
        player.setList(new ArrayList<>());
        player.setItemRenderer(new StringTextRenderer<MarketItem>() {
            @Override
            public String getString(MarketItem value) {
                return value.name;
            }
        });
        player.subscribeSelection(((widget, item) -> pSelected = item));
        player.bindList(new ReadOnlyBinding<List<MarketItem>>() {
            @Override
            public List<MarketItem> get() {
                return playerItems;
            }
        });

        citizen = find("citizenList", UIList.class);
        citizen.setList(new ArrayList<>());
        citizen.setItemRenderer(new StringTextRenderer<MarketItem>() {
            @Override
            public String getString(MarketItem value) {
                return value.name;
            }
        });
        citizen.subscribeSelection(((widget, item) -> cSelected = item));
        citizen.bindList(new ReadOnlyBinding<List<MarketItem>>() {
            @Override
            public List<MarketItem> get() {
                return citizenItems;
            }
        });

        result = find("message", UILabel.class);
        result.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return message;
            }
        });

        confirm = find("tradeButton", UIButton.class);
        confirm.subscribe(widget -> {
            if (tradingUISystem.isAcceptable(player.getSelection(), citizen.getSelection())) {
                if (tradingUISystem.trade(player.getSelection(), citizen.getSelection())) {
                    message = "Trade completed.";
                    tradingUISystem.refreshLists();
                } else {
                    message = "Trade failed.";
                }
            } else {
                message = "Offer declined.";
            }
        });

        cancel = find("cancelButton", UIButton.class);
        cancel.subscribe(widget -> {
            nuiManager.closeScreen("MetalRenegades:tradingScreen");
        });


        pCost = find("playerCost", UILabel.class);
        pCost.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return "Cost: " + pSelected.cost;
            }
        });

        cCost = find("citizenCost", UILabel.class);
        cCost.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                return "Cost: " + cSelected.cost;
            }
        });
    }

    public void setPlayerItems(List<MarketItem> list) {
        playerItems = list;
    }

    public void setCitizenItems(List<MarketItem> list) {
        this.citizenItems = list;
    }

    @Override
    public void onClosed() {
        super.onClosed();
        message = "";
        player.setSelection(null);
        citizen.setSelection(null);
    }
}
