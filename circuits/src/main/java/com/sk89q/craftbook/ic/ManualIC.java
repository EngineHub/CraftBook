package com.sk89q.craftbook.ic;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public abstract class ManualIC extends AbstractIC implements IC{

    private Server server;
    private Sign sign;

    public ManualIC(Server server, Sign block) {
        super(server,block);
        this.server = server;
        this.sign = block;
    }

    @Override
    protected Server getServer() {
        return server;
    }

    @Override
    protected Sign getSign() {
        return sign;
    }

    @Override
    public void unload() {
    }

    public void click(ChipState chip, Player player) {
    }
}
