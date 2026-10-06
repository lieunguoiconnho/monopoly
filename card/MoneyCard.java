package ProjectOop.card;

import ProjectOop.player.Player;

/**
 * Thẻ Tiền – thêm (amount > 0) hoặc trừ (amount < 0) tiền người chơi.
 */
public class MoneyCard extends Card {
    private int amount;

    public MoneyCard(String description, int amount) {
        super(description);
        this.amount = amount;
    }

    @Override
    public void apply(Player p) {
        if (amount >= 0) {
            p.addMoney(amount);
        } else {
            if (!p.deductMoney(-amount)) {
                p.deductMoney(p.getBalance());
                p.setBankrupt(true);
            }
        }
    }
}
