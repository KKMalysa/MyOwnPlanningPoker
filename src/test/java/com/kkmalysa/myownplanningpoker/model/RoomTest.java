package com.kkmalysa.myownplanningpoker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoomTest {

    @Test
    void shouldAddPlayerToRoom() {
        Room room = new Room();
        Player player = new Player("Karol", 5);

        room.getPlayers().put(player.getName(), player);

        assertEquals(1, room.getPlayers().size());
        assertTrue(room.getPlayers().containsKey("Karol"));
        assertEquals(5, room.getPlayers().get("Karol").getVote());
    }

    @Test
    void shouldCalculateAverageVoteCorrectly() {
        Room room = new Room();
        room.getPlayers().put("Ania", new Player("Ania", 3));
        room.getPlayers().put("Bartek", new Player("Bartek", 5));
        room.getPlayers().put("Chloe", new Player("Chloe", 8));

        double average = room.calculateAverageVote();

        assertEquals(5.33, average, 0.01);
    }
}
