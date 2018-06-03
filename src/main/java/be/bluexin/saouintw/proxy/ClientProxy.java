package be.bluexin.saouintw.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Part of saouintw, the networking mod for the SAO UI
 *
 * @author Bluexin
 */
@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
    }
}
