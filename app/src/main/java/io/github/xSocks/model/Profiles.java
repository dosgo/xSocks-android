package io.github.xSocks.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Profiles {
    @SerializedName("profiles")
    private List<Profile> profiles;

    public List<Profile> getProfiles() {
        return profiles;
    }

    public Profiles() {
        profiles = new ArrayList<>();
    }

    public void remove(int id) {
        for (Profile p : profiles) {
            if (p.getId() == id) {
                profiles.remove(p);
                break;
            }
        }
    }

    public Profile getProfile(int id) {
        for (Profile p : profiles) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public int getMaxId() {
        int max = 0;
        for (Profile p : profiles) {
            if (p.getId() > max) {
                max = p.getId();
            }
        }
        return max;
    }

    public void addProfile(Profile profile) {
        profiles.add(profile);
    }
}
