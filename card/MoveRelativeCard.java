package ProjectOop.card;

import ProjectOop.player.Player;

/**
 * Thẻ Di Chuyển Tương Đối – dịch chuyển người chơi một số ô
 * so với vị trí hiện tại (có thể âm → lùi về sau).
 * Không kích hoạt passGo khi lùi, chỉ kích hoạt khi tiến qua ô 0.
 */
public class MoveRelativeCard extends Card {
    private int steps;

    public MoveRelativeCard(String description, int steps) {
        super(description);
        this.steps = steps;
    }

    @Override
    public void apply(Player p) {
        int newPos = p.getPosition() + steps;
        if (steps > 0 && newPos >= 40) {
            p.passGo(); // Tiến và vượt qua GO
            newPos = newPos % 40;
        } else if (steps < 0 && newPos < 0) {
            newPos = (newPos + 40) % 40; // Lùi không nhận GO
        }
        p.setPosition(newPos);
    }
}
