package ProjectOop.card;

import ProjectOop.player.Player;

/**
 * Lớp cha trừu tượng cho mọi loại thẻ bài (Cơ Hội & Khí Vận).
 * Mỗi thẻ có mô tả và phương thức apply() tác động lên người rút.
 */
public abstract class Card {
    protected String description;

    public Card(String description) {
        this.description = description;
    }

    /** Áp dụng hiệu ứng thẻ lên người chơi p. */
    public abstract void apply(Player p);

    public String getDescription() { return description; }
}
