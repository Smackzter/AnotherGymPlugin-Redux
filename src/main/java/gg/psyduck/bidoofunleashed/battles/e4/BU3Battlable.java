package gg.psyduck.bidoofunleashed.battles.e4;

import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import com.pixelmonmod.pixelmon.storage.PlayerStorage;
import gg.psyduck.bidoofunleashed.api.enums.EnumBattleType;
import gg.psyduck.bidoofunleashed.api.gyms.Requirement;
import gg.psyduck.bidoofunleashed.battles.Category;
import gg.psyduck.bidoofunleashed.config.MsgConfigKeys;
import gg.psyduck.bidoofunleashed.battles.gyms.Gym;
import gg.psyduck.bidoofunleashed.battles.battletypes.BattleType;
import gg.psyduck.bidoofunleashed.utils.MessageUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface BU3Battlable {

	/** A static requirement all gyms will have. Since we don't want gyms saving this requirement, it's better to reference here */
	Requirement lvlCapRequirement = new Requirement() {
		@Override
		public String id() {
			return "lvl-cap"; // This really won't matter, but we have it here because it's required
		}

		@Override
		public boolean passes(Gym gym, Player player) throws Exception {
			BattleType bt = gym.getBattleSettings(gym.getBattleType(player));
			PlayerStorage storage = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) player).orElseThrow(() -> new Exception("Missing player storage data for " + player.getName()));
			return Arrays.stream(storage.partyPokemon).noneMatch(nbt -> nbt != null && nbt.getInteger(NbtKeys.LEVEL) > bt.getLvlCap());
		}

		@Override
		public void onInvalid(Gym gym, Player player) {
			Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
			tokens.put("bu3_level_cap", s -> Optional.of(Text.of(gym.getBattleSettings(gym.getBattleType(player)).getBattleRules().levelCap)));
			player.sendMessage(MessageUtils.fetchAndParseMsg(player, MsgConfigKeys.REQUIREMENT_LEVELCAP, tokens, null));
		}

		@Override
		public Requirement supply(String... args) throws Exception {
			return this; // This also doesn't matter, but requirements are requirements
		}
	};

	/**
	 * States whether or not a player can challenge the implementing instance.
	 *
	 * @param player The player in question
	 * @return Whether the player can challenge the implementing instance
	 */
	boolean canChallenge(Player player);

	/**
	 * Starts a battle between two players. Since battles with NPCs are essentially pre-initialized, we really just need to do
	 * the work this method does inside the battle listener to avoid circular references.
	 *
	 * @param leader The leader of the gym accepting the challenge
	 * @param challenger The challenger trying to prove their worth
	 */
	void startBattle(Player leader, Player challenger);

	EnumBattleType getBattleType(Player player);

	BattleType getBattleSettings(EnumBattleType type);

	Category getCategory();

	int getWeight();
}
