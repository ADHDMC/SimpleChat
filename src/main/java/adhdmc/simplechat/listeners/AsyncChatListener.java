package adhdmc.simplechat.listeners;

import adhdmc.simplechat.SimpleChat;
import adhdmc.simplechat.utils.ChatPermission;
import adhdmc.simplechat.utils.Message;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;

public class AsyncChatListener implements Listener {
    Server server = SimpleChat.getInstance().getServer();
    MiniMessage miniMessage = SimpleChat.getMiniMessage();
    @EventHandler
    public void onPlayerChat(AsyncChatDecorateEvent chatEvent){
        String originalMessage = miniMessage.serialize(chatEvent.originalMessage());
        Player player = chatEvent.player();
        String chatFormat = Message.CHAT_FORMAT.getMessage();
        Component chatStyle = chatStyleParse(player, chatFormat);
        Component messageParsed = permissionParsedMessage(player, originalMessage);
        Component completeParsed = chatStyle.replaceText(TextReplacementConfig.builder().match("%player_message%").replacement(messageParsed).build());
        chatEvent.result(completeParsed);
    }
    //Stolen from https://github.com/YouHaveTrouble/JustChat @YouHaveTrouble
    private Component permissionParsedMessage(Player player, String message) {
        TagResolver.Builder tagResolver = TagResolver.builder();
        for(ChatPermission perm : ChatPermission.values()) {
            if (player.hasPermission(perm.getPermission()) && perm.getTagResolver() != null) {
                tagResolver.resolver(perm.getTagResolver());
            }
        }
        MiniMessage msgParser = MiniMessage.builder().tags(tagResolver.build()).build();
        return msgParser.deserialize(message);
    }
    //Stolen from https://github.com/YouHaveTrouble/JustChat @YouHaveTrouble
    private Component chatStyleParse(Player player, String style) {

        Component styleParsed = miniMessage.deserialize(style);

        if (PlaceholderAPI.containsPlaceholders(style)) {
            Matcher matcher = PlaceholderAPI.getPlaceholderPattern().matcher(style);
            while (matcher.find()) {
                String string = matcher.group(0);

                String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, string);
                if (parsedPlaceholder.equals(string)) continue;

                Component placeholderComponent = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);

                TextReplacementConfig replacementConfig = TextReplacementConfig
                        .builder()
                        .match(string)
                        .replacement(placeholderComponent)
                        .build();
                styleParsed = styleParsed.replaceText(replacementConfig);
            }
        }
        return styleParsed;
    }
}
