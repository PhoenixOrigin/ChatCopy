package net.phoenix.chatcopy.mixin;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {

    @ModifyVariable(method = "onGameMessage", at = @At(value = "LOAD", ordinal = 0), ordinal = 0, argsOnly = true)
    private Text fabric_modifyGameMessage(Text message, Text message1, boolean overlay) {
        MutableText new_message = Text.empty();
        message.getSiblings().forEach(text -> {
            if (text.getStyle().getHoverEvent() == null) {
                new_message.append(text.copy().setStyle(text.getStyle().withColor(text.getStyle().getColor()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Alt + Click to copy")))));
            }
        });
        return new_message;
    }

    @Unique
    private String wrapCoding(Text origCoded) {
        return origCoded.getString().replaceAll("(§[0-9a-fklmnor])", "{$1}");
    }

}
