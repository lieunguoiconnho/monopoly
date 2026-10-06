package ProjectOop.dice;

public class Dice {

    private int lastValue; // Lưu lại giá trị vừa đổ để so sánh doubles từ GameController

    public int roll() {
        lastValue = (int)(Math.random() * 6) + 1;
        return lastValue;
    }

    public int getLastValue() { return lastValue; }
}
