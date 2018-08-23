package gg.psyduck.bidoofunleashed.players;

import com.google.common.collect.Lists;
import gg.psyduck.bidoofunleashed.gyms.Badge;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class PlayerData {

	private final UUID uuid;

	private List<Badge> badges = Lists.newArrayList();

	private Roles role = Roles.NONE;

	public void awardBadge(Badge badge) {
		this.badges.add(badge);
	}

	public boolean hasBadge(Badge badge) {
	    return this.badges.stream().anyMatch(b -> b.getName().equals(badge.getName()) && b.getItemType().equals(badge.getItemType()));
    }

	public void removeBadge(Badge badge) {
	    if (this.hasBadge(badge)) {
	        this.badges.remove(badge);
        }
    }

	public void setRole(Roles role) {
		this.role = role;
	}
}
