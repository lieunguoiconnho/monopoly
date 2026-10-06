package ProjectOop.card;

import ProjectOop.player.Player;
import ProjectOop.square.JailSquare;

/**
 * Thẻ Vào Tù – tống người chơi vào ô Jail (vị trí 10) ngay lập tức.
 * Không nhận $200 dù có đi qua ô GO.
 */
public class JailCard extends Card {

    public JailCard(String description) {
        super(description);
    }

    @Override
    public void apply(Player p) {
        p.setPosition(JailSquare.JAIL_POSITION);
        p.setInJail(true);
        p.setTurnsInJail(0);
    }
}
