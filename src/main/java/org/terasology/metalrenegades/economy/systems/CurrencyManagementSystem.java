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
import org.terasology.economy.events.UpdateWalletEvent;
import org.terasology.economy.systems.WalletSystem;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class CurrencyManagementSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private final int COOLDOWN = 100;
    private final String UPDATE_WALLET = "update_wallet";

    private int counter = COOLDOWN;

    @In
    private WalletSystem walletSystem;

//    @In
//    private DelayManager delayManager;
//
//    @Override
//    public void initialise() {
//        delayManager.addPeriodicAction(walletSystem.wallet, UPDATE_WALLET, 1000, 3000);
//    }

//    private Logger logger = LoggerFactory.getLogger(CurrencyManagementSystem.class);

    @Override
    public void update(float delta) {
//        logger.info("Adding money to wallet ...");
        if (counter == 0) {
//            logger.info("\n\n ADDING MONEY TO WALLET\n\n");
            walletSystem.wallet.send(new UpdateWalletEvent(20));
            counter = COOLDOWN;
        } else {
            counter--;
        }
    }
}
