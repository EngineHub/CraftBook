package com.sk89q.craftbook.sponge.st;

public class SelfTriggerClock implements Runnable {

    @Override
    public void run() {
        SelfTriggerManager.think();
    }
}
