package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.api.mod.HexItemTags;
import at.petrak.hexcasting.api.player.HexPlayerDataHelper;
import at.petrak.hexcasting.api.spell.casting.ControllerInfo;
import at.petrak.hexcasting.api.spell.casting.ResolvedPattern;
import at.petrak.hexcasting.api.spell.casting.ResolvedPatternValidity;
import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.api.spell.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexSounds;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Sent client->server when the player finishes drawing a pattern.
 * Server will send back a MsgNewSpellPatternAck packet
 */
public record MsgNewSpellPatternSyn(InteractionHand handUsed, HexPattern pattern,
                                    List<ResolvedPattern> resolvedPatterns)
    implements IMessage {
    public static final ResourceLocation ID = modLoc("patCS");

    @Override
    public ResourceLocation getFabricId() {
        return ID;
    }

    public static MsgNewSpellPatternSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = buf.readEnum(InteractionHand.class);
        var pattern = HexPattern.DeserializeFromNBT(buf.readAnySizeNbt());

        var resolvedPatternsLen = buf.readInt();
        var resolvedPatterns = new ArrayList<ResolvedPattern>(resolvedPatternsLen);
        for (int i = 0; i < resolvedPatternsLen; i++) {
            resolvedPatterns.add(ResolvedPattern.DeserializeFromNBT(buf.readAnySizeNbt()));
        }
        return new MsgNewSpellPatternSyn(hand, pattern, resolvedPatterns);
    }

    @Override
    public void serialize(FriendlyByteBuf buf) {
        buf.writeEnum(handUsed);
        buf.writeNbt(this.pattern.serializeToNBT());
        buf.writeInt(this.resolvedPatterns.size());
        for (var pat : this.resolvedPatterns) {
            buf.writeNbt(pat.serializeToNBT());
        }
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        var held = sender.getItemInHand(this.handUsed);
        if (held.is(HexItemTags.WANDS)) {
            boolean autoFail = false;

            if (!resolvedPatterns.isEmpty()) {
                var allPoints = new ArrayList<HexCoord>();
                for (int i = 0; i < resolvedPatterns.size() - 1; i++) {
                    ResolvedPattern pat = resolvedPatterns.get(i);
                    allPoints.addAll(pat.getPattern().positions(pat.getOrigin()));
                }
                var currentResolvedPattern = resolvedPatterns.get(resolvedPatterns.size() - 1);
                var currentSpellPoints = currentResolvedPattern.getPattern()
                    .positions(currentResolvedPattern.getOrigin());
                if (currentSpellPoints.stream().anyMatch(allPoints::contains)) {
                    autoFail = true;
                }
            }

            var harness = HexPlayerDataHelper.getHarness(sender, this.handUsed);

            ControllerInfo clientInfo;
            if (autoFail) {
                clientInfo = new ControllerInfo(false, false, harness.getStack().isEmpty(), true,
                    harness.generateDescs());
            } else {
                clientInfo = harness.executeNewPattern(this.pattern, sender.getLevel());

                if (clientInfo.getWasSpellCast() && clientInfo.getHasCastingSound()) {
                    sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(),
                        HexSounds.ACTUALLY_CAST.get(), SoundSource.PLAYERS, 1f,
                        1f + ((float) Math.random() - 0.5f) * 0.2f);
                }
            }

            if (clientInfo.isStackClear()) {
                HexPlayerDataHelper.setHarness(sender, null);
                HexPlayerDataHelper.setPatterns(sender, List.of());
            } else {
                HexPlayerDataHelper.setHarness(sender, harness);
                if (!resolvedPatterns.isEmpty()) {
                    resolvedPatterns.get(resolvedPatterns.size() - 1).setValid(clientInfo.getWasPrevPatternInvalid() ?
                        ResolvedPatternValidity.ERROR : ResolvedPatternValidity.OK);
                }
                HexPlayerDataHelper.setPatterns(sender, resolvedPatterns);
            }

            HexMessages.getNetwork()
                .send(PacketDistributor.PLAYER.with(() -> sender), new MsgNewSpellPatternAck(clientInfo));
        }
    }

}