package net.phoenix.chatcopy.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.phoenix.chatcopy.mixin.accessors.ChatHudAccessor;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.IntStream;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Inject(method = "mouseClicked",
            at = @At("HEAD"))
    private void onChatClick(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(client.currentScreen instanceof ChatScreen)) return;
        if (!InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
            return;
        }
        ChatHudLine message = getMessageAt(mouseX, mouseY);
        if (message != null) {
            String toCopy = wrapCoding(message.content());
            client.keyboard.setClipboard(toCopy);
        }
    }

    @Unique
    private String wrapCoding(Text origCoded) {
        return origCoded.getString().replaceAll("(§[0-9a-fklmnor])", "{$1}");
    }

    @Unique
    private ChatHudLine getMessageAt(double x, double y) {
        ChatHudAccessor accessor = (ChatHudAccessor) MinecraftClient.getInstance().inGameHud.getChatHud();
        int lineSelected = accessor.invokeGetMessageLineIndex(
                accessor.invokeToChatLineX(x), accessor.invokeToChatLineY(y));

        if (lineSelected == -1) return null;

        List<Integer> indexesOfEntryEnds = IntStream.range(0, accessor.getVisibleMessages().size())
                .filter(index -> accessor.getVisibleMessages().get(index).endOfEntry())
                .boxed()
                .toList();

        int indexOfMessageEntryEnd = indexesOfEntryEnds
                .stream()
                .filter(index -> index <= lineSelected)
                .reduce((a, b) -> b)
                .orElse(-1);

        if (indexOfMessageEntryEnd == -1) {
            LogManager.getLogger().warn("Something cursed happened (indexOfMessageEntryEnd == -1)");
        }

        int indexOfMessage = indexesOfEntryEnds.indexOf(indexOfMessageEntryEnd);
        return accessor.getMessages().get(indexOfMessage);
    }

}