package ProjectOop.square;

import ProjectOop.player.*;

/**
 * Ô Thuế – 2 ô trên bàn cờ:
 *   Ô 4: Thuế Thu Nhập ($200)
 *   Ô 38: Thuế Xa Xỉ ($100)
 */
public class TaxSquare extends Square {
    private int taxAmount;

    public TaxSquare(int position, String name, int taxAmount) {
        super(position, name);
        this.taxAmount = taxAmount;
    }

    @Override
    public void applyEffect(Player p) {
        System.out.println(p.getName() + " phải nộp thuế \"" + name + "\": $" + taxAmount);
        if (!p.deductMoney(taxAmount)) {
            // Không đủ tiền: trừ hết rồi đánh dấu phá sản
            p.deductMoney(p.getBalance());
            p.setBankrupt(true);
        }
    }

    public int getTaxAmount() { return taxAmount; }
}
