package gg.psyduck.bidoofunleashed.battles.gyms;

import com.google.common.collect.Lists;
import gg.psyduck.bidoofunleashed.api.spec.BU3PokemonSpec;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Getter
public class GymPool {

	private final File pool;
    private transient List<BU3PokemonSpec> team = Lists.newArrayList();

    public GymPool(File pool) {
    	this.pool = pool;
    	this.createFiles(pool);
    }

    public GymPool init() {
    	this.team = ShowdownImporter.importFromFile(pool);
    	return this;
    }

    private void createFiles(File path) {
	    if(!path.exists()) {
		    path.getParentFile().mkdirs();
		    try {
			    path.createNewFile();
		    } catch (IOException ignored) {}
	    }
    }
}
