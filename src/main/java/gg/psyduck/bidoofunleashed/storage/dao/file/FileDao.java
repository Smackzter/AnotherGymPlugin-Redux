package gg.psyduck.bidoofunleashed.storage.dao.file;

import com.google.common.collect.Lists;
import com.nickimpact.impactor.api.plugins.SpongePlugin;
import gg.psyduck.bidoofunleashed.BidoofUnleashed;
import gg.psyduck.bidoofunleashed.gyms.Gym;
import gg.psyduck.bidoofunleashed.players.PlayerData;
import gg.psyduck.bidoofunleashed.storage.dao.AbstractBU3Dao;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FileDao extends AbstractBU3Dao {

	private static final File BASE_PATH_PLAYERS = new File("bidoof-unleashed-3/json/players/");
	private static final File BASE_PATH_GYMS = new File("bidoof-unleashed-3/json/gyms/");

	public FileDao(SpongePlugin plugin) {
		super(plugin, "Flatfile");
	}

	@Override
	public void init() throws Exception {
		if(!BASE_PATH_PLAYERS.exists()) {
			BASE_PATH_PLAYERS.mkdirs();
		}

		if(!BASE_PATH_GYMS.exists()) {
			BASE_PATH_GYMS.mkdirs();
		}
	}

	@Override
	public void shutdown() throws Exception {}

	@Override
	public void addPlayerData(PlayerData data) throws Exception {
		this.addOrUpdatePlayerData(data);
	}

	@Override
	public void updatePlayerData(PlayerData data) throws Exception {
		this.addOrUpdatePlayerData(data);
	}

	private void addOrUpdatePlayerData(PlayerData data) throws Exception {
		File target = new File(BASE_PATH_PLAYERS, data.getUuid().toString().substring(0, 2) + "/" + data.getUuid().toString() + ".json");
		if(!target.exists()) {
			target.getParentFile().mkdirs();
			target.createNewFile();
		}

		FileWriter writer = new FileWriter(target);
		writer.write(BidoofUnleashed.prettyGson.toJson(data));
		writer.close();
	}

	@Override
	public Optional<PlayerData> getPlayerData(UUID uuid) throws Exception {
		File target = new File(BASE_PATH_PLAYERS, uuid.toString().substring(0, 2) + "/" + uuid.toString() + ".json");
		if(!target.exists()) {
			return Optional.empty();
		}

		return Optional.of(BidoofUnleashed.prettyGson.fromJson(new FileReader(target), PlayerData.class));
	}

	@Override
	public void addGym(Gym gym) throws Exception {
		this.addOrUpdateGym(gym);
	}

	@Override
	public void updateGym(Gym gym) throws Exception {
		this.addOrUpdateGym(gym);
	}

	private void addOrUpdateGym(Gym gym) throws Exception {
		File target = new File(BASE_PATH_GYMS, gym.getName() + "/" + gym.getName() + ".json");
		if(!target.exists()) {
			target.getParentFile().mkdirs();
			target.createNewFile();
		}

		FileWriter writer = new FileWriter(target);
		writer.write(BidoofUnleashed.prettyGson.toJson(gym));
		writer.close();
	}

	@Override
	public List<Gym> fetchGyms() throws Exception {
		List<Gym> gyms = Lists.newArrayList();
		for (File base : Objects.requireNonNull(BASE_PATH_GYMS.listFiles())) {
			for (File gym : Objects.requireNonNull(base.listFiles((d, s) -> s.toLowerCase().endsWith(".json")))) {
				gyms.add(BidoofUnleashed.prettyGson.fromJson(new FileReader(gym), Gym.class).initialize());
			}
		}

		return gyms;
	}

    @Override
    public void removeGym(Gym gym) throws Exception {
        File target = new File(BASE_PATH_GYMS, gym.getName() + "/" + gym.getName() + ".json");
        if(target.exists()) {
            target.delete();
        }
    }
}
