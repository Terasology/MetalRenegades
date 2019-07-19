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
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.itemRendering.StringTextRenderer;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MarketScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    private UIList<String> items;

    private Logger logger = LoggerFactory.getLogger(MarketScreen.class);

    @Override
    public void initialise() {
        items = find("itemList", UIList.class);
        if (items != null) {
            items.setList(new ArrayList<>());

            items.setItemRenderer(new StringTextRenderer<String>() {
                @Override
                public String getString(String value) {
                    return value;
                }
            });

            items.subscribeSelection(((widget, item) -> handleItemSelection(item)));
        }
    }

    @Override
    public boolean isModal() {
        return true;
    }

    public void setItemList(Map<String, Integer> resources) {
        List<String> itemList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            itemList.add(entry.getKey());
        }

        items.setList(itemList);
    }

    private void handleItemSelection(String a) {
        logger.info("Selected item: {}", a);
    }
}
