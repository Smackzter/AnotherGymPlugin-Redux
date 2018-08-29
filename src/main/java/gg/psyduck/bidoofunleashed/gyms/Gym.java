package gg.psyduck.bidoofunleashed.gyms;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.psyduck.bidoofunleashed.api.rewards.BU3Reward;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PixelmonStorage;
import gg.psyduck.bidoofunleashed.api.enums.EnumBattleType;
import gg.psyduck.bidoofunleashed.api.enums.EnumLeaderType;
import gg.psyduck.bidoofunleashed.api.gyms.Requirement;
import gg.psyduck.bidoofunleashed.gyms.temporary.BattleRegistry;
import gg.psyduck.bidoofunleashed.gyms.temporary.Challenge;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.world.Location;

import java.io.File;
import java.util.*;

@Getter
public class Gym {


	private String name;
	private Badge badge;
	private Arena arena;
	private List<Requirement> requirements;
	private Map<EnumBattleType, List<BU3Reward>> rewards;
	private List<String> rules;
	private List<String> clauses;
	private Map<UUID, EnumLeaderType> leaders;

	private GymPool pool;

	private transient BattleRules battleRules;
	private transient Queue<Player> queue = new LinkedList<>();
	private transient boolean open = false;

	private static final File BASE_PATH_GYMS = new File("bidoof-unleashed-3/json/gyms/");

	public Gym(Builder builder) {
		this.name = builder.name;
		this.badge = builder.badge;
		this.arena = builder.arena;
		this.requirements = builder.requirements;
		this.rewards = builder.rewards;
		this.leaders = builder.leaders;

		this.rules = builder.rules;
		this.clauses = builder.clauses;
		this.pool = new GymPool(new File("./" + BASE_PATH_GYMS, this.name + "/pool.json")).init();
		this.initialize();
	}

	public void addLeader(UUID uuid, EnumLeaderType type) {
		this.leaders.put(uuid, type);
	}

	private boolean canChallenge(Player player) {
		return this.queue != null && this.arena.isSetup() && player.hasPermission("bu3.gyms." + this.name.toLowerCase().replaceAll(" ", "_") + ".contest");
	}

	public void queue(Player player) {
		if(this.canChallenge(player)) {
			this.queue.add(player);
		}
	}

	public void startBattle(Entity leader, Player challenger) {
		BattleParticipant bpL;
		if (leader instanceof NPCTrainer) {
			bpL = new TrainerParticipant((NPCTrainer) leader, (EntityPlayerMP) challenger, ((NPCTrainer) leader).getPokemonStorage().count());
		} else {
			EntityPixelmon starter = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) leader).get().getFirstAblePokemon((World) leader.getWorld());
			bpL = new PlayerParticipant((EntityPlayerMP) leader, starter);
			leader.setLocationAndRotationSafely(new Location<>(leader.getWorld(), arena.leader.position), arena.leader.rotation);
		}

		challenger.setLocationAndRotationSafely(new Location<>(challenger.getWorld(), arena.challenger.position), arena.challenger.rotation);
		EntityPixelmon starter = PixelmonStorage.pokeBallManager.getPlayerStorage((EntityPlayerMP) challenger).get().getFirstAblePokemon((World) challenger.getWorld());
		new BattleControllerBase(bpL, new PlayerParticipant((EntityPlayerMP) challenger, starter), battleRules);
		BattleRegistry.register(new Challenge(leader, challenger), this);
	}

	public Gym initialize() {
		String impTxt = repImportText(this.rules);
		impTxt += repImportText(this.clauses);

		this.battleRules = new BattleRules(impTxt);

		this.queue = new LinkedList<>();
		this.pool.init();
		return this;
	}

	private String repImportText(List<String> content) {
		StringBuilder builder = new StringBuilder();
		for(String str : content) {
			builder.append(str).append("\n");
		}

		return builder.toString();
	}

	public void setOpen(boolean value) {
	    this.open = value;
    }

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Represents the areas a player will be teleported based on their relation to the current instance
	 */
	@AllArgsConstructor
	public static class Arena {
		private LocAndRot spectators;
		private LocAndRot challenger;
		private LocAndRot leader;

		boolean isSetup() {
			return this.challenger != null && this.leader != null;
		}
	}

	@AllArgsConstructor
	public static class LocAndRot {
		private Vector3d position;
		private Vector3d rotation;
	}

	public static class Builder {
		private String name;
		private Badge badge;
		private Arena arena;

		private List<Requirement> requirements = Lists.newArrayList();
		private Map<EnumBattleType, List<BU3Reward>> rewards = Maps.newHashMap();

		private List<String> rules = Lists.newArrayList();
		private List<String> clauses = Lists.newArrayList();

		private Map<UUID, EnumLeaderType> leaders = Maps.newHashMap();

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder badge(Badge badge) {
			this.badge = badge;
			return this;
		}

		public Builder arena(Arena arena) {
			this.arena = arena;
			return this;
		}

		public Builder clause(String clause) {
			Preconditions.checkArgument(BattleClauseRegistry.getClauseRegistry().hasClause(clause), "Invalid clause: " + clause);
			this.clauses.add(clause);
			return this;
		}

		public Builder clauses(String... clauses) {
			Arrays.stream(clauses).forEach(clause -> Preconditions.checkArgument(BattleClauseRegistry.getClauseRegistry().hasClause(clause), "Invalid clause: " + clause));
			this.clauses.addAll(Arrays.asList(clauses));
			return this;
		}

		public Builder rule(String rule) {
			this.rules.add(rule);
			return this;
		}

		public Builder rules(String... rules) {
			this.rules.addAll(Arrays.asList(rules));
			return this;
		}

		public Builder levelCap(int cap) {
			Preconditions.checkArgument(cap > 0 && cap < PixelmonConfig.maxLevel);
			this.rules.add("LevelCap: " + cap);
			return this;
		}

		public Builder rewards(EnumBattleType type, BU3Reward... rewards) {
			this.rewards.put(type, Arrays.asList(rewards));
			return this;
		}

		public Builder leader(UUID uuid, EnumLeaderType type) {
			leaders.put(uuid, type);
			return this;
		}

		public Builder requirements(Requirement... requirements) {
			this.requirements.addAll(Arrays.asList(requirements));
			return this;
		}

		public Gym build() {
			Preconditions.checkNotNull(this.name);
			Preconditions.checkNotNull(this.badge);

			return new Gym(this);
		}
	}
}
