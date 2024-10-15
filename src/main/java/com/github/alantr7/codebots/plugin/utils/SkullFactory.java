package com.github.alantr7.codebots.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;

import java.net.URL;
import java.util.UUID;

public class SkullFactory {

    public static ItemStack createSkull(String url) {
        try {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            profile.getTextures().setSkin(new URL(url));
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);

            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwnerProfile(profile);

            head.setItemMeta(meta);
            return head;
        } catch (Exception e) {
            e.printStackTrace();
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }

}
