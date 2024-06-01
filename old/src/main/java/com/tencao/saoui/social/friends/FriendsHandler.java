/*
 * Copyright (C) 2016-2024 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tencao.saoui.social.friends;

import com.tencao.saomclib.Client;
import com.tencao.saoui.config.ConfigHandler;
import com.tencao.saoui.social.StaticPlayerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Part of SAOUI
 *
 * @author Bluexin
 */
public class FriendsHandler {

    private static FriendsHandler instance;
    private final File friendsFile;
    private final List<FriendRequest> friendRequests = new ArrayList<>();
    private String[] friends;

    private FriendsHandler(FMLPreInitializationEvent event) {
        this.friendsFile = new File(Client.INSTANCE.getMinecraft().mcDataDir.getAbsolutePath(), "saouifriends");
        if (!friendsFile.exists()) writeFriends(friends);
        friends = loadFriends();
    }

    public static FriendsHandler instance() {
        return instance;
    }

    public static void preInit(FMLPreInitializationEvent event) {
        instance = new FriendsHandler(event);
    }

    public String[] loadFriends() {
        try {
            final FileInputStream stream = new FileInputStream(friendsFile);
            final String[] friends;

            if (stream.available() != 0) {
                final int count = (stream.read() & 0xFF);

                friends = new String[count];

                for (int i = 0; i < count; i++) {
                    final int length = (stream.read() & 0xFF);
                    final byte[] bytes = new byte[length];

                    stream.read(bytes, 0, length);

                    friends[i] = new String(bytes);
                }
            } else friends = new String[0];

            stream.close();

            return friends;
        } catch (IOException e) {
            return new String[0];
        }
    }

    public String[] listFriends() {
        if (friends == null) friends = loadFriends();

        return friends;
    }

    public boolean addFriends(EntityPlayer... players) {
        friends = listFriends();
        final ArrayList<String> newNames = new ArrayList<>();

        Stream.of(players).map(EntityPlayer::getDisplayNameString).forEach(player -> {
            if (Stream.of(friends).noneMatch(friend -> friend.equals(player))) newNames.add(player);
        });

        String[] bb = new String[newNames.size()];
        System.arraycopy(newNames.toArray(new String[0]), 0, bb, 0, bb.length);
        return newNames.size() <= 0 || addRawFriends(bb);
    }

    public boolean isFriend(String name) {
        return Arrays.asList(listFriends()).contains(name);
    }

    public boolean isFriend(EntityPlayer player) {
        return isFriend(StaticPlayerHelper.INSTANCE.getName(player));
    }

    public boolean addRawFriends(String[] names) {
        friends = listFriends();

        final String[] resized = new String[friends.length + names.length];

        System.arraycopy(friends, 0, resized, 0, friends.length);
        System.arraycopy(names, 0, resized, friends.length, names.length);

        if (writeFriends(resized)) {
            friends = resized;
            return true;
        } else return false;
    }

    public boolean writeFriends(String[] friends) {
        final String[] data = friends == null ? new String[0] : friends;

        synchronized (friendsFile) {
            try (FileOutputStream stream = new FileOutputStream(friendsFile)) {
                final int count = (data.length % 0x100);
                stream.write(count);

                for (int i = 0; i < count; i++) {
                    final byte[] bytes = data[i].getBytes();
                    final int length = (bytes.length % 0x100);

                    stream.write(length);
                    stream.write(bytes, 0, length);
                }

                stream.flush();
                stream.close();

                return true;
            } catch (IOException e) {
                if (ConfigHandler.INSTANCE.getEnableDebug()) e.printStackTrace();

                return false;
            }
        }
    }

    public void acceptAddFriend(EntityPlayer player) {
        synchronized (friendRequests) {
            int index = -1;

            for (int i = 0; i < friendRequests.size(); i++)
                if (friendRequests.get(i).equals(player.getDisplayNameString())) {
                    index = i;
                    break;
                }

            if (index >= 0 && (isFriend(player) || addFriends(player))) friendRequests.remove(index);
        }
    }

    public void cancelAddFriend(EntityPlayer player) {
        synchronized (friendRequests) {
            int index = -1;

            for (int i = 0; i < friendRequests.size(); i++)
                if (friendRequests.get(i).equals(player.getDisplayNameString())) {
                    index = i;
                    break;
                }

            if (index >= 0) friendRequests.remove(index);
        }
    }

}
