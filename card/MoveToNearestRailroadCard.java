package ProjectOop.card;

import ProjectOop.player.Player;

/**
 * Thẻ Di Chuyển Đến Ga Gần Nhất (ô 5, 15, 25, 35) theo chiều đi.
 * Nhận $200 nếu đi qua ô GO.
 */
public class MoveToNearestRailroadCard extends Card {

    public MoveToNearestRailroadCard(String description) {
        super(description);
    }

    @Override
    public void apply(Player p) {
        int pos = p.getPosition();
        int target;
        if (pos < 5 || pos >= 35) {
            target = 5;
            if (pos >= 35) {
                p.passGo(); // Đi qua ô GO
            }
        } else if (pos < 15) {
            target = 15;
        } else if (pos < 25) {
            target = 25;
        } else {
            target = 35;
        }
        p.setPosition(target);
    }
}
