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

import org.terasology.economy.events.UpdateWalletEvent;
import org.terasology.economy.systems.WalletSystem;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class CurrencyManagementSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private final int COOLDOWN = 500;

    private int counter = COOLDOWN;

    @In
    private WalletSystem walletSystem;

    @Override
    public void update(float delta) {
        if (counter == 0) {
            walletSystem.wallet.send(new UpdateWalletEvent(20));
            counter = COOLDOWN;
        } else {
            counter--;
        }
    }
}
