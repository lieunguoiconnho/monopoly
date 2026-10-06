package ProjectOop.card;

import ProjectOop.player.Player;

/**
 * Thẻ Di Chuyển Tuyệt Đối – đưa người chơi đến ô cụ thể.
 * Nếu collectGoBonus = true và ô đích nhỏ hơn vị trí hiện tại
 * (tức là vượt qua ô GO), người chơi nhận $200.
 */
public class MoveToCard extends Card {
    private int targetPosition;
    private boolean collectGoBonus;

    public MoveToCard(String description, int targetPosition, boolean collectGoBonus) {
        super(description);
        this.targetPosition = targetPosition;
        this.collectGoBonus = collectGoBonus;
    }

    @Override
    public void apply(Player p) {
        if (collectGoBonus && targetPosition < p.getPosition()) {
            p.passGo(); // Vượt qua GO → nhận $200
        }
        p.setPosition(targetPosition);
    }
}
